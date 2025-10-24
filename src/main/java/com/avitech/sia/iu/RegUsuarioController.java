package com.avitech.sia.iu;

import com.avitech.sia.db.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegUsuarioController {

    @FXML private Label lblTitle;
    @FXML private TextField txtUsuario, txtEmail, txtTelefono, txtDireccion;
    @FXML private PasswordField pwPassword;
    @FXML private ComboBox<String> cbRol;

    private Stage dialogStage;
    private UsuarioDAO.Usuario usuario = null; // For editing existing user
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        cbRol.setItems(FXCollections.observableArrayList("ADMIN", "SUPERVISOR", "OPERADOR"));
        cbRol.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUsuario(UsuarioDAO.Usuario usuario) {
        this.usuario = usuario;
        this.isEditMode = true;
        lblTitle.setText("Editar Usuario");
        txtUsuario.setText(usuario.usuario());
        txtUsuario.setDisable(true); // Username cannot be changed
        cbRol.setValue(usuario.rol());
        txtEmail.setText(usuario.email());
        txtTelefono.setText(usuario.telefono());
        txtDireccion.setText(usuario.direccion());
        pwPassword.setPromptText("Dejar en blanco para no cambiar");
    }

    public UsuarioDAO.Usuario getResult() {
        return usuario;
    }

    @FXML
    private void onSave() {
        if (validateInput()) {
            String passwordHash = pwPassword.getText().isEmpty() ?
                    (isEditMode ? usuario.password() : "") : // Keep old password if editing and not changed
                    pwPassword.getText(); // In a real app, hash this password

            if (isEditMode) {
                usuario = new UsuarioDAO.Usuario(
                        usuario.id(),
                        txtUsuario.getText(),
                        passwordHash,
                        cbRol.getValue(),
                        txtEmail.getText(),
                        txtTelefono.getText(),
                        txtDireccion.getText()
                );
            } else {
                usuario = new UsuarioDAO.Usuario(
                        0, // ID will be set by DB
                        txtUsuario.getText(),
                        passwordHash,
                        cbRol.getValue(),
                        txtEmail.getText(),
                        txtTelefono.getText(),
                        txtDireccion.getText()
                );
            }
            dialogStage.close();
        }
    }

    @FXML
    private void onCancel() {
        usuario = null; // Indicate cancellation
        dialogStage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (txtUsuario.getText() == null || txtUsuario.getText().trim().isEmpty()) {
            errorMessage += "El nombre de usuario es obligatorio.\n";
        }
        if (!isEditMode && (pwPassword.getText() == null || pwPassword.getText().trim().isEmpty())) {
            errorMessage += "La contraseña es obligatoria para nuevos usuarios.\n";
        }
        if (cbRol.getValue() == null || cbRol.getValue().trim().isEmpty()) {
            errorMessage += "El rol es obligatorio.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
            return false;
        }
    }
}
