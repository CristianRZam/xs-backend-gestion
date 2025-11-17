package com.sistema.sistema.application.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.domain.usecase.ParameterUseCase;
import com.sistema.sistema.domain.usecase.ProductUseCase;
import com.sistema.sistema.infrastructure.report.*;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductReportService {

    private final ProductUseCase productUseCase;
    private final ParameterUseCase parameterUseCase;

    public ProductReportService(ProductUseCase productUseCase, ParameterUseCase parameterUseCase) {
        this.productUseCase = productUseCase;
        this.parameterUseCase = parameterUseCase;
    }

    private Map<String, String> buildFilters(ProductViewRequest request) {
        Map<String, String> filters = new LinkedHashMap<>();

        if (request.getCode() != null && !request.getCode().isBlank()) {
            filters.put("Código", request.getCode());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            filters.put("Nombre", request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            filters.put("Descripción", request.getDescription());
        }

        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            List<ParameterDto> categories = parameterUseCase.getListParameterByCode("CATEGORIA_PRODUCTO");

            String nombres = categories.stream()
                    .filter(p -> request.getCategories().contains(p.getParameterId().intValue()))
                    .map(ParameterDto::getName)
                    .collect(Collectors.joining(", "));

            String key = request.getCategories().size() > 1 ? "Categorías" : "Categoría";
            filters.put(key, nombres);
        }

        if (request.getUnitMeasures() != null && !request.getUnitMeasures().isEmpty()) {
            List<ParameterDto> unitMeasures = parameterUseCase.getListParameterByCode("UNIDAD_MEDIDA_PRODUCTO");

            String nombres = unitMeasures.stream()
                    .filter(p -> request.getUnitMeasures().contains(p.getParameterId().intValue()))
                    .map(ParameterDto::getName)
                    .collect(Collectors.joining(", "));

            String key = request.getUnitMeasures().size() > 1 ? "Unidades de medida" : "Unidad de medida";
            filters.put(key, nombres);
        }

        if (request.getValuationMethods() != null && !request.getValuationMethods().isEmpty()) {
            List<ParameterDto> valuationMethods = parameterUseCase.getListParameterByCode("UNIDAD_MEDIDA_PRODUCTO");

            String nombres = valuationMethods.stream()
                    .filter(p -> request.getUnitMeasures().contains(p.getParameterId().intValue()))
                    .map(ParameterDto::getName)
                    .collect(Collectors.joining(", "));

            String key = request.getUnitMeasures().size() > 1 ? "Métodos de valuación" : "Método de valuaión";
            filters.put(key, nombres);
        }

        if (request.getManageVariant() != null) {
            String valor = request.getManageVariant() ? "Con variante" : "Sin variante";
            filters.put("Variante", valor);
        }

        if (request.getMinimumStock() != null) {
            filters.put("Stock mínimo", String.valueOf(request.getMinimumStock()));
        }

        if (request.getMaximumStock() != null) {
            filters.put("Stock máximo", String.valueOf(request.getMaximumStock()));
        }

        if (request.getStatus() != null) {
            filters.put("Estado", request.getStatus() ? "Habilitado" : "Inhabilitado");
        }

        return filters;
    }

    public byte[] generatePdfReport(ProductViewRequest request) {
        List<ProductDTO> products = productUseCase.findAllFiltered(request);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Map<String, String> filters = buildFilters(request);
            boolean hasFilters = !filters.isEmpty();

            int baseMarginTop = 90;
            int filterRows = (int) Math.ceil((double) filters.size() / 2);
            int extraMargin = filterRows * 15;
            int marginTop = baseMarginTop + extraMargin;

            Document document = new Document(PageSize.A4, 36, 36, marginTop, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Header
            String username = SecurityUtil.getCurrentUsername();
            writer.setPageEvent(new PdfReportUtil("Reporte de productos", username));
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
            List<String> columnTitles = Arrays.asList(
                    "Nº", "Código", "Nombre", "Categoría", "U. Medida", "Variante", "Precio", "Stock", "Estado"
            );
            float[] columnWidths = {0.6f, 2.0f, 3.5f, 2.2f, 1.8f, 1.4f, 1.4f, 1.2f, 1.4f};

            PdfPTable table = PdfTableUtil.createHeader(columnTitles, columnWidths);
            if (!hasFilters) {
                table.setSpacingBefore(10f);
            }

            com.lowagie.text.Font bodyFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);

            List<Integer> alignments = Arrays.asList(
                    Element.ALIGN_CENTER, // Nº
                    Element.ALIGN_LEFT,   // Código
                    Element.ALIGN_LEFT,   // Nombre
                    Element.ALIGN_LEFT,   // Categoría
                    Element.ALIGN_LEFT,   // U. Medida
                    Element.ALIGN_CENTER, // Variante
                    Element.ALIGN_RIGHT,  // Precio
                    Element.ALIGN_CENTER, // Stock
                    Element.ALIGN_CENTER  // Estado
            );

            int index = 1;
            for (ProductDTO p : products) {
                PdfTableUtil.addRow(
                        table,
                        Arrays.asList(
                                String.valueOf(index++),
                                p.getCode() != null ? p.getCode() : "-",
                                p.getName() != null ? p.getName() : "-",
                                p.getNameCategory() != null ? p.getNameCategory() : "-",
                                p.getNameUnitMeasure() != null ? p.getNameUnitMeasure() : "-",
                                p.getManageVariants() != null && p.getManageVariants() ? "Sí" : "No",
                                p.getBasePrice() != null ? String.format("%.2f", p.getBasePrice()) : "-",
                                p.getTotalStock() != null ? String.valueOf(p.getTotalStock()) : "0",
                                p.getActive() != null && p.getActive() ? "Habilitado" : "Inhabilitado"
                        ),
                        bodyFont,
                        alignments
                );
            }

            document.add(table);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando el reporte PDF de productos", e);
        }
    }


    public byte[] generateExcelReport(ProductViewRequest request) {
        List<ProductDTO> products = productUseCase.findAllFiltered(request);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // ---- Hoja principal ----
            Sheet sheet = workbook.createSheet("Reporte de Productos");
            int rowIdx = 0;

            // ---- Encabezado general ----
            String username = SecurityUtil.getCurrentUsername();
            ExcelReportHeaderUtil.createCoverPage(workbook, sheet, "Reporte de Productos", username, 9);
            rowIdx = sheet.getLastRowNum() + 2;

            // ---- Filtros ----
            Map<String, String> filters = buildFilters(request);
            if (!filters.isEmpty()) {
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

            // ---- Cabecera de la tabla ----
            String[] columns = {
                    "Nº", "Código", "Nombre", "Categoría", "U. Medida", "Variante", "Precio", "Stock", "Estado"
            };

            HorizontalAlignment[] alignments = {
                    HorizontalAlignment.CENTER, // Nº
                    HorizontalAlignment.LEFT,   // Código
                    HorizontalAlignment.LEFT,   // Nombre
                    HorizontalAlignment.LEFT,   // Categoría
                    HorizontalAlignment.LEFT,   // U. Medida
                    HorizontalAlignment.CENTER, // Variante
                    HorizontalAlignment.RIGHT,  // Precio
                    HorizontalAlignment.CENTER, // Stock
                    HorizontalAlignment.CENTER  // Estado
            };

            rowIdx = ExcelReportTableUtil.createTableHeader(workbook, sheet, rowIdx, columns, alignments, (short) 11);

            // ---- Datos ----
            int index = 1;
            for (ProductDTO p : products) {
                Object[] rowValues = {
                        index++,
                        p.getCode() != null ? p.getCode() : "-",
                        p.getName() != null ? p.getName() : "-",
                        p.getNameCategory() != null ? p.getNameCategory() : "-",
                        p.getNameUnitMeasure() != null ? p.getNameUnitMeasure() : "-",
                        p.getManageVariants() != null && p.getManageVariants() ? "Sí" : "No",
                        p.getBasePrice() != null ? String.format("%.2f", p.getBasePrice()) : "-",
                        p.getTotalStock() != null ? String.valueOf(p.getTotalStock()) : "0",
                        p.getActive() != null && p.getActive() ? "Habilitado" : "Inhabilitado"
                };

                rowIdx = ExcelReportDataUtil.writeDataRow(
                        workbook, sheet, rowIdx,
                        rowValues, alignments, (short) 10
                );
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando el reporte Excel de productos", e);
        }
    }

}
