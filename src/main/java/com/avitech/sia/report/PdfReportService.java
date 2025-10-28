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
        float margin = 40;
        float width = mediaBox.getWidth() - 2 * margin;
        float y = mediaBox.getHeight() - margin;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.newLineAtOffset(margin, y);
        cs.showText("AVITECH — SIA");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y - 22);
        cs.showText(title);
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(margin, y - 38);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        cs.showText("Generado: " + ts);
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
        cs.newLineAtOffset(margin, y - 54);
        if (filtrosText.length() > 140) filtrosText = filtrosText.substring(0, 137) + "...";
        cs.showText(filtrosText);
        cs.endText();

        cs.moveTo(margin, y - 60);
        cs.lineTo(margin + width, y - 60);
        cs.stroke();
    }

    // =============== SANIDAD: Lista de medicamentos (nombre, presentación, stock, mínimo) ===============
    private void buildSanidad(PDDocument doc, ReportRequest req) throws Exception {
        MedicamentoDAO medDao = new MedicamentoDAO();
        List<Medicamento> meds;
        try { meds = medDao.getAll(); } catch (Exception ex) { meds = new ArrayList<>(); }

        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Sanidad (Medicamentos)", req);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - 80;
            float[] colWidths = {220, 140, 80, 80};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true);
            for (Medicamento m : meds) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Sanidad (Medicamentos)", req); y = page.getMediaBox().getHeight() - 80; y = drawRow(cs, margin, y, colWidths, new String[]{"Nombre", "Presentación", "Stock", "Mínimo"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{ nz(m.getNombre()), nz(m.getPresentacion()), String.valueOf(m.getStock()), String.valueOf(m.getStockMinimo()) }, false);
            }
        } finally { cs.close(); }
    }

    // =============== PRODUCCION: Lista de producción (fecha, galpón, total, L, M, S, mort, resp) ===============
    private void buildProduccion(PDDocument doc, ReportRequest req) throws Exception {
        ProduccionDAO prodDao = new ProduccionDAO();
        List<Produccion> all;
        try { all = prodDao.getAll(); } catch (Exception ex) { all = new ArrayList<>(); }

        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Producción de Huevos", req);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - 80;
            float[] colWidths = {80, 60, 70, 50, 50, 50, 60, 120};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true);
            for (Produccion p : all) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Producción de Huevos", req); y = page.getMediaBox().getHeight() - 80; y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Galpón", "Total", "L", "M", "S", "Mort.", "Responsable"}, true); }
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

        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Alertas Recientes", req);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - 80;
            float[] colWidths = {520};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true);
            for (String d : desc) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Alertas Recientes", req); y = page.getMediaBox().getHeight() - 80; y = drawRow(cs, margin, y, colWidths, new String[]{"Descripción"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{ nz(d) }, false);
            }
        } finally { cs.close(); }
    }

    // =============== AUDITORIA: lista de auditoría (fecha, usuarioId, acción, módulo, detalle) ===============
    private void buildAuditoria(PDDocument doc, ReportRequest req) throws Exception {
        AuditoriaDAO audDao = new AuditoriaDAO();
        List<AuditoriaDAO.AuditoriaRecord> rows;
        try { rows = audDao.getAll(); } catch (Exception ex) { rows = new ArrayList<>(); }

        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Auditoría", req);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - 80;
            float[] colWidths = {120, 70, 90, 90, 180};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true);
            for (AuditoriaDAO.AuditoriaRecord r : rows) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Auditoría", req); y = page.getMediaBox().getHeight() - 80; y = drawRow(cs, margin, y, colWidths, new String[]{"Fecha", "Id User", "Acción", "Módulo", "Detalle"}, true); }
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

        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        try {
            buildHeader(cs, page.getMediaBox(), "Reporte — Usuarios", req);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - 80;
            float[] colWidths = {140, 90, 150, 90, 160};
            y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true);
            for (UsuarioDAO.Usuario u : rows) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); buildHeader(cs, page.getMediaBox(), "Reporte — Usuarios", req); y = page.getMediaBox().getHeight() - 80; y = drawRow(cs, margin, y, colWidths, new String[]{"Usuario", "Rol", "Email", "Teléfono", "Dirección"}, true); }
                y = drawRow(cs, margin, y, colWidths, new String[]{
                        nz(u.usuario()), nz(u.rol()), nz(u.email()), nz(u.telefono()), trimTo(nz(u.direccion()), 40)
                }, false);
            }
        } finally { cs.close(); }
    }

    private float drawRow(PDPageContentStream cs, float x, float y, float[] colWidths, String[] cells, boolean header) throws IOException {
        float height = 18f;
        float cx = x;
        for (int i = 0; i < colWidths.length; i++) {
            String text = i < cells.length ? (cells[i] == null ? "" : cells[i]) : "";
            cs.beginText();
            cs.setFont(header ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, header ? 10 : 9);
            cs.newLineAtOffset(cx + 4, y - 12);
            cs.showText(trimTo(text, (int) colWidths[i] / 6));
            cs.endText();
            cx += colWidths[i];
        }
        cs.moveTo(x, y - height);
        cs.lineTo(x + sum(colWidths), y - height);
        cs.stroke();
        return y - height;
    }

    private float sum(float[] a) { float t = 0; for (float v : a) t += v; return t; }
    private String nz(String s) { return s == null ? "" : s; }
}
