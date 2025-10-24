package com.avitech.sia.db;

import java.math.BigDecimal;

public class Medicamento {
    private int id;
    private String nombre;
    private String presentacion;
    private int stock;
    private int stockMinimo;
    private BigDecimal valorUnitario;

    public Medicamento(int id, String nombre, String presentacion, int stock, int stockMinimo, BigDecimal valorUnitario) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.valorUnitario = valorUnitario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
    
    public double getStockLevel() {
        if (stockMinimo <= 0 || stock > stockMinimo * 2) {
            return 1.0; // Full or more than double the minimum
        }
        return (double) stock / (stockMinimo * 2);
    }
}
