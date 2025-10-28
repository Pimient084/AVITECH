package com.avitech.sia.db;

public class PlanCatalog {
    private int id;
    private String nombre;
    private String descripcion;
    private String edad;
    private String estado;

    public PlanCatalog() {}

    public PlanCatalog(int id, String nombre, String descripcion, String edad, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.edad = edad;
        this.estado = estado;
    }

    public PlanCatalog(String nombre, String descripcion, String edad, String estado) {
        this(0, nombre, descripcion, edad, estado);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

