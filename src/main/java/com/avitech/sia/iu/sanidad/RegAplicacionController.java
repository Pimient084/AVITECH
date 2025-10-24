package com.avitech.sia.iu.sanidad;

import com.avitech.sia.iu.sanidad.dto.AplicacionDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RegAplicacionController {

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbLote;
    @FXML private ComboBox<String> cbMedicamento;
    @FXML private TextField tfDosis;
    @FXML private ComboBox<String> cbVia;
    @FXML private ComboBox<String> cbResponsable;
    @FXML private TextArea taObs;

    private Stage dialogStage;
    private AplicacionDTO result;  // output

    @FXML
    public void initialize() {
        // demo: valores de muestra; en real, cargar desde BD vía DAO/Service
        dpFecha.setValue(LocalDate.now());
        cbLote.getItems().addAll("Galpón 1", "Galpón 2", "Galpón 3");
        cbMedicamento.getItems().addAll("Vacuna Newcastle", "Vitamina E+Selenio", "Antibiótico Respiratorio");
        cbVia.getItems().addAll("Oral", "Agua", "Inyectable", "Tópica");
        cbResponsable.getItems().addAll("Juan Pérez", "Ana López", "Carlos Rivera");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public AplicacionDTO getResult() { return result; }

    @FXML
    private void onClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void onSave() {
        if (!valid()) return;

        result = new AplicacionDTO(
                dpFecha.getValue(),
                cbLote.getValue(),
                cbMedicamento.getValue(),
                tfDosis.getText().trim(),
                cbVia.getValue(),
                cbResponsable.getValue(),
                taObs.getText().trim()
        );

        onClose();
    }

    private boolean valid() {
        StringBuilder sb = new StringBuilder();

        if (dpFecha.getValue() == null) sb.append("• Fecha.\n");
        if (isEmpty(cbLote)) sb.append("• Lote/Galpón.\n");
        if (isEmpty(cbMedicamento)) sb.append("• Medicamento.\n");
        if (tfDosis.getText() == null || tfDosis.getText().isBlank()) sb.append("• Dosis.\n");
        if (isEmpty(cbVia)) sb.append("• Vía de aplicación.\n");
        if (isEmpty(cbResponsable)) sb.append("• Responsable.\n");

        if (sb.length() > 0) {
            new Alert(Alert.AlertType.WARNING, "Completa los campos:\n\n" + sb).showAndWait();
            return false;
        }
        return true;
    }

    private boolean isEmpty(ComboBox<String> cb) {
        return cb.getValue() == null || cb.getValue().isBlank();
    }
}
