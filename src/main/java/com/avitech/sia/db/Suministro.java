package com.avitech.sia.db;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Suministro {
    private int id;
    private LocalDate fecha;
    private String tipo;
    private String item;
    private int cantidad;
    private String unidad;
    private String responsable;
    private String proveedor;
    private String motivo;
    private BigDecimal valorTotal;

    public Suministro(int id, LocalDate fecha, String tipo, String item, int cantidad, String unidad, String responsable, String proveedor, String motivo, BigDecimal valorTotal) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.item = item;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.responsable = responsable;
        this.proveedor = proveedor;
        this.motivo = motivo;
        this.valorTotal = valorTotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
