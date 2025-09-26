package com.sistema.sistema.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.application.dto.response.user.UserViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.usecase.ParameterUseCase;
import com.sistema.sistema.domain.usecase.UserUseCase;
import com.sistema.sistema.infrastructure.report.*;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserReportService {

    private final UserUseCase userUseCase;
    private final ParameterUseCase parameterUseCase;

    public UserReportService(UserUseCase userUseCase, ParameterUseCase parameterUseCase) {
        this.userUseCase = userUseCase;
        this.parameterUseCase = parameterUseCase;
    }

    // 📌 Construcción de filtros (para PDF y Excel)
    private Map<String, String> buildFilters(UserViewRequest request) {
        Map<String, String> filters = new LinkedHashMap<>();

        if (request.getTypeDocuments() != null && !request.getTypeDocuments().isEmpty()) {
            List<Parameter> typeDocuments = parameterUseCase.getListParameterByCode("TIPO_DOCUMENTO");

            String nombres = typeDocuments.stream()
                    .filter(p -> request.getTypeDocuments().contains(p.getParameterId().intValue()))
                    .map(Parameter::getName)
                    .collect(Collectors.joining(", "));

            filters.put("Tipos de documento", nombres);
        }

        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            filters.put("Nº documento", request.getDocument());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            filters.put("Nombre", request.getFullName());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            filters.put("Usuario", request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            filters.put("Correo electrónico", request.getEmail());
        }

        if (request.getStatus() != null) {
            filters.put("Estado", request.getStatus() ? "Habilitado" : "Inhabilitado");
        }

        return filters;
    }

    // 📌 Generación de PDF
    public byte[] generatePdfReport(UserViewRequest request) {
        UserViewResponse response = userUseCase.init(request);
        Map<String, String> filters = buildFilters(request);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Calcular margen superior dinámico
            int baseMarginTop = 90;
            int filterRows = (int) Math.ceil((double) filters.size() / 2);
            int marginTop = baseMarginTop + (filterRows * 15);

            Document document = new Document(PageSize.A4, 36, 36, marginTop, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            String currentUserUsername = SecurityUtil.getCurrentUsername();
            writer.setPageEvent(new PdfReportUtil("Reporte de usuarios", currentUserUsername));

            document.open();

            // ---- Renderizamos filtros ----
            if (!filters.isEmpty()) {
                var filterTitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
                var filterNameFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

                Paragraph filterTitle = new Paragraph("Filtros aplicados:", filterTitleFont);
                filterTitle.setSpacingAfter(5f);
                document.add(filterTitle);

                PdfPTable filtersTable = PdfTableUtil.createFiltersTable(filters,  filterNameFont);
                document.add(filtersTable);
            }

            // ---- Cabecera de la tabla ----
            List<String> columnTitles = Arrays.asList("Nº", "Tipo Documento", "Nº Documento", "Nombres", "Usuario", "Correo Electrónico", "Estado");
            float[] columnWidths = {0.5f, 1.5f, 1.5f, 2f, 1.5f, 2f, 1f};

            PdfPTable table = PdfTableUtil.createHeader(columnTitles, columnWidths);
            if (filters.isEmpty()) {
                table.setSpacingBefore(10f);
            }

            var bodyFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);
            List<Integer> alignments = Arrays.asList(
                    Element.ALIGN_CENTER, // Nº
                    Element.ALIGN_CENTER, // Tipo doc
                    Element.ALIGN_LEFT,   // Nº doc
                    Element.ALIGN_LEFT,   // Nombre
                    Element.ALIGN_LEFT,   // Usuario
                    Element.ALIGN_LEFT,   // Email
                    Element.ALIGN_CENTER  // Estado
            );

            int index = 1;
            for (var user : response.getUsers()) {
                PdfTableUtil.addRow(
                        table,
                        Arrays.asList(
                                String.valueOf(index++),
                                user.getPerson().getTypeDocumentName(),
                                user.getPerson().getDocument(),
                                user.getPerson().getFullName(),
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
    public byte[] generateExcelReport(UserViewRequest request) {
        UserViewResponse response = userUseCase.init(request);
        Map<String, String> filters = buildFilters(request);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Usuarios");
            int rowIdx = 0;

            // ---- Encabezado ----
            String currentUserUsername = SecurityUtil.getCurrentUsername();
            ExcelReportHeaderUtil.createCoverPage(workbook, sheet, "Reporte de Usuarios", currentUserUsername, 6);
            rowIdx = sheet.getLastRowNum() + 2;

            // ---- Filtros aplicados ----
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
                    keyCell.setCellStyle(boldStyle); // <-- nombre del filtro en negrita
                    filterRow.createCell(1).setCellValue(entry.getValue());
                }
                rowIdx++;
            }

            // ---- Cabecera de la tabla ----
            String[] columns = {"Nº", "Tipo Documento", "Nº Documento", "Nombres", "Usuario", "Correo Electrónico", "Estado"};
            HorizontalAlignment[] alignments = {
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.CENTER
            };

            rowIdx = ExcelReportTableUtil.createTableHeader(workbook, sheet, rowIdx, columns, alignments, (short) 11);

            // ---- Datos ----
            HorizontalAlignment[] dataAlignments = {
                    HorizontalAlignment.CENTER,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.LEFT,
                    HorizontalAlignment.CENTER
            };

            int index = 1;
            for (var user : response.getUsers()) {
                Object[] rowValues = {
                        index++,
                        user.getPerson().getTypeDocumentName(),
                        user.getPerson().getDocument(),
                        user.getPerson().getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getActive() ? "Habilitado" : "Inhabilitado"
                };

                rowIdx = ExcelReportDataUtil.writeDataRow(workbook, sheet, rowIdx, rowValues, dataAlignments, (short) 10);
            }

            // Ajustar ancho
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
