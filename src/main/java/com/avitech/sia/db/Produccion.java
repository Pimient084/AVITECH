package com.avitech.sia.db;

import java.time.LocalDate;

public class Produccion {
    private int id;
    private LocalDate fecha;
    private int galpon;
    private int totalHuevos;
    private int huevosL;
    private int huevosM;
    private int huevosS;
    private float temperatura;
    private float humedad;
    private int mortalidad;
    private String responsable;

    public Produccion(int id, LocalDate fecha, int galpon, int totalHuevos, int huevosL, int huevosM, int huevosS, float temperatura, float humedad, int mortalidad, String responsable) {
        this.id = id;
        this.fecha = fecha;
        this.galpon = galpon;
        this.totalHuevos = totalHuevos;
        this.huevosL = huevosL;
        this.huevosM = huevosM;
        this.huevosS = huevosS;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.mortalidad = mortalidad;
        this.responsable = responsable;
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

    public int getGalpon() {
        return galpon;
    }

    public void setGalpon(int galpon) {
        this.galpon = galpon;
    }

    public int getTotalHuevos() {
        return totalHuevos;
    }

    public void setTotalHuevos(int totalHuevos) {
        this.totalHuevos = totalHuevos;
    }

    public int getHuevosL() {
        return huevosL;
    }

    public void setHuevosL(int huevosL) {
        this.huevosL = huevosL;
    }

    public int getHuevosM() {
        return huevosM;
    }

    public void setHuevosM(int huevosM) {
        this.huevosM = huevosM;
    }

    public int getHuevosS() {
        return huevosS;
    }

    public void setHuevosS(int huevosS) {
        this.huevosS = huevosS;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(float temperatura) {
        this.temperatura = temperatura;
    }

    public float getHumedad() {
        return humedad;
    }

    public void setHumedad(float humedad) {
        this.humedad = humedad;
    }

    public int getMortalidad() {
        return mortalidad;
    }

    public void setMortalidad(int mortalidad) {
        this.mortalidad = mortalidad;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
}
