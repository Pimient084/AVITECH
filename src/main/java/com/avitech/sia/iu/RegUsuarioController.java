package com.avitech.sia.iu;

import com.avitech.sia.db.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Optional;

public class RegUsuarioController {

    @FXML private Label lblTitle;
    @FXML private TextField txtUsuario, txtEmail, txtTelefono;
    @FXML private TextArea txtDireccion;
    @FXML private PasswordField pwPassword;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnEliminar; // Added for the delete button

    private Stage dialogStage;
    private UsuarioDAO.Usuario usuario = null; // For editing existing user
    private boolean isEditMode = false;
    private boolean deleted = false; // To indicate if the user was deleted

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

        // Show delete button only in edit mode
        btnEliminar.setVisible(true);
        btnEliminar.setManaged(true);
    }

    public UsuarioDAO.Usuario getResult() {
        return usuario;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @FXML
    private void onSave() {
        if (validateInput()) {
            String rawPassword = pwPassword.getText();
            String passwordHash;

            if (rawPassword.isEmpty()) {
                passwordHash = isEditMode ? usuario.password() : ""; // Keep old password if editing and not changed, or empty for new if allowed
            } else {
                // TODO: Implement proper password hashing here (e.g., using BCrypt)
                // For now, we'll use the raw password, but this is INSECURE.
                passwordHash = rawPassword;
                System.out.println("WARNING: Password is not hashed! Raw password: " + passwordHash); // Temporary debug
            }

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

    @FXML
    private void onDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar al usuario " + usuario.usuario() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleted = true;
            usuario = null; // Clear user object as it's being deleted
            dialogStage.close();
        }
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
