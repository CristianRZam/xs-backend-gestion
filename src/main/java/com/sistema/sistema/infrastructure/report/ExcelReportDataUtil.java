package com.sistema.sistema.infrastructure.report;

import org.apache.poi.ss.usermodel.*;

public class ExcelReportDataUtil {

    private ExcelReportDataUtil() {}

    /**
     * 📌 Escribe una fila de datos con configuración por columna
     *
     * @param workbook   Libro Excel
     * @param sheet      Hoja de Excel
     * @param rowIdx     Índice de la fila
     * @param values     Valores de las celdas
     * @param alignments Alineación por columna (mismo tamaño que values)
     * @param fontSize   Tamaño de la fuente
     * @return índice de la siguiente fila libre (rowIdx + 1)
     */
    public static int writeDataRow(Workbook workbook, Sheet sheet, int rowIdx,
                                   Object[] values, HorizontalAlignment[] alignments,
                                   short fontSize) {

        if (alignments.length != values.length) {
            throw new IllegalArgumentException("El tamaño de alignments debe ser igual al de values");
        }

        Row row = sheet.createRow(rowIdx++);

        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);

            // estilo dinámico para cada celda
            CellStyle dataStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontHeightInPoints(fontSize);
            dataStyle.setFont(font);
            dataStyle.setAlignment(alignments[i]);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // valores dinámicos
            Object value = values[i];
            if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
            } else if (value instanceof Boolean bool) {
                cell.setCellValue(bool ? "Sí" : "No");
            } else {
                cell.setCellValue(value != null ? value.toString() : "-");
            }

            cell.setCellStyle(dataStyle);
        }

        return rowIdx;
    }

    /**
     * 📌 Escribe múltiples filas de datos
     */
    public static int writeDataRows(Workbook workbook, Sheet sheet, int rowIdx,
                                    Object[][] data, HorizontalAlignment[] alignments,
                                    short fontSize) {
        for (Object[] rowValues : data) {
            rowIdx = writeDataRow(workbook, sheet, rowIdx, rowValues, alignments, fontSize);
        }
        return rowIdx;
    }
}
