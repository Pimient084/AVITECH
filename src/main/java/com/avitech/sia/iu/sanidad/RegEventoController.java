// src/main/java/com/avitech/sia/iu/sanidad/RegEventoController.java
package com.avitech.sia.iu.sanidad;

import com.avitech.sia.iu.sanidad.dto.EventoDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RegEventoController {

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbLoteAfectado;
    @FXML private ComboBox<String> cbTipoEvento;
    @FXML private TextField tfAvesAfectadas;
    @FXML private TextArea taDescripcion;
    @FXML private TextArea taAcciones;
    @FXML private ComboBox<String> cbResponsable;

    private Stage dialogStage;
    private EventoDTO result;

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());
        cbLoteAfectado.getItems().addAll("Galpón 1", "Galpón 2", "Galpón 3");
        cbTipoEvento.getItems().addAll("Enfermedad", "Incidencia", "Mortalidad", "Otro");
        cbResponsable.getItems().addAll("Juan Pérez", "Ana López", "Carlos Rivera");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public EventoDTO getResult() { return result; }

    @FXML
    private void onClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void onSave() {
        if (!valid()) return;

        result = new EventoDTO(
                dpFecha.getValue(),
                valueOf(cbLoteAfectado),
                valueOf(cbTipoEvento),
                tfAvesAfectadas.getText().trim(),
                taDescripcion.getText().trim(),
                taAcciones.getText().trim(),
                valueOf(cbResponsable)
        );
        onClose();
    }

    private boolean valid() {
        StringBuilder sb = new StringBuilder();

        if (dpFecha.getValue() == null) sb.append("• Fecha.\n");
        if (isEmpty(cbLoteAfectado)) sb.append("• Lote/Galpón afectado.\n");
        if (isEmpty(cbTipoEvento)) sb.append("• Tipo de evento.\n");
        if (tfAvesAfectadas.getText() == null || tfAvesAfectadas.getText().isBlank()) sb.append("• Aves afectadas.\n");
        if (taDescripcion.getText() == null || taDescripcion.getText().isBlank()) sb.append("• Descripción del evento.\n");
        if (taAcciones.getText() == null || taAcciones.getText().isBlank()) sb.append("• Acciones tomadas.\n");
        if (isEmpty(cbResponsable)) sb.append("• Responsable del reporte.\n");

        if (sb.length() > 0) {
            new Alert(Alert.AlertType.WARNING, "Completa los campos:\n\n" + sb).showAndWait();
            return false;
        }
        return true;
    }

    private boolean isEmpty(ComboBox<String> cb) {
        return cb.getValue() == null || cb.getValue().isBlank();
    }

    private String valueOf(ComboBox<String> cb) {
        return cb.getValue() == null ? "" : cb.getValue();
    }
}