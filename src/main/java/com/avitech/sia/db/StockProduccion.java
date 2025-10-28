package com.avitech.sia.db;

public class StockProduccion {
    private long id;
    private String producto;
    private String lote;
    private int cantidad;
    private String ubicacion;
    private String estado;

    public StockProduccion(long id, String producto, String lote, int cantidad, String ubicacion, String estado) {
        this.id = id;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public long getId() { return id; }
    public String getProducto() { return producto; }
    public String getLote() { return lote; }
    public int getCantidad() { return cantidad; }
    public String getUbicacion() { return ubicacion; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return "StockProduccion{" +
                "id=" + id +
                ", producto='" + producto + '\'' +
                ", lote='" + lote + '\'' +
                ", cantidad=" + cantidad +
                ", ubicacion='" + ubicacion + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}

