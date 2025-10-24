package com.avitech.sia.iu.sanidad;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MedRow {
    private final StringProperty nombre = new SimpleStringProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();
    private final DoubleProperty nivel = new SimpleDoubleProperty(); // 0..1
    private final StringProperty inventario = new SimpleStringProperty();

    public MedRow(String n, int s, double pct, String inv) {
        nombre.set(n);
        stock.set(s);
        nivel.set(pct);
        inventario.set(inv);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty stockTextoProperty() {
        return new SimpleStringProperty(String.valueOf(stock.get()) + " frascos");
    }

    public DoubleProperty nivelProperty() {
        return nivel;
    }

    public StringProperty inventarioProperty() {
        return inventario;
    }

    public double getNivel() {
        return nivel.get();
    }
}
