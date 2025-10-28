package com.avitech.sia.report;

import java.time.LocalDate;
import java.util.Optional;

public class ReportRequest {
    private final ReportType type;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final String lote;
    private final String articulo;
    private final String categoria;
    private final String responsable;
    private final boolean preview;

    public ReportRequest(ReportType type, LocalDate desde, LocalDate hasta,
                         String lote, String articulo, String categoria,
                         String responsable, boolean preview) {
        this.type = type;
        this.desde = desde;
        this.hasta = hasta;
        this.lote = normalize(lote);
        this.articulo = normalize(articulo);
        this.categoria = normalize(categoria);
        this.responsable = normalize(responsable);
        this.preview = preview;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || t.toLowerCase().startsWith("todo")) return null;
        return t;
    }

    public ReportType getType() { return type; }
    public Optional<LocalDate> getDesde() { return Optional.ofNullable(desde); }
    public Optional<LocalDate> getHasta() { return Optional.ofNullable(hasta); }
    public Optional<String> getLote() { return Optional.ofNullable(lote); }
    public Optional<String> getArticulo() { return Optional.ofNullable(articulo); }
    public Optional<String> getCategoria() { return Optional.ofNullable(categoria); }
    public Optional<String> getResponsable() { return Optional.ofNullable(responsable); }
    public boolean isPreview() { return preview; }
}

