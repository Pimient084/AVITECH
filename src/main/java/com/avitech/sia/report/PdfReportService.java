package com.avitech.sia.report;

import com.avitech.sia.db.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

    // =============== SANIDAD: Lista de medicamentos (nombre, presentación, stock, mínimo) ===============
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
            float[] colWidths = {300, 200, 100, 100};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true);
            for (Medicamento m : meds) {
                if (y < 80) { cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Sanidad (Medicamentos)", req); y = tableStartY(page); y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{ nz(m.getNombre()), nz(m.getPresentacion()), String.valueOf(m.getStock()), String.valueOf(m.getStockMinimo()) }, false);
            }
        } finally { cs.close(); }
    }

    // =============== PRODUCCION: Lista de producción (fecha, galpón, total, L, M, S, mort, resp) ===============
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
            float[] colWidths = {120, 90, 90, 60, 60, 60, 80, 200};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true);
            for (Produccion p : all) {
                if (y < 80) { cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Producción de Huevos", req); y = tableStartY(page); y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{
                        p.getFecha().toString(), String.valueOf(p.getGalpon()), String.valueOf(p.getTotalHuevos()),
                        String.valueOf(p.getHuevosL()), String.valueOf(p.getHuevosM()), String.valueOf(p.getHuevosS()),
                        String.valueOf(p.getMortalidad()), nz(p.getResponsable())
                }, false);
            }
        } finally { cs.close(); }
    }

    // =============== ALERTAS: descripciones recientes ===============
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
            float[] colWidths = {720};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true);
            for (String d : desc) {
                if (y < 80) { cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Alertas Recientes", req); y = tableStartY(page); y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{ nz(d) }, false);
            }
        } finally { cs.close(); }
    }

    // =============== AUDITORIA: lista de auditoría (fecha, usuarioId, acción, módulo, detalle) ===============
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
            float[] colWidths = {180, 100, 120, 120, 240};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true);
            for (AuditoriaDAO.AuditoriaRecord r : rows) {
                if (y < 80) { cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Auditoría", req); y = tableStartY(page); y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{
                        r.fecha().toString(), String.valueOf(r.idUsuario()), nz(r.accion()), nz(r.modulo()), trimTo(nz(r.detalle()), 80)
                }, false);
            }
        } finally { cs.close(); }
    }

    // =============== USUARIOS: lista de usuarios (usuario, rol, email, tel, dir) ===============
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
            float[] colWidths = {200, 130, 220, 120, 260};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true);
            for (UsuarioDAO.Usuario u : rows) {
                if (y < 80) { cs.close(); page = new PDPage(PAGE_SIZE); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Usuarios", req); y = tableStartY(page); y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{
                        nz(u.usuario()), nz(u.rol()), nz(u.email()), nz(u.telefono()), trimTo(nz(u.direccion()), 80)
                }, false);
            }
        } finally { cs.close(); }
    }

    private float drawRow(PDPageContentStream cs, float x, float y, float[] colWidths, String[] cells, boolean header) throws IOException {
        float height = 18f; // altura estándar de fila
        float cx = x;
        for (int i = 0; i < colWidths.length; i++) {
            String text = i < cells.length ? (cells[i] == null ? "" : cells[i]) : "";
            String prepared = trimTo(sanitize(text), (int) colWidths[i] / 6);
            cs.beginText();
            cs.setFont(header ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, header ? 10 : 9);
            cs.newLineAtOffset(cx + 4, y - ROW_TEXT_BASELINE_OFFSET);
            cs.showText(prepared);
            cs.endText();
            cx += colWidths[i];
        }
        // Línea inferior de la fila
        cs.moveTo(x, y - height);
        cs.lineTo(x + sum(colWidths), y - height);
        cs.stroke();
        return y - height;
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
