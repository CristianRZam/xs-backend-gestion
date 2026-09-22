package com.sistema.sistema.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.sistema.application.dto.request.report.ReportFilterRequest;
import com.sistema.sistema.infrastructure.report.*;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class BusinessReportService {
    @PersistenceContext private EntityManager em;

    public byte[] excel(String type, ReportFilterRequest filter) {
        Report report = query(type, filter);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(report.title);
            ExcelReportHeaderUtil.createCoverPage(wb, sheet, report.title, SecurityUtil.getCurrentUsername(), report.columns.length);
            int row = sheet.getLastRowNum() + 2;
            Row dates = sheet.createRow(row++);
            dates.createCell(0).setCellValue("Periodo: " + period(filter));
            HorizontalAlignment[] aligns = new HorizontalAlignment[report.columns.length];
            Arrays.fill(aligns, HorizontalAlignment.LEFT);
            row = ExcelReportTableUtil.createTableHeader(wb, sheet, row, report.columns, aligns, (short) 11);
            for (Object[] values : report.rows) row = ExcelReportDataUtil.writeDataRow(wb, sheet, row, values, aligns, (short) 10);
            for (int i=0;i<report.columns.length;i++) sheet.autoSizeColumn(i);
            wb.write(out); return out.toByteArray();
        } catch (Exception e) { throw new RuntimeException("No se pudo generar el Excel", e); }
    }

    public byte[] pdf(String type, ReportFilterRequest filter) {
        Report report = query(type, filter);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 90, 32);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PdfReportUtil(report.title, SecurityUtil.getCurrentUsername()));
            doc.open();
            doc.add(new Paragraph("Periodo: " + period(filter)));
            PdfPTable table = PdfTableUtil.createHeader(Arrays.asList(report.columns), equal(report.columns.length));
            Font font = new Font(Font.HELVETICA, 8);
            List<Integer> aligns = Collections.nCopies(report.columns.length, Element.ALIGN_LEFT);
            for (Object[] values : report.rows) PdfTableUtil.addRow(table, Arrays.stream(values).map(String::valueOf).toList(), font, aligns);
            doc.add(table); doc.close(); return out.toByteArray();
        } catch (Exception e) { throw new RuntimeException("No se pudo generar el PDF", e); }
    }

    @SuppressWarnings("unchecked") private Report query(String type, ReportFilterRequest f) {
        LocalDate start = f.getStartDate() == null ? LocalDate.now().minusDays(30) : f.getStartDate();
        LocalDate end = f.getEndDate() == null ? LocalDate.now() : f.getEndDate();
        if (end.isBefore(start)) throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        String sql; String title; String[] columns;
        switch (type.toLowerCase()) {
            case "sales" -> { title="Reporte de ventas"; columns=new String[]{"N° venta","Fecha","Total (S/)"}; sql="SELECT sale_number, DATE(created_at), total FROM sales WHERE status='COMPLETED' AND deleted_at IS NULL AND DATE(created_at) BETWEEN :start AND :end ORDER BY created_at"; }
            case "products" -> { title="Productos vendidos"; columns=new String[]{"Producto","Unidades","Importe (S/)"}; sql="SELECT p.name,SUM(si.quantity),SUM(si.subtotal) FROM sale_items si JOIN sales s ON s.id=si.sale_id JOIN products p ON p.id=si.product_id WHERE s.status='COMPLETED' AND s.deleted_at IS NULL AND DATE(s.created_at) BETWEEN :start AND :end GROUP BY p.name ORDER BY SUM(si.quantity) DESC"; }
            case "payments" -> { title="Métodos de pago"; columns=new String[]{"Método","Importe (S/)"}; sql="SELECT p.payment_method,SUM(p.amount) FROM payments p JOIN sales s ON s.id=p.sale_id WHERE s.status='COMPLETED' AND s.deleted_at IS NULL AND DATE(s.created_at) BETWEEN :start AND :end GROUP BY p.payment_method ORDER BY SUM(p.amount) DESC"; }
            case "cash" -> { title="Reporte de caja"; columns=new String[]{"Caja","Apertura","Cierre","Inicial (S/)","Esperado (S/)","Contado (S/)","Diferencia (S/)"}; sql="SELECT id,opened_at,closed_at,opening_amount,expected_amount,closing_amount,difference FROM cash_sessions WHERE deleted_at IS NULL AND DATE(opened_at) BETWEEN :start AND :end ORDER BY opened_at"; }
            case "orders" -> { title="Reporte de órdenes"; columns=new String[]{"N° orden","Fecha","Tipo","Estado"}; sql="SELECT order_number,DATE(created_at),order_type,status FROM orders WHERE deleted_at IS NULL AND DATE(created_at) BETWEEN :start AND :end ORDER BY created_at"; }
            default -> throw new IllegalArgumentException("Tipo de reporte inválido");
        }
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("start", start).setParameter("end", end).getResultList();
        for(Object[] row:rows) for(int i=0;i<row.length;i++) if(row[i] instanceof BigDecimal n) row[i]=String.format(Locale.US,"S/ %.2f",n);
        return new Report(title, columns, rows);
    }
    private String period(ReportFilterRequest f) { return (f.getStartDate()==null?LocalDate.now().minusDays(30):f.getStartDate()) + " al " + (f.getEndDate()==null?LocalDate.now():f.getEndDate()); }
    private float[] equal(int count) { float[] values=new float[count]; Arrays.fill(values, 1f); return values; }
    private record Report(String title, String[] columns, List<Object[]> rows) {}
}
