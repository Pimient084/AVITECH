package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.UsuarioDAO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuariosController {

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
    @FXML private TableColumn<UserRow, Integer> colId;
    @FXML private TableColumn<UserRow, String>  colUsuario;
    @FXML private TableColumn<UserRow, String>  colRol;
    @FXML private TableColumn<UserRow, String>  colEmail;
    @FXML private TableColumn<UserRow, String>  colTelefono;
    @FXML private TableColumn<UserRow, String>  colDireccion;
    @FXML private TableColumn<UserRow, HBox>    colAcciones;

    // Datos
    private final ObservableList<UserRow> masterData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredData;

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");

        // Opciones filtros
        cbRol.setItems(FXCollections.observableArrayList("Todos los roles", "ADMIN", "SUPERVISOR", "OPERADOR"));
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados", "Activo", "Inactivo")); // placeholder; no hay estado en DB
        cbRol.getSelectionModel().selectFirst();
        cbEstado.getSelectionModel().selectFirst();

        // Columnas
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().id()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().usuario()));
        colRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().rol()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().email())));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().telefono())));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(nullSafe(d.getValue().direccion())));

        colAcciones.setCellValueFactory(d -> new SimpleObjectProperty<>(buildActions(d.getValue())));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });

        loadUserData();

        // Filtrado
        filteredData = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, a, b) -> applyFilter());
        cbRol.valueProperty().addListener((obs, a, b) -> applyFilter());
        cbEstado.valueProperty().addListener((obs, a, b) -> applyFilter());

        // Ordenamiento + filtrado
        SortedList<UserRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblUsuarios.comparatorProperty());
        tblUsuarios.setItems(sortedData);

        // KPIs básicos
        lblTotal.textProperty().bind(Bindings.size(filteredData).asString());
        lblAdmins.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filteredData.stream().filter(u -> "ADMIN".equals(u.rol())).count()),
                filteredData));
        // Sin estado ni último acceso en DB
        lblActivos.setText("N/A");
        lblConHoy.setText("N/A");

        // CTA nuevo
        btnNuevo.setOnAction(e -> onNuevoUsuario());
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

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

        filteredData.setPredicate(u -> {
            boolean qOk = q.isEmpty()
                    || String.valueOf(u.id()).contains(q)
                    || contains(u.usuario(), q)
                    || contains(u.email(), q)
                    || contains(u.telefono(), q)
                    || contains(u.direccion(), q)
                    || contains(u.rol(), q);
            boolean rolOk = rol == null || rol.equals("Todos los roles") || Objects.equals(rol, u.rol());
            return qOk && rolOk;
        });
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
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

        return new HBox(6, btnEdit, btnPwd, btnDel);
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
            App.configureFullScreen(dialogStage);

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
            App.configureFullScreen(dialogStage);

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
    @FXML private void goDashboard()  { App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/suministros.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/sanidad.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/produccion.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml", "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml", "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml", "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }
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
