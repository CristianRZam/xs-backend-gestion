package com.sistema.sistema.infrastructure.report;

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Element;

import java.awt.Color;

public class PdfCellUtil {

    public static PdfPCell createCell(String text, Font font, int hAlign, int vAlign, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(hAlign);
        cell.setVerticalAlignment(vAlign);
        cell.setPadding(5f);

        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    // Atajo para header (centrado y con fondo)
    public static PdfPCell createHeaderCell(String text, Font font, Color bgColor) {
        return createCell(text, font, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, bgColor);
    }

    // Atajo para body (alineación libre, sin fondo)
    public static PdfPCell createBodyCell(String text, Font font, int hAlign) {
        return createCell(text, font, hAlign, Element.ALIGN_MIDDLE, null);
    }
}
