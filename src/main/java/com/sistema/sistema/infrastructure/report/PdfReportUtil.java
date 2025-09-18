package com.sistema.sistema.infrastructure.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class PdfReportUtil extends PdfPageEventHelper {

    private final String title;
    private final String username;

    public PdfReportUtil(String title, String username) {
        this.title = title;
        this.username = username;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            float startX = document.left();
            float startY = document.top() + 50; // 📌 menos alto porque no hay filtros

            // ---- Título centrado (fila única) ----
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setTotalWidth(document.right() - document.left());
            titleTable.setLockedWidth(true);
            titleTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell titleCell = new PdfPCell(new Phrase(title, new Font(Font.HELVETICA, 14, Font.BOLD)));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleTable.addCell(titleCell);

            float currentY = startY;
            titleTable.writeSelectedRows(0, -1, startX, currentY, cb);
            currentY -= titleTable.getTotalHeight() + 8;

            // ---- Usuario a la izquierda y Fecha a la derecha en la misma fila ----
            String dateTime = ZonedDateTime.now(ZoneId.of("America/Lima"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidths(new int[]{6, 2}); // más espacio a la izquierda
            infoTable.setTotalWidth(document.right() - document.left());
            infoTable.setLockedWidth(true);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Font infoFont = new Font(Font.HELVETICA, 9, Font.ITALIC);

            PdfPCell userCell = new PdfPCell(new Phrase("Generado por: " + username, infoFont));
            userCell.setBorder(Rectangle.NO_BORDER);
            userCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell dateCell = new PdfPCell(new Phrase(dateTime, infoFont));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            infoTable.addCell(userCell);
            infoTable.addCell(dateCell);

            infoTable.writeSelectedRows(0, -1, startX, currentY, cb);

        } catch (DocumentException e) {
            throw new ExceptionConverter(e);
        }
    }
}
