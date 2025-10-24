package com.avitech.sia.iu;

import com.avitech.sia.db.LoteDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegLoteController {

    @FXML private Label lblTitle;
    @FXML private TextField tfNombreLote, tfCantidadGallinas;
    @FXML private ComboBox<String> cbEstado;

    private Stage dialogStage;
    private LoteDAO.Lote lote = null;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo", "Reemplazo"));
        cbEstado.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setLote(LoteDAO.Lote lote) {
        this.lote = lote;
        this.isEditMode = true;
        lblTitle.setText("Editar Lote");
        tfNombreLote.setText(lote.getNombreLote());
        cbEstado.setValue(lote.getEstado());
        tfCantidadGallinas.setText(String.valueOf(lote.getCantidadGallinas()));
    }

    public LoteDAO.Lote getResult() {
        return lote;
    }

    @FXML
    private void onSave() {
        if (validateInput()) {
            if (isEditMode) {
                lote = lote.setNombreLote(tfNombreLote.getText());
                lote = lote.setEstado(cbEstado.getValue());
                lote = lote.setCantidadGallinas(Integer.parseInt(tfCantidadGallinas.getText()));
            } else {
                lote = new LoteDAO.Lote(
                        tfNombreLote.getText(),
                        cbEstado.getValue(),
                        Integer.parseInt(tfCantidadGallinas.getText())
                );
            }
            dialogStage.close();
        }
    }

    @FXML
    private void onCancel() {
        lote = null; // Indicate cancellation
        dialogStage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (tfNombreLote.getText() == null || tfNombreLote.getText().trim().isEmpty()) {
            errorMessage += "El nombre del lote es obligatorio.\n";
        }
        if (cbEstado.getValue() == null || cbEstado.getValue().trim().isEmpty()) {
            errorMessage += "El estado es obligatorio.\n";
        }
        if (tfCantidadGallinas.getText() == null || !tfCantidadGallinas.getText().matches("\\d+")) {
            errorMessage += "La cantidad de gallinas debe ser un número entero.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
            return false;
        }
    }
}
