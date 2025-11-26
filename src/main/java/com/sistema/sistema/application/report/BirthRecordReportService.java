package com.sistema.sistema.application.report;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordDetailDTO;
import com.sistema.sistema.domain.repository.BirthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BirthRecordReportService {

    private final BirthRecordRepository repository;

    public byte[] generateCertificate(Long id) {

        BirthRecordDetailDTO data = repository.findDetail(id)
                .orElseThrow(() -> new RuntimeException("No existe certificado con ID " + id));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // 🔥 AGREGA MARCA DE AGUA
            writer.setPageEvent(new WatermarkEvent("REFERENCIAL"));

            document.open();

            // ===== FUENTES =====
            BaseFont handwrittenFont = BaseFont.createFont(
                    "C:/Windows/Fonts/segoesc.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font scriptFont = new Font(handwrittenFont, 11);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font yearFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font actNumberFont = new Font(Font.HELVETICA, 10, Font.BOLD);

            // ===== ESCUDO =====
            Image escudo = Image.getInstance(Objects.requireNonNull(getClass().getResource("/static/img/escudo.png")));
            escudo.scaleToFit(35, 35);
            escudo.setAlignment(Element.ALIGN_CENTER);
            document.add(escudo);

            // ===== ENCABEZADO =====
            Paragraph gov = new Paragraph("REPÚBLICA DEL PERÚ", headerFont);
            gov.setAlignment(Element.ALIGN_CENTER);
            gov.setSpacingAfter(8f);
            document.add(gov);

            // ================= UBICACIÓN =================
            PdfPTable locationTable = new PdfPTable(6);
            locationTable.setWidthPercentage(100);
            locationTable.setSpacingAfter(10f);
            locationTable.setWidths(new float[]{2.5f, 4f, 2.8f, 4f, 4f, 6f});

            addDotted(locationTable, "DISTRITO DE", data.getDistrictName(), headerFont, scriptFont);
            addDotted(locationTable, "PROVINCIA DE", data.getDistrictProvince(), headerFont, scriptFont);
            addDotted(locationTable, "DEPARTAMENTO DE", data.getDistrictDepartment(), headerFont, scriptFont);
            document.add(locationTable);

            // ===== OFICINA =====
            PdfPTable civilOfficeTable = new PdfPTable(2);
            civilOfficeTable.setWidthPercentage(100);
            civilOfficeTable.setWidths(new float[]{6f, 8f});
            civilOfficeTable.setSpacingAfter(8f);

            PdfPCell civilOfficeLabel = new PdfPCell(new Phrase("OFICINA DEL REGISTRO DEL ESTADO CIVIL DE", headerFont));
            civilOfficeLabel.setBorder(Rectangle.NO_BORDER);
            civilOfficeLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            civilOfficeTable.addCell(civilOfficeLabel);

            PdfPCell civilOfficeValue = new PdfPCell(new Phrase(data.getDistrictName(), scriptFont));
            civilOfficeValue.setBorder(Rectangle.NO_BORDER);
            civilOfficeValue.setCellEvent(new DottedCellBottomBorder());
            civilOfficeValue.setPaddingBottom(6);
            civilOfficeTable.addCell(civilOfficeValue);

            document.add(civilOfficeTable);

            // ================== AÑO CENTRADO ==================
            PdfPTable yearRow = new PdfPTable(1);
            yearRow.setWidthPercentage(100);

            PdfPCell yearCell = new PdfPCell(new Phrase(String.valueOf(data.getYear()), yearFont));
            yearCell.setBorder(Rectangle.NO_BORDER);
            yearCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            yearCell.setPaddingBottom(6f);
            yearRow.addCell(yearCell);

            document.add(yearRow);

            // ================== ACTA + TÍTULO ==================
            String paddedActNumber;
            try {
                paddedActNumber = String.format("%05d", Integer.parseInt(data.getActNumber()));
            } catch (Exception e) {
                paddedActNumber = "00000";
            }

            PdfPTable titleActTable = new PdfPTable(3);
            titleActTable.setWidthPercentage(100);
            titleActTable.setWidths(new float[]{35f, 30f, 35f});
            titleActTable.setSpacingAfter(6f);

            titleActTable.addCell(noBorderCell());

            PdfPCell titleCell = new PdfPCell(new Phrase("ACTA DE NACIMIENTO", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleActTable.addCell(titleCell);

            PdfPTable actBoxTable = new PdfPTable(paddedActNumber.length());
            actBoxTable.setWidthPercentage(100);

            for (char c : paddedActNumber.toCharArray()) {
                PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(c), actNumberFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setFixedHeight(16f);
                cell.setBorder(Rectangle.BOX);
                actBoxTable.addCell(cell);
            }

            PdfPCell actOuterCell = new PdfPCell(actBoxTable);
            actOuterCell.setBorder(Rectangle.NO_BORDER);
            actOuterCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            actOuterCell.setPaddingRight(10f);
            titleActTable.addCell(actOuterCell);

            document.add(titleActTable);

            // ================= DATOS PRINCIPALES =================
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[]{2f, 4f, 2f, 4f});

            addCellLine(table, "De:", labelFont, true);
            addCellLine(table, data.getPersonName(), scriptFont, false);

            addCellLine(table, "Fecha de nacimiento:", labelFont, true);
            addCellLine(table, data.getBirthDatetime().toLocalDate().toString(), scriptFont, false);

            addCellLine(table, "Lugar de nacimiento:", labelFont, true);
            addCellLine(table, data.getBirthPlace(), scriptFont, false);

            addCellLine(table, "Sexo:", labelFont, true);
            addCellLine(table, data.getSex(), scriptFont, false);

            document.add(table);

            // ================= PADRES =================
            PdfPTable parentsTable = new PdfPTable(2);
            parentsTable.setWidthPercentage(100);
            parentsTable.setSpacingBefore(10);

            addSection(parentsTable, "DATOS DEL PADRE", labelFont);
            addSection(parentsTable, "DATOS DE LA MADRE", labelFont);

            addCellLine(parentsTable, data.getFatherName(), scriptFont, false);
            addCellLine(parentsTable, data.getMotherName(), scriptFont, false);

            addCellLine(parentsTable, data.getFatherNationality(), scriptFont, false);
            addCellLine(parentsTable, data.getMotherNationality(), scriptFont, false);

            addCellLine(parentsTable, data.getFatherOccupation(), scriptFont, false);
            addCellLine(parentsTable, data.getMotherOccupation(), scriptFont, false);

            document.add(parentsTable);

            // ================= FIRMA =================
            PdfPTable signatureTable = new PdfPTable(3);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(25);
            signatureTable.setWidths(new float[]{3f, 3f, 3f});

            signatureTable.addCell(createSignatureBlock("REGISTRADOR"));
            signatureTable.addCell(createSignatureBlock("PADRE"));
            signatureTable.addCell(createSignatureBlock("MADRE"));

            document.add(signatureTable);

            // ================= SELLOS =================
            PdfPTable sealTable = new PdfPTable(2);
            sealTable.setWidthPercentage(100);
            sealTable.setSpacingBefore(25);
            sealTable.setWidths(new float[]{5f, 5f});

            sealTable.addCell(createStampSpace("Sello de la Oficina Registral"));
            sealTable.addCell(createStampSpace("Sello Legal"));

            document.add(sealTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando certificado", e);
        }
    }

    // ==========================================================
    // 🔧 HELPERS
    // ==========================================================

    private PdfPCell noBorderCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private void addCellLine(PdfPTable table, String text, Font font, boolean label) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidthBottom(label ? 0.6f : 1.1f);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthTop(0);
        table.addCell(cell);
    }

    private void addSection(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.93f);
        table.addCell(cell);
    }

    private void addDotted(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell l = new PdfPCell(new Phrase(label, fontLabel));
        l.setBorder(Rectangle.NO_BORDER);
        l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(value, fontValue));
        v.setBorder(Rectangle.NO_BORDER);
        v.setCellEvent(new DottedCellBottomBorder());
        v.setPaddingBottom(4);
        v.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(v);
    }

    private PdfPCell createSignatureBlock(String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(60);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);

        cell.setCellEvent((PdfPCellEvent) (c, rect, canvas) -> {
            PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
            cb.moveTo(rect.getLeft() + 10, rect.getTop() - 10);
            cb.lineTo(rect.getRight() - 10, rect.getTop() - 10);
            cb.stroke();
        });

        cell.setPhrase(new Phrase(label, new Font(Font.HELVETICA, 10, Font.BOLD)));
        cell.setPaddingTop(40);
        return cell;
    }

    private PdfPCell createStampSpace(String label) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(85);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Phrase(label));
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private static class DottedCellBottomBorder implements PdfPCellEvent {
        @Override
        public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
            PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
            cb.setLineDash(2f, 2f);
            cb.moveTo(rect.getLeft(), rect.getBottom());
            cb.lineTo(rect.getRight(), rect.getBottom());
            cb.stroke();
            cb.setLineDash(1);
        }
    }

    // 🔥================ MARCA DE AGUA =================🔥
    public static class WatermarkEvent extends PdfPageEventHelper {

        private final String watermarkText;

        public WatermarkEvent(String watermarkText) {
            this.watermarkText = watermarkText;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            Font font = new Font(Font.HELVETICA, 72, Font.BOLD, new Color(200, 200, 200, 50));

            Phrase phrase = new Phrase(watermarkText, font);

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    phrase,
                    (document.getPageSize().getWidth() / 2),
                    (document.getPageSize().getHeight() / 2),
                    45  // Rotación diagonal
            );
        }
    }
}
