package com.sistema.sistema.infrastructure.report;

import com.sistema.sistema.infrastructure.style.AppColors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.awt.Color;

public class ExcelReportTableUtil {

    private ExcelReportTableUtil() {
    }

    /**
     * 📌 Crea una cabecera de tabla reutilizable con configuración por columna
     *
     * @param workbook    Libro Excel (debe ser XSSFWorkbook para usar colores RGB)
     * @param sheet       Hoja de Excel
     * @param rowIdx      Índice de la fila inicial
     * @param columns     Array con los títulos de las columnas
     * @param alignments  Array con la alineación de cada columna
     *                    (debe tener el mismo tamaño que columns)
     * @param fontSize    Tamaño de la letra (ej: 11)
     * @return índice de la siguiente fila libre (rowIdx + 1)
     */
    public static int createTableHeader(Workbook workbook, Sheet sheet, int rowIdx,
                                        String[] columns, HorizontalAlignment[] alignments,
                                        short fontSize) {

        if (alignments.length != columns.length) {
            throw new IllegalArgumentException("El tamaño de alignments debe ser igual al de columns");
        }

        // Convertir AppColors.PRIMARY a XSSFColor
        Color awtColor = AppColors.PRIMARY;
        XSSFColor primaryColor = new XSSFColor(
                new byte[]{(byte) awtColor.getRed(), (byte) awtColor.getGreen(), (byte) awtColor.getBlue()},
                null
        );

        Row headerRow = sheet.createRow(rowIdx++);

        for (int i = 0; i < columns.length; i++) {
            CellStyle headerStyle = workbook.createCellStyle();

            // aplicar color solo si es un XSSFWorkbook
            if (workbook instanceof XSSFWorkbook xssfWorkbook) {
                ((XSSFCellStyle) headerStyle).setFillForegroundColor(primaryColor);
            }

            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints(fontSize);
            headerStyle.setFont(headerFont);

            headerStyle.setAlignment(alignments[i]);

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        return rowIdx;
    }
}
