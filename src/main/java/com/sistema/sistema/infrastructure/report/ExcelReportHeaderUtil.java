package com.sistema.sistema.infrastructure.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelReportHeaderUtil {

    private ExcelReportHeaderUtil() {
    }

    /**
     * 📌 Crea una portada/encabezado para reportes Excel
     *
     * @param workbook   Libro Excel
     * @param sheet      Hoja de Excel
     * @param title      Título del reporte
     * @param username   Usuario que genera el reporte
     * @param endCol     Columna final a fusionar (ej: 3 = columnas A-D)
     */
    public static void createCoverPage(Workbook workbook, Sheet sheet,
                                       String title, String username, int endCol) {
        int rowIdx = 0;

        String imageUrl = "https://www.gstatic.com/webp/gallery/1.jpg";
        try (InputStream is = new URL(imageUrl).openStream()) {
            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper helper = workbook.getCreationHelper();

            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(1);
            anchor.setRow2(2);

            drawing.createPicture(anchor, pictureIdx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---- Estilo del título ----
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Fila 1 y 2 → título (fusionado en columnas B..endCol)
        Row titleRow = sheet.createRow(rowIdx++);
        Row titleRow2 = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(1);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(
                0, 1, 1, endCol
        ));

        // ---- Estilo de etiquetas ----
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

        // ---- Fecha ----
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Obtener fecha/hora Perú
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima"));

        Row dateRow = sheet.createRow(rowIdx++);
        Cell dateLabel = dateRow.createCell(0);
        dateLabel.setCellValue("Fecha:");
        dateLabel.setCellStyle(boldStyle);
        dateRow.createCell(1).setCellValue(now.format(dateFormatter));

        // ---- Hora ----
        Row hourRow = sheet.createRow(rowIdx++);
        Cell hourLabel = hourRow.createCell(0);
        hourLabel.setCellValue("Hora:");
        hourLabel.setCellStyle(boldStyle);
        hourRow.createCell(1).setCellValue(now.format(timeFormatter));

        // ---- Usuario ----
        Row userRow = sheet.createRow(rowIdx++);
        Cell userLabel = userRow.createCell(0);
        userLabel.setCellValue("Usuario:");
        userLabel.setCellStyle(boldStyle);
        userRow.createCell(1).setCellValue(username);

        // Ajustar ancho columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
}
