package com.avitech.sia.report;

import com.avitech.sia.db.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PdfReportService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    // Usar tamaño carta apaisado (horizontal) para todos los reportes (swap width/height)
    private static final PDRectangle PAGE_SIZE = new PDRectangle(PDRectangle.LETTER.getHeight(), PDRectangle.LETTER.getWidth());

    // Constantes de layout: margen y separación clara (2 líneas ~ 24pt) entre encabezado y tabla
    private static final float MARGIN = 40f;
    private static final float HEADER_BLOCK_HEIGHT = 60f; // distancia desde el tope (y - MARGIN) hasta la línea inferior del encabezado
    private static final float GAP_AFTER_HEADER = 24f; // dos líneas de ~12pt
    private static final float ROW_TEXT_BASELINE_OFFSET = 12f; // drawRow posiciona el texto en y - 12

    // Tipografía y métricas
    private static final PDFont FONT = PDType1Font.HELVETICA;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final float FONT_SIZE = 9f;
    private static final float FONT_SIZE_HEADER = 10f;
    private static final float LINE_HEIGHT = 12f; // altura por línea de texto
    private static final float ROW_V_PADDING = 4f; // padding vertical dentro de la celda

    public Path generate(ReportRequest req) throws Exception {
        Objects.requireNonNull(req, "ReportRequest no debe ser null");

        Path outDir = ensureOutDir();
        String baseName = switch (req.getType()) {
            case SANIDAD -> "reporte_sanidad";
            case PRODUCCION -> "reporte_produccion";
            case ALERTAS -> "reporte_alertas";
            case AUDITORIA -> "reporte_auditoria";
            case USUARIOS -> "reporte_usuarios";
        };
        Path out = outDir.resolve(baseName + "_" + TS.format(LocalDateTime.now()) + ".pdf");

        try (PDDocument doc = new PDDocument()) {
            switch (req.getType()) {
                case SANIDAD -> buildSanidad(doc, req);
                case PRODUCCION -> buildProduccion(doc, req);
                case ALERTAS -> buildAlertas(doc, req);
                case AUDITORIA -> buildAuditoria(doc, req);
                case USUARIOS -> buildUsuarios(doc, req);
            }
            doc.save(out.toFile());
        }
        return out;
    }

    private Path ensureOutDir() throws IOException {
        Path out = Paths.get("build", "reports", "pdf");
        Files.createDirectories(out);
        return out;
    }

    private void buildHeader(PDPageContentStream cs, PDRectangle mediaBox, String title, ReportRequest req) throws IOException {
        float width = mediaBox.getWidth() - 2 * MARGIN;
        float y = mediaBox.getHeight() - MARGIN;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(sanitize("AVITECH — SIA"));
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(MARGIN, y - 22);
        cs.showText(sanitize(title));
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(MARGIN, y - 38);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        cs.showText(sanitize("Generado: " + ts));
        cs.endText();

        List<String> filtros = new ArrayList<>();
        req.getDesde().ifPresent(d -> filtros.add("Desde: " + d));
        req.getHasta().ifPresent(h -> filtros.add("Hasta: " + h));
        req.getLote().ifPresent(l -> filtros.add("Lote: " + l));
        req.getArticulo().ifPresent(a -> filtros.add("Artículo: " + a));
        req.getCategoria().ifPresent(c -> filtros.add("Categoría: " + c));
        req.getResponsable().ifPresent(r -> filtros.add("Responsable: " + r));
        String filtrosText = filtros.isEmpty() ? "Sin filtros" : String.join("  |  ", filtros);

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        cs.newLineAtOffset(MARGIN, y - 54);
        if (filtrosText.length() > 140) filtrosText = filtrosText.substring(0, 137) + "...";
        cs.showText(sanitize(filtrosText));
        cs.endText();

        // Línea inferior del encabezado
        cs.moveTo(MARGIN, y - HEADER_BLOCK_HEIGHT);
        cs.lineTo(MARGIN + width, y - HEADER_BLOCK_HEIGHT);
        cs.stroke();
    }

    // Calcula el Y inicial de la tabla para que el primer texto quede a 2 líneas de la línea del encabezado
    private float tableStartY(PDPage page) {
        float headerLineY = page.getMediaBox().getHeight() - MARGIN - HEADER_BLOCK_HEIGHT;
        // drawRow escribe el texto en (y - ROW_TEXT_BASELINE_OFFSET), así que subimos 12 para que la base del texto
        // quede exactamente GAP_AFTER_HEADER por debajo de la línea del encabezado.
        return headerLineY - GAP_AFTER_HEADER + ROW_TEXT_BASELINE_OFFSET;
    }

    private float[] scaleToWidth(PDRectangle mediaBox, float[] raw) {
        float available = mediaBox.getWidth() - 2 * MARGIN;
        float sum = 0f; for (float v : raw) sum += v;
        if (sum <= 0) return raw;
        float factor = available / sum;
        float[] out = new float[raw.length];
        for (int i = 0; i < raw.length; i++) out[i] = raw[i] * factor;
        return out;
    }

    private float computeRowHeight(String[] cells, float[] colWidths, boolean header) throws IOException {
        float maxLines = 1f;
        PDFont f = header ? FONT_BOLD : FONT;
        float fs = header ? FONT_SIZE_HEADER : FONT_SIZE;
        for (int i = 0; i < colWidths.length; i++) {
            String text = i < cells.length ? sanitize(nz(cells[i])) : "";
            List<String> lines = wrapText(text, f, fs, colWidths[i] - 8 /*padding horizontal aprox*/);
            if (lines.size() > maxLines) maxLines = lines.size();
        }
        return ROW_V_PADDING * 2 + maxLines * LINE_HEIGHT;
    }

    // =============== SANIDAD ===============
    private void buildSanidad(PDDocument doc, ReportRequest req) throws Exception {
        MedicamentoDAO medDao = new MedicamentoDAO();
        List<Medicamento> meds;
        try { meds = medDao.getAll(); } catch (Exception ex) { meds = new ArrayList<>(); }

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Sanidad (Medicamentos)", req);
            float margin = MARGIN;
            float y = tableStartY(page);
            float[] colWidths = scaleToWidth(page.getMediaBox(), new float[]{300, 200, 100, 100});
            y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true);
            for (Medicamento m : meds) {
                String[] cells = new String[]{ nz(m.getNombre()), nz(m.getPresentacion()), String.valueOf(m.getStock()), String.valueOf(m.getStockMinimo()) };
                float nextH = computeRowHeight(cells, colWidths, false);
                if (y - nextH < MARGIN + 20) {
                    cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page);
                    buildHeader(cs, page.getMediaBox(), "Reporte — Sanidad (Medicamentos)", req);
                    y = tableStartY(page);
                    colWidths = scaleToWidth(page.getMediaBox(), new float[]{300, 200, 100, 100});
                    y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true);
                }
                y = drawRow(cs, margin, y, colWidths, cells, false);
            }
        } finally { cs.close(); }
    }

    // =============== PRODUCCION ===============
    private void buildProduccion(PDDocument doc, ReportRequest req) throws Exception {
        ProduccionDAO prodDao = new ProduccionDAO();
        List<Produccion> all;
        try { all = prodDao.getAll(); } catch (Exception ex) { all = new ArrayList<>(); }

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Producción de Huevos", req);
            float margin = MARGIN;
            float y = tableStartY(page);
            float[] colWidths = scaleToWidth(page.getMediaBox(), new float[]{120, 90, 90, 60, 60, 60, 80, 200});
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true);
            for (Produccion p : all) {
                String[] cells = new String[]{
                        p.getFecha().toString(), String.valueOf(p.getGalpon()), String.valueOf(p.getTotalHuevos()),
                        String.valueOf(p.getHuevosL()), String.valueOf(p.getHuevosM()), String.valueOf(p.getHuevosS()),
                        String.valueOf(p.getMortalidad()), nz(p.getResponsable())
                };
                float nextH = computeRowHeight(cells, colWidths, false);
                if (y - nextH < MARGIN + 20) {
                    cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page);
                    buildHeader(cs, page.getMediaBox(), "Reporte — Producción de Huevos", req);
                    y = tableStartY(page);
                    colWidths = scaleToWidth(page.getMediaBox(), new float[]{120, 90, 90, 60, 60, 60, 80, 200});
                    y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true);
                }
                y = drawRow(cs, margin, y, colWidths, cells, false);
            }
        } finally { cs.close(); }
    }

    // =============== ALERTAS ===============
    private void buildAlertas(PDDocument doc, ReportRequest req) throws Exception {
        AlertaDAO alertaDAO = new AlertaDAO();
        List<String> desc;
        try { desc = alertaDAO.getRecentAlertsDescriptions(100); } catch (Exception ex) { desc = new ArrayList<>(); }

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Alertas Recientes", req);
            float margin = MARGIN;
            float y = tableStartY(page);
            float[] colWidths = scaleToWidth(page.getMediaBox(), new float[]{720});
            y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true);
            for (String d : desc) {
                String[] cells = new String[]{ nz(d) };
                float nextH = computeRowHeight(cells, colWidths, false);
                if (y - nextH < MARGIN + 20) {
                    cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page);
                    buildHeader(cs, page.getMediaBox(), "Reporte — Alertas Recientes", req);
                    y = tableStartY(page);
                    colWidths = scaleToWidth(page.getMediaBox(), new float[]{720});
                    y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true);
                }
                y = drawRow(cs, margin, y, colWidths, cells, false);
            }
        } finally { cs.close(); }
    }

    // =============== AUDITORIA ===============
    private void buildAuditoria(PDDocument doc, ReportRequest req) throws Exception {
        AuditoriaDAO audDao = new AuditoriaDAO();
        List<AuditoriaDAO.AuditoriaRecord> rows;
        try { rows = audDao.getAll(); } catch (Exception ex) { rows = new ArrayList<>(); }

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Auditoría", req);
            float margin = MARGIN;
            float y = tableStartY(page);
            float[] colWidths = scaleToWidth(page.getMediaBox(), new float[]{180, 100, 120, 120, 240});
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true);
            for (AuditoriaDAO.AuditoriaRecord r : rows) {
                String[] cells = new String[]{
                        r.fecha().toString(), String.valueOf(r.idUsuario()), nz(r.accion()), nz(r.modulo()), nz(r.detalle())
                };
                float nextH = computeRowHeight(cells, colWidths, false);
                if (y - nextH < MARGIN + 20) {
                    cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page);
                    buildHeader(cs, page.getMediaBox(), "Reporte — Auditoría", req);
                    y = tableStartY(page);
                    colWidths = scaleToWidth(page.getMediaBox(), new float[]{180, 100, 120, 120, 240});
                    y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true);
                }
                y = drawRow(cs, margin, y, colWidths, cells, false);
            }
        } finally { cs.close(); }
    }

    // =============== USUARIOS ===============
    private void buildUsuarios(PDDocument doc, ReportRequest req) throws Exception {
        UsuarioDAO udao = new UsuarioDAO();
        List<UsuarioDAO.Usuario> rows;
        try { rows = udao.getAll(); } catch (Exception ex) { rows = new ArrayList<>(); }

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Usuarios", req);
            float margin = MARGIN;
            float y = tableStartY(page);
            float[] colWidths = scaleToWidth(page.getMediaBox(), new float[]{200, 130, 220, 120, 260});
            y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true);
            for (UsuarioDAO.Usuario u : rows) {
                String[] cells = new String[]{ nz(u.usuario()), nz(u.rol()), nz(u.email()), nz(u.telefono()), nz(u.direccion()) };
                float nextH = computeRowHeight(cells, colWidths, false);
                if (y - nextH < MARGIN + 20) {
                    cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page);
                    buildHeader(cs, page.getMediaBox(), "Reporte — Usuarios", req);
                    y = tableStartY(page);
                    colWidths = scaleToWidth(page.getMediaBox(), new float[]{200, 130, 220, 120, 260});
                    y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true);
                }
                y = drawRow(cs, margin, y, colWidths, cells, false);
            }
        } finally { cs.close(); }
    }

    private float drawRow(PDPageContentStream cs, float x, float y, float[] colWidths, String[] cells, boolean header) throws IOException {
        PDFont f = header ? FONT_BOLD : FONT;
        float fs = header ? FONT_SIZE_HEADER : FONT_SIZE;

        // Precalcular envolturas y altura
        List<List<String>> wrapped = new ArrayList<>();
        int maxLines = 1;
        for (int i = 0; i < colWidths.length; i++) {
            String text = i < cells.length ? sanitize(nz(cells[i])) : "";
            List<String> lines = wrapText(text, f, fs, colWidths[i] - 8);
            wrapped.add(lines);
            if (lines.size() > maxLines) maxLines = lines.size();
        }
        float height = ROW_V_PADDING * 2 + maxLines * LINE_HEIGHT;

        float cx = x;
        for (int i = 0; i < colWidths.length; i++) {
            List<String> lines = wrapped.get(i);
            // Dibujar cada línea en la celda
            float textY = y - ROW_TEXT_BASELINE_OFFSET - ROW_V_PADDING;
            for (int li = 0; li < lines.size(); li++) {
                cs.beginText();
                cs.setFont(f, fs);
                cs.newLineAtOffset(cx + 4, textY - li * LINE_HEIGHT);
                cs.showText(lines.get(li));
                cs.endText();
            }
            cx += colWidths[i];
        }
        // Línea inferior de la fila
        cs.moveTo(x, y - height);
        cs.lineTo(x + sum(colWidths), y - height);
        cs.stroke();
        return y - height;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) { lines.add(""); return lines; }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            String candidate = current.length() == 0 ? w : current + " " + w;
            if (stringWidth(candidate, font, fontSize) <= maxWidth) {
                current.setLength(0); current.append(candidate);
            } else {
                if (current.length() > 0) { lines.add(current.toString()); current.setLength(0); }
                // Si la palabra sola no cabe, partirla
                if (stringWidth(w, font, fontSize) <= maxWidth) {
                    current.append(w);
                } else {
                    // Partir por caracteres
                    String remaining = w;
                    while (!remaining.isEmpty()) {
                        int cut = pickFittingPrefix(remaining, font, fontSize, maxWidth);
                        lines.add(remaining.substring(0, cut));
                        remaining = remaining.substring(cut);
                    }
                }
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private int pickFittingPrefix(String s, PDFont font, float fontSize, float maxWidth) throws IOException {
        int lo = 1, hi = s.length(), best = 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            float w = stringWidth(s.substring(0, mid), font, fontSize);
            if (w <= maxWidth) { best = mid; lo = mid + 1; }
            else hi = mid - 1;
        }
        return Math.max(1, best);
    }

    private float stringWidth(String s, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(s) / 1000f * fontSize;
    }

    private String sanitize(String s) {
        if (s == null) return "";
        // Reemplazar CR/LF y otros controles por espacio, colapsar espacios, y trim
        String t = s.replace('\n', ' ').replace('\r', ' ');
        t = t.replaceAll("\\p{C}", " ");
        t = t.replaceAll("\\s{2,}", " ").trim();
        return t;
    }

    private String trimTo(String s, int max) {
        if (s == null) return "";
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private float sum(float[] a) { float t = 0; for (float v : a) t += v; return t; }
    private String nz(String s) { return s == null ? "" : s; }
}
