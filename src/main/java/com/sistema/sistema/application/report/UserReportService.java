package com.sistema.sistema.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.application.dto.response.user.UserViewResponse;
import com.sistema.sistema.domain.usecase.UserUseCase;
import com.sistema.sistema.infrastructure.report.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserReportService {
    private final UserUseCase userUseCase;

    public UserReportService(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    // 📌 Generación de PDF
    public byte[] generatePdfReport(UserViewRequest request, String username) {
        UserViewResponse response = userUseCase.init(request);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ---- Construcción dinámica de filtros ----
            Map<String, String> filters = new LinkedHashMap<>();
            if (request.getName() != null && !request.getName().isBlank()) {
                filters.put("Nombre", request.getName());
            }

            boolean hasFilters = !filters.isEmpty();

            // Calcular margen superior dinámico
            int baseMarginTop = 90;
            int filterRows = (int) Math.ceil((double) filters.size() / 2);
            int extraMargin = filterRows * 15;
            int marginTop = baseMarginTop + extraMargin;

            Document document = new Document(PageSize.A4, 36, 36, marginTop, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Header (sin filtros, solo título y usuario)
            writer.setPageEvent(new PdfReportUtil("Reporte de usuarios", username));

            document.open();

            // ---- Renderizamos filtros ----
            if (hasFilters) {
                com.lowagie.text.Font filterTitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font filterFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);

                Paragraph filterTitle = new Paragraph("Filtros aplicados:", filterTitleFont);
                filterTitle.setSpacingAfter(5f);
                document.add(filterTitle);

                PdfPTable filtersTable = PdfTableUtil.createFiltersTable(filters, filterFont);
                document.add(filtersTable);
            }

            // ---- Cabecera de la tabla ----
            List<String> columnTitles = Arrays.asList("Nº" ,"Usuario", "Correo Electrónico", "Estado");
            float[] columnWidths = {1f, 3f, 4f, 2f};

            PdfPTable table = PdfTableUtil.createHeader(columnTitles, columnWidths);

            if (!hasFilters) {
                table.setSpacingBefore(10f);
            }

            com.lowagie.text.Font bodyFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);
            List<Integer> alignments = Arrays.asList(
                    Element.ALIGN_CENTER,
                    Element.ALIGN_LEFT,
                    Element.ALIGN_LEFT,
                    Element.ALIGN_CENTER
            );

            int index = 1;
            for (var user : response.getUsers()) {
                PdfTableUtil.addRow(
                        table,
                        Arrays.asList(
                                String.valueOf(index++),
                                user.getUsername(),
                                user.getEmail(),
                                user.getActive() ? "Habilitado" : "Inhabilitado"
                        ),
                        bodyFont,
                        alignments
                );
            }

            document.add(table);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    // 📌 Generación de Excel
    public byte[] generateExcelReport(UserViewRequest request, String username) {
        UserViewResponse response = userUseCase.init(request);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ---- Única Hoja ----
            Sheet sheet = workbook.createSheet("Reporte de Usuarios");
            int rowIdx = 0;

            // ---- Encabezado (utilidad) ----
            ExcelReportHeaderUtil.createCoverPage(workbook, sheet, "Reporte de Usuarios", username, 5);
            rowIdx = sheet.getLastRowNum() + 2;

            CellStyle bodyStyle = workbook.createCellStyle();
            bodyStyle.setWrapText(true);

            // ---- Filtros aplicados ----
            Map<String, String> filters = new LinkedHashMap<>();
            if (request.getName() != null && !request.getName().isBlank()) {
                filters.put("Nombre", request.getName());
            }

            if (!filters.isEmpty()) {
                // Estilo negrita
                CellStyle boldStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);

                Row filterTitleRow = sheet.createRow(rowIdx++);
                Cell filterTitleCell = filterTitleRow.createCell(0);
                filterTitleCell.setCellValue("Filtros aplicados:");
                filterTitleCell.setCellStyle(boldStyle);

                for (Map.Entry<String, String> entry : filters.entrySet()) {
                    Row filterRow = sheet.createRow(rowIdx++);
                    Cell keyCell = filterRow.createCell(0);
                    keyCell.setCellValue(entry.getKey());
                    keyCell.setCellStyle(boldStyle); // <-- título del filtro en negrita

                    filterRow.createCell(1).setCellValue(entry.getValue());
                }
                rowIdx++;
            }

            String[] columns = {"Nº" ,"Usuario", "Correo Electrónico", "Estado"};
            HorizontalAlignment[] alignments = {
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER
            };

            rowIdx = ExcelReportTableUtil.createTableHeader(
                    workbook,
                    sheet,
                    rowIdx,
                    columns,
                    alignments,
                    (short) 11
            );

            // Definir alineaciones por columna
            HorizontalAlignment[] dataAlignments = {
                    HorizontalAlignment.CENTER,   // Nº
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.CENTER
            };

            // Escribir filas dinámicamente
            int index = 1;
            for (var user : response.getUsers()) {
                Object[] rowValues = {
                        index++,
                        user.getUsername(),
                        user.getEmail(),
                        user.getActive() ? "Habilitado" : "Inhabilitado"
                };

                rowIdx = ExcelReportDataUtil.writeDataRow(
                        workbook, sheet, rowIdx,
                        rowValues, dataAlignments, (short) 10
                );
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar en bytes
            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
