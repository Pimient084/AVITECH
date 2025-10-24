package com.avitech.sia.iu;

import com.avitech.sia.db.Suministro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RegEntradaSuministroController {

    @FXML private TextField txtItem, txtCantidad, txtProveedor, txtValorTotal;
    @FXML private ComboBox<String> cbUnidad, cbResponsable;
    @FXML private DatePicker dpFecha;
    @FXML private TextArea txtMotivo;

    private Stage dialogStage;
    private Suministro result = null;

    @FXML
    private void initialize() {
        cbUnidad.setItems(FXCollections.observableArrayList("unidad", "kg", "L", "caja", "frasco"));
        dpFecha.setValue(LocalDate.now());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setResponsables(ObservableList<String> responsables) {
        cbResponsable.setItems(responsables);
        if (!responsables.isEmpty()) {
            cbResponsable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onSave() {
        if (validateInput()) {
            result = new Suministro(
                    0, // ID will be set by the database
                    dpFecha.getValue(),
                    "Entrada",
                    txtItem.getText(),
                    Integer.parseInt(txtCantidad.getText()),
                    cbUnidad.getValue(),
                    cbResponsable.getValue(),
                    txtProveedor.getText(),
                    txtMotivo.getText(),
                    new BigDecimal(txtValorTotal.getText())
            );
            dialogStage.close();
        }
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    public Suministro getResult() {
        return result;
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (txtItem.getText() == null || txtItem.getText().trim().isEmpty()) {
            errorMessage += "El ítem es obligatorio.\n";
        }
        if (txtCantidad.getText() == null || !txtCantidad.getText().matches("\\d+")) {
            errorMessage += "La cantidad debe ser un número entero.\n";
        }
        if (cbUnidad.getValue() == null) {
            errorMessage += "La unidad es obligatoria.\n";
        }
        if (cbResponsable.getValue() == null) {
            errorMessage += "El responsable es obligatorio.\n";
        }
        if (dpFecha.getValue() == null) {
            errorMessage += "La fecha es obligatoria.\n";
        }
        if (txtValorTotal.getText() == null || !txtValorTotal.getText().matches("\\d+(\\.\\d{1,2})?")) {
            errorMessage += "El valor total debe ser un número decimal (ej: 1250.00).\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
            return false;
        }
    }
}
