package com.avitech.sia.iu;

import java.time.LocalDate;

public class Mov {
    public final String fecha, item, cantidad, unidad, tipo, responsable, detalles, stock;
    public final String itemLc, detallesLc, respLc;
    public final LocalDate localDate;

    public Mov(String fecha, String item, String cantidad, String unidad, String tipo,
               String responsable, String detalles, String stock) {
        this.fecha = fecha;
        this.item = item;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.tipo = tipo;
        this.responsable = responsable;
        this.detalles = detalles;
        this.stock = stock;

        this.itemLc = item.toLowerCase();
        this.detallesLc = detalles.toLowerCase();
        this.respLc = responsable.toLowerCase();

        // intenta parsear yyyy-MM-dd desde el prefijo de fecha
        LocalDate ld = null;
        try { ld = LocalDate.parse(fecha.substring(0, 10)); } catch (Exception ignored) {}
        this.localDate = ld;
    }
}
