package com.avitech.sia.iu.sanidad;

import com.avitech.sia.db.Medicamento;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class RegMedicamentoController {

    @FXML private TextField tfNombre, tfPresentacion, tfStock, tfStockMinimo, tfValorUnitario;

    private Stage dialogStage;
    private Medicamento result;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public Medicamento getResult() {
        return result;
    }

    @FXML
    private void onSave() {
        if (validateInput()) {
            result = new Medicamento(
                    0, // ID will be set by the database
                    tfNombre.getText(),
                    tfPresentacion.getText(),
                    Integer.parseInt(tfStock.getText()),
                    Integer.parseInt(tfStockMinimo.getText()),
                    new BigDecimal(tfValorUnitario.getText())
            );
            dialogStage.close();
        }
    }

    @FXML
    private void onClose() {
        result = null; // Indicate cancellation
        dialogStage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (tfNombre.getText() == null || tfNombre.getText().trim().isEmpty()) {
            errorMessage += "El nombre es obligatorio.\n";
        }
        if (tfPresentacion.getText() == null || tfPresentacion.getText().trim().isEmpty()) {
            errorMessage += "La presentación es obligatoria.\n";
        }
        if (tfStock.getText() == null || !tfStock.getText().matches("\\d+")) {
            errorMessage += "El stock debe ser un número entero.\n";
        }
        if (tfStockMinimo.getText() == null || !tfStockMinimo.getText().matches("\\d+")) {
            errorMessage += "El stock mínimo debe ser un número entero.\n";
        }
        if (tfValorUnitario.getText() == null || !tfValorUnitario.getText().matches("\\d+(\\.\\d{1,2})?")) {
            errorMessage += "El valor unitario debe ser un número decimal (ej: 150.50).\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
            return false;
        }
    }
}
