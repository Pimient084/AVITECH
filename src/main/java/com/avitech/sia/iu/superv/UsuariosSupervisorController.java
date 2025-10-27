package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import com.avitech.sia.db.UsuarioDAO;
import com.avitech.sia.iu.RegUsuarioController; // Added import
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuariosSupervisorController {

    // DAOs
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final int ACTOR_ID = 1; // TODO: Replace with actual logged-in user ID

    // Topbar
    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    // Filtros y CTA
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbRol;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnNuevo;

    // KPIs
    @FXML private Label lblTotal;
    @FXML private Label lblActivos;
    @FXML private Label lblAdmins;
    @FXML private Label lblConHoy;

    // Tabla
    @FXML private TableView<UserRow> tblUsuarios;
    @FXML private TableColumn<UserRow, String> colNombre; // Will display 'usuario'
    @FXML private TableColumn<UserRow, String> colUsuario;
    @FXML private TableColumn<UserRow, String> colRol;
    @FXML private TableColumn<UserRow, String> colEstado;
    @FXML private TableColumn<UserRow, String> colUltimoAcc;
    @FXML private TableColumn<UserRow, HBox>   colAcciones; // Changed from String to HBox

    // Datos
    private final ObservableList<UserRow> masterData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredData;

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Supervisor");
        lblUserInfo.setText("Supervisor");

        // Opciones filtros
        cbRol.setItems(FXCollections.observableArrayList("Todos los roles", "ADMIN", "SUPERVISOR", "OPERADOR"));
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados", "Activo", "Inactivo")); // No hay campo 'estado' en DB
        cbRol.getSelectionModel().selectFirst();
        cbEstado.getSelectionModel().selectFirst();

        // Columnas
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().usuario())); // Display usuario in 'Nombre' column
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().usuario()));
        colRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().rol()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty("N/A")); // No hay campo 'estado' en DB
        colUltimoAcc.setCellValueFactory(d -> new SimpleStringProperty("N/A")); // No hay campo 'ultimo_acceso' en DB

        colAcciones.setCellValueFactory(d -> new SimpleObjectProperty<>(buildActions(d.getValue())));

        loadUserData();

        // Filtrado
        filteredData = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, a, b) -> applyFilter());
        cbRol.valueProperty().addListener((obs, a, b) -> applyFilter());
        cbEstado.valueProperty().addListener((obs, a, b) -> applyFilter());

        tblUsuarios.setItems(filteredData);

        // KPIs atados
        lblTotal.textProperty().bind(Bindings.size(filteredData).asString());
        lblActivos.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filteredData.stream().filter(u -> "Activo".equals("N/A")).count()), // No hay campo 'estado' en DB
                filteredData));
        lblAdmins.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filteredData.stream().filter(u -> "ADMIN".equals(u.rol())).count()),
                filteredData));
        lblConHoy.textProperty().bind(Bindings.createStringBinding(
                () -> "N/A", // No hay campo 'ultimo_acceso' en DB
                filteredData));

        // CTA nuevo
        btnNuevo.setOnAction(e -> onNuevoUsuario());
    }

    private void loadUserData() {
        try {
            List<UsuarioDAO.Usuario> usuarios = usuarioDAO.getAll();
            masterData.setAll(usuarios.stream().map(UserRow::new).collect(Collectors.toList()));
        } catch (Exception e) {
            showError("Error al cargar usuarios", e);
        }
    }

    private void applyFilter() {
        String q = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String rol = cbRol.getValue();
        String estado = cbEstado.getValue(); // Not used as 'estado' is not in DB

        filteredData.setPredicate(u -> {
            boolean qOk = q.isEmpty()
                    || u.usuario().toLowerCase().contains(q)
                    || (u.email() != null && u.email().toLowerCase().contains(q));
            boolean rolOk = rol == null || rol.equals("Todos los roles") || Objects.equals(rol, u.rol());
            // boolean estadoOk = estado == null || estado.equals("Todos los estados") || Objects.equals(estado, u.estado()); // Not used
            return qOk && rolOk; // && estadoOk;
        });
    }

    private HBox buildActions(UserRow row) {
        Button btnEdit = new Button("✏");
        Button btnPwd  = new Button("🔑");
        Button btnDel  = new Button("🗑");

        btnEdit.getStyleClass().add("ghostBtn");
        btnPwd.getStyleClass().add("ghostBtn");
        btnDel.getStyleClass().add("ghostBtn");

        btnEdit.setOnAction(e -> onEditarUsuario(row));
        btnPwd.setOnAction(e -> onCambiarPassword(row));
        btnDel.setOnAction(e -> onEliminarUsuario(row));

        HBox box = new HBox(6, btnEdit, btnPwd, btnDel);
        return box;
    }

    @FXML
    private void onNuevoUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_usuario.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Nuevo Usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNuevo.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegUsuarioController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            UsuarioDAO.Usuario newUser = controller.getResult();
            if (newUser != null) {
                usuarioDAO.insert(newUser, ACTOR_ID);
                loadUserData();
                applyFilter();
            }
        } catch (IOException e) {
            showError("Error al abrir el diálogo de nuevo usuario", e);
        } catch (Exception e) {
            showError("Error al guardar nuevo usuario", e);
        }
    }

    private void onEditarUsuario(UserRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_usuario.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblUsuarios.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegUsuarioController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            // Create a UsuarioDAO.Usuario object from UserRow for editing
            UsuarioDAO.Usuario userToEdit = new UsuarioDAO.Usuario(
                    row.id(), row.usuario(), row.passwordHash(), row.rol(), row.email(), row.telefono(), row.direccion()
            );
            controller.setUsuario(userToEdit);

            dialogStage.showAndWait();

            if (controller.isDeleted()) { // Check if the user was deleted from the modal
                usuarioDAO.delete(userToEdit.id(), ACTOR_ID);
                loadUserData();
                applyFilter();
            } else {
                UsuarioDAO.Usuario updatedUser = controller.getResult();
                if (updatedUser != null) {
                    usuarioDAO.update(updatedUser, ACTOR_ID);
                    loadUserData();
                    applyFilter();
                }
            }
        } catch (IOException e) {
            showError("Error al abrir el diálogo de edición", e);
        } catch (Exception e) {
            showError("Error al actualizar usuario", e);
        }
    }

    private void onCambiarPassword(UserRow row) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Cambiar contraseña para " + row.usuario());
        dialog.setContentText("Nueva contraseña:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                // In a real app, hash this password before sending to DAO
                usuarioDAO.updatePassword(row.id(), newPassword, ACTOR_ID);
                new Alert(Alert.AlertType.INFORMATION, "Contraseña actualizada con éxito.").showAndWait();
            } catch (Exception e) {
                showError("Error al cambiar contraseña", e);
            }
        });
    }

    private void onEliminarUsuario(UserRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar al usuario " + row.usuario() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                usuarioDAO.delete(row.id(), ACTOR_ID);
                loadUserData();
                applyFilter();
            } catch (Exception e) {
                showError("Error al eliminar usuario", e);
            }
        }
    }

    private void showError(String header, Exception e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, header + ": " + e.getMessage()).showAndWait();
    }

    /* ================== Navegación ================== */
    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech — SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/superv/reportes_super.fxml", "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml", "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/superv/parametros_super.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { /* Already here */ }
    @FXML private void goBackup()     { App.goTo("/fxml/superv/respaldos_super.fxml", "SIA Avitech — Respaldos"); }
    @FXML private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }

    /* ===== Modelo de fila ===== */
    public record UserRow(int id,
                          String usuario,
                          String passwordHash, // Stored for update, not displayed
                          String rol,
                          String email,
                          String telefono,
                          String direccion) {
        public UserRow(UsuarioDAO.Usuario u) {
            this(u.id(), u.usuario(), u.password(), u.rol(), u.email(), u.telefono(), u.direccion());
        }
    }
}
