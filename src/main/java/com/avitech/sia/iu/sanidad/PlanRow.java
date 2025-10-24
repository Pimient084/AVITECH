package com.avitech.sia.iu.sanidad;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlanRow {
    private final StringProperty plan = new SimpleStringProperty();
    private final StringProperty desc = new SimpleStringProperty();
    private final StringProperty edad = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    public PlanRow(String p, String d, String e, String s) {
        plan.set(p);
        desc.set(d);
        edad.set(e);
        estado.set(s);
    }

    public StringProperty planProperty() {
        return plan;
    }

    public StringProperty descProperty() {
        return desc;
    }

    public StringProperty edadProperty() {
        return edad;
    }

    public StringProperty estadoProperty() {
        return estado;
    }
}
