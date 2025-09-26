package com.sistema.sistema.application.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.domain.usecase.RoleUseCase;
import com.sistema.sistema.infrastructure.report.*;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
public class RoleReportService {

    private final RoleUseCase roleUseCase;

    public RoleReportService(RoleUseCase roleUseCase) {
        this.roleUseCase = roleUseCase;
    }

    // 📌 Generación de PDF
    public byte[] generatePdfReport(RoleViewRequest request) {
        RoleViewResponse response = roleUseCase.init(request);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ---- Construcción dinámica de filtros ----
            Map<String, String> filters = buildFilters(request);
            boolean hasFilters = !filters.isEmpty();

            // Calcular margen superior dinámico
            int baseMarginTop = 90;
            int filterRows = (int) Math.ceil((double) filters.size() / 2);
            int extraMargin = filterRows * 15;
            int marginTop = baseMarginTop + extraMargin;

            Document document = new Document(PageSize.A4, 36, 36, marginTop, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Header (sin filtros, solo título y usuario)
            String currentUserUsername = SecurityUtil.getCurrentUsername();
            writer.setPageEvent(new PdfReportUtil("Reporte de Roles", currentUserUsername));

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
            List<String> columnTitles = Arrays.asList("Nº", "Nombre", "Descripción", "Estado");
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
            for (var role : response.getRoles()) {
                PdfTableUtil.addRow(
                        table,
                        Arrays.asList(
                                String.valueOf(index++),
                                role.getName(),
                                role.getDescription() != null ? role.getDescription() : "-",
                                role.getActive() ? "Habilitado" : "Inhabilitado"
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
    public byte[] generateExcelReport(RoleViewRequest request) {
        RoleViewResponse response = roleUseCase.init(request);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // ---- Única Hoja ----
            Sheet sheet = workbook.createSheet("Reporte de Roles");
            int rowIdx = 0;

            // ---- Encabezado (utilidad) ----
            String currentUserUsername = SecurityUtil.getCurrentUsername();
            ExcelReportHeaderUtil.createCoverPage(workbook, sheet, "Reporte de Roles", currentUserUsername, 3);
            rowIdx = sheet.getLastRowNum() + 2;

            CellStyle bodyStyle = workbook.createCellStyle();
            bodyStyle.setWrapText(true);

            // ---- Filtros aplicados ----
            Map<String, String> filters = buildFilters(request);
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
                    keyCell.setCellStyle(boldStyle);

                    filterRow.createCell(1).setCellValue(entry.getValue());
                }
                rowIdx++;
            }

            String[] columns = {"Nº", "Nombre", "Descripción", "Estado"};
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
                    HorizontalAlignment.LEFT,     // Nombre
                    HorizontalAlignment.LEFT,     // Descripción
                    HorizontalAlignment.CENTER    // Estado
            };

            // Escribir filas dinámicamente
            int index = 1;
            for (var role : response.getRoles()) {
                Object[] rowValues = {
                        index++,
                        role.getName(),
                        role.getDescription() != null ? role.getDescription() : "-",
                        role.getActive() ? "Habilitado" : "Inhabilitado"
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

    // 📌 Método privado para construir filtros (evita duplicación)
    private Map<String, String> buildFilters(RoleViewRequest request) {
        Map<String, String> filters = new LinkedHashMap<>();
        if (request.getName() != null && !request.getName().isBlank()) {
            filters.put("Nombre", request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            filters.put("Descripción", request.getDescription());
        }
        if (request.getStatus() != null) {
            filters.put("Estado", request.getStatus() ? "Habilitado" : "Inhabilitado");
        }
        return filters;
    }
}
