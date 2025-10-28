package com.avitech.sia.iu.sanidad;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class PlanSanitarioFormController {

    @FXML private TextField txtPlan;
    @FXML private TextArea txtDesc;
    @FXML private TextField txtEdad;
    @FXML private ComboBox<String> cbEstado;

    private Stage dialogStage;
    private boolean saved = false;

    @FXML
    private void initialize() {
        // Poblamos las opciones del estado aquí (el FXML no define <items>)
        if (cbEstado != null) {
            cbEstado.setItems(FXCollections.observableArrayList("Preventivo", "Curativo", "Mixto"));
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setInitial(String plan, String desc, String edad, String estado) {
        if (plan != null) txtPlan.setText(plan);
        if (desc != null) txtDesc.setText(desc);
        if (edad != null) txtEdad.setText(edad);
        if (estado != null) cbEstado.getSelectionModel().select(estado);
    }

    @FXML
    private void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void onSave() {
        if (txtPlan.getText() == null || txtPlan.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "El nombre del plan es obligatorio").showAndWait();
            return;
        }
        if (cbEstado.getValue() == null || cbEstado.getValue().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Seleccione el estado del plan").showAndWait();
            return;
        }
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    public boolean isSaved() { return saved; }

    public String getPlan() { return txtPlan.getText(); }
    public String getDesc() { return txtDesc.getText(); }
    public String getEdad() { return txtEdad.getText(); }
    public String getEstado() { return cbEstado.getValue(); }
}
