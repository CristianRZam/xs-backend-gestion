package com.sistema.sistema.infrastructure.report;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.util.Map;

public class PdfTableUtil {

    // 📌 Cabecera con configuración por defecto
    public static PdfPTable createHeader(java.util.List<String> columnTitles, float[] columnWidths) {
        return createHeader(columnTitles, columnWidths, com.sistema.sistema.infrastructure.style.AppColors.PRIMARY, java.awt.Color.WHITE, 10);
    }

    // 📌 Cabecera configurable
    public static PdfPTable createHeader(java.util.List<String> columnTitles, float[] columnWidths,
                                         java.awt.Color headerBackgroundColor, java.awt.Color fontColor, int fontSize) {
        PdfPTable table = new PdfPTable(columnTitles.size());
        table.setWidthPercentage(100);

        if (columnWidths != null && columnWidths.length == columnTitles.size()) {
            try {
                table.setWidths(columnWidths);
            } catch (Exception e) {
                throw new RuntimeException("Error configurando anchos de columnas", e);
            }
        }

        Font headerFont = new Font(Font.HELVETICA, fontSize, Font.BOLD, fontColor);

        for (String title : columnTitles) {
            table.addCell(PdfCellUtil.createHeaderCell(title, headerFont, headerBackgroundColor));
        }

        return table;
    }

    // 📌 Agregar fila al body
    public static void addRow(PdfPTable table, java.util.List<String> values, Font font, java.util.List<Integer> alignments) {
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            int alignment = (alignments != null && i < alignments.size()) ? alignments.get(i) : Element.ALIGN_LEFT;
            table.addCell(PdfCellUtil.createBodyCell(value != null ? value : "-", font, alignment));
        }
    }

    // 📌 Tabla de filtros dinámica
    public static PdfPTable createFiltersTable(Map<String, String> filters, Font filterFont) {
        PdfPTable filtersTable = new PdfPTable(2);
        filtersTable.setWidthPercentage(100);

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            PdfPCell filterCell = new PdfPCell(new Phrase(entry.getKey() + ": " + entry.getValue(), filterFont));
            filterCell.setBorder(Rectangle.NO_BORDER);
            filterCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            filtersTable.addCell(filterCell);
        }

        // Completar fila si queda impar
        if (filters.size() % 2 != 0) {
            PdfPCell emptyCell = new PdfPCell(new Phrase(" "));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            filtersTable.addCell(emptyCell);
        }

        filtersTable.setSpacingAfter(10f);
        return filtersTable;
    }
}
