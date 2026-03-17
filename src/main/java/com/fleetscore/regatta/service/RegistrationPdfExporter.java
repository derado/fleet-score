package com.fleetscore.regatta.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fleetscore.regatta.api.dto.RegattaResponse;
import com.fleetscore.regatta.api.dto.RegistrationResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.Table.TableBuilder;
import org.vandeseer.easytable.structure.cell.TextCell;

import static org.vandeseer.easytable.settings.HorizontalAlignment.CENTER;
import static org.vandeseer.easytable.settings.HorizontalAlignment.LEFT;
import static org.vandeseer.easytable.settings.HorizontalAlignment.RIGHT;

public class RegistrationPdfExporter {

    private static final String FONT_REGULAR_PATH = "/fonts/LiberationSans-Regular.ttf";
    private static final String FONT_BOLD_PATH = "/fonts/LiberationSans-Bold.ttf";

    private static final Color HEADER_BG = new Color(41, 65, 122);
    private static final Color HEADER_TEXT = Color.WHITE;
    private static final Color ROW_EVEN = new Color(245, 245, 245);
    private static final Color ROW_ODD = Color.WHITE;
    private static final Color GROUP_HEADER_BG = new Color(220, 230, 241);

    private static final float PAGE_MARGIN = 40f;
    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - 2 * PAGE_MARGIN;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] exportRegistrations(RegattaResponse regatta,
                                      List<RegistrationResponse> registrations,
                                      boolean groupBySailingClass) {
        try (PDDocument document = new PDDocument()) {
            PDFont fontRegular = loadFont(document, FONT_REGULAR_PATH);
            PDFont fontBold = loadFont(document, FONT_BOLD_PATH);

            PDPage firstPage = new PDPage(PDRectangle.A4);
            document.addPage(firstPage);

            float cursorY = drawHeader(document, firstPage, regatta, fontRegular, fontBold);

            if (groupBySailingClass) {
                Map<String, List<RegistrationResponse>> grouped = registrations.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.sailingClass().name(),
                                LinkedHashMap::new,
                                Collectors.toList()));

                for (Map.Entry<String, List<RegistrationResponse>> entry : grouped.entrySet()) {
                    cursorY = drawGroupHeader(document, cursorY, entry.getKey(), fontBold);
                    cursorY = drawRegistrationTable(document, cursorY, entry.getValue(), fontRegular, fontBold);
                    cursorY -= 15f;
                }
            } else {
                cursorY = drawRegistrationTable(document, cursorY, registrations, fontRegular, fontBold);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate registration PDF", e);
        }
    }

    private PDFont loadFont(PDDocument document, String classpathPath) throws IOException {
        try (var fontStream = getClass().getResourceAsStream(classpathPath)) {
            if (fontStream == null) {
                throw new IOException("Font not found on classpath: " + classpathPath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }

    private float drawHeader(PDDocument document, PDPage page, RegattaResponse regatta,
                              PDFont fontRegular, PDFont fontBold) throws IOException {
        float y = page.getMediaBox().getHeight() - PAGE_MARGIN;

        try (PDPageContentStream cs = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            cs.beginText();
            cs.setFont(fontBold, 16);
            cs.newLineAtOffset(PAGE_MARGIN, y);
            cs.showText(regatta.name());
            cs.endText();
            y -= 18;

            cs.beginText();
            cs.setFont(fontRegular, 10);
            cs.newLineAtOffset(PAGE_MARGIN, y);
            String dateRange = regatta.startDate().format(DATE_FMT) + " - " + regatta.endDate().format(DATE_FMT);
            cs.showText(regatta.venue() + "  |  " + dateRange);
            cs.endText();
            y -= 20;
        }

        return y;
    }

    private float drawGroupHeader(PDDocument document, float cursorY, String className,
                                   PDFont fontBold) throws IOException {
        if (cursorY < PAGE_MARGIN + 60) {
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            cursorY = newPage.getMediaBox().getHeight() - PAGE_MARGIN;
        }

        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
        try (PDPageContentStream cs = new PDPageContentStream(document, currentPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            cs.setNonStrokingColor(GROUP_HEADER_BG);
            cs.addRect(PAGE_MARGIN, cursorY - 16, TABLE_WIDTH, 20);
            cs.fill();

            cs.beginText();
            cs.setFont(fontBold, 11);
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(PAGE_MARGIN + 5, cursorY - 12);
            cs.showText(className);
            cs.endText();
        }

        return cursorY - 22;
    }

    private float drawRegistrationTable(PDDocument document, float cursorY,
                                        List<RegistrationResponse> registrations,
                                        PDFont fontRegular, PDFont fontBold) throws IOException {
        Table table = buildTable(registrations, fontRegular, fontBold);

        TableDrawer tableDrawer = TableDrawer.builder()
                .table(table)
                .startX(PAGE_MARGIN)
                .startY(cursorY)
                .endY(PAGE_MARGIN)
                .build();

        tableDrawer.draw(
                () -> document,
                () -> new PDPage(PDRectangle.A4),
                PAGE_MARGIN
        );

        return cursorY - table.getHeight();
    }

    private Table buildTable(List<RegistrationResponse> registrations,
                             PDFont fontRegular, PDFont fontBold) {
        TableBuilder tableBuilder = Table.builder()
                .addColumnsOfWidth(30, 160, 55, 60, 50, 160)
                .fontSize(9)
                .font(fontRegular)
                .borderColor(Color.WHITE)
                .padding(4);

        tableBuilder.addRow(Row.builder()
                .add(TextCell.builder().text("#").horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text("Sailor Name").horizontalAlignment(LEFT).borderWidth(1).build())
                .add(TextCell.builder().text("Nation").horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text("Sail No.").horizontalAlignment(RIGHT).borderWidth(1).build())
                .add(TextCell.builder().text("Gender").horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text("Club").horizontalAlignment(LEFT).borderWidth(1).build())
                .backgroundColor(HEADER_BG)
                .textColor(HEADER_TEXT)
                .font(fontBold)
                .fontSize(9)
                .build());

        AtomicInteger rowNum = new AtomicInteger(1);
        for (RegistrationResponse reg : registrations) {
            int idx = rowNum.getAndIncrement();
            Color bgColor = idx % 2 == 0 ? ROW_EVEN : ROW_ODD;
            String nationCode = reg.sailingNation() != null ? reg.sailingNation().code() : "";
            String gender = reg.gender() != null ? reg.gender().name() : "";

            tableBuilder.addRow(Row.builder()
                    .add(TextCell.builder().text(String.valueOf(idx)).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(reg.sailorName()).horizontalAlignment(LEFT).borderWidth(1).build())
                    .add(TextCell.builder().text(nationCode).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(String.valueOf(reg.sailNumber())).horizontalAlignment(RIGHT).borderWidth(1).build())
                    .add(TextCell.builder().text(gender).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(reg.sailingClubName() != null ? reg.sailingClubName() : "").horizontalAlignment(LEFT).borderWidth(1).build())
                    .backgroundColor(bgColor)
                    .build());
        }

        return tableBuilder.build();
    }
}
