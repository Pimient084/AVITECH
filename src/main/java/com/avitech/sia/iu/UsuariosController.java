package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.DBUtil;
import com.avitech.sia.db.UserDAO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class UsuariosController {

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
    @FXML private TableColumn<UserRow, String> colNombre;
    @FXML private TableColumn<UserRow, String> colUsuario;
    @FXML private TableColumn<UserRow, String> colRol;
    @FXML private TableColumn<UserRow, String> colEstado;
    @FXML private TableColumn<UserRow, String> colUltimoAcc;
    @FXML private TableColumn<UserRow, HBox>   colAcciones;

    // Datos
    private final ObservableList<UserRow> baseData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filtered;
    private UserDAO userDAO;

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");
        checkDatabaseConnection();

        userDAO = new UserDAO();

        // Opciones filtros
        cbRol.setItems(FXCollections.observableArrayList("Todos los roles", "Administrador", "Supervisor", "Operador"));
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados", "Activo", "Inactivo"));
        cbRol.getSelectionModel().selectFirst();
        cbEstado.getSelectionModel().selectFirst();

        // Columnas
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombre()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().usuario()));
        colRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().rol()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colUltimoAcc.setCellValueFactory(d -> new SimpleStringProperty(DF.format(d.getValue().ultimoAcceso())));

        colAcciones.setCellValueFactory(d -> new SimpleObjectProperty<>(buildActions(d.getValue())));

        // Cargar datos de la base de datos
        loadUserData();

        // Filtrado
        filtered = new FilteredList<>(baseData, r -> true);
        tfSearch.textProperty().addListener((obs, a, b) -> applyFilter());
        cbRol.valueProperty().addListener((obs, a, b) -> applyFilter());
        cbEstado.valueProperty().addListener((obs, a, b) -> applyFilter());

        tblUsuarios.setItems(filtered);

        // KPIs atados
        lblTotal.textProperty().bind(Bindings.size(filtered).asString());
        lblActivos.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filtered.stream().filter(u -> "Activo".equals(u.estado())).count()),
                filtered));
        lblAdmins.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filtered.stream().filter(u -> "Administrador".equals(u.rol())).count()),
                filtered));
        // Conectados hoy (demo: usuarios con último acceso del día actual)
        lblConHoy.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(filtered.stream().filter(u -> u.ultimoAcceso().toLocalDate().equals(LocalDateTime.now().toLocalDate())).count()),
                filtered));

        // CTA nuevo
        btnNuevo.setOnAction(e -> onNuevoUsuario());
    }

    private void checkDatabaseConnection() {
        try (Connection connection = DBUtil.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                lblSystemStatus.setText("Sistema Online – MySQL Conectado");
                lblSystemStatus.setStyle("-fx-text-fill: #4CAF50;"); // Green color for success
            } else {
                lblSystemStatus.setText("Sistema Offline – MySQL Desconectado");
                lblSystemStatus.setStyle("-fx-text-fill: #F44336;"); // Red color for error
            }
        } catch (SQLException e) {
            lblSystemStatus.setText("Sistema Offline – Error de Conexión MySQL");
            lblSystemStatus.setStyle("-fx-text-fill: #F44336;"); // Red color for error
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    private void loadUserData() {
        baseData.setAll(userDAO.getAllUsers());
    }

    private void applyFilter() {
        String q = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String rol = cbRol.getValue();
        String est = cbEstado.getValue();

        filtered.setPredicate(u -> {
            boolean qOk = q.isEmpty()
                    || u.nombre().toLowerCase().contains(q)
                    || u.usuario().toLowerCase().contains(q);
            boolean rolOk = rol == null || rol.equals("Todos los roles") || Objects.equals(rol, u.rol());
            boolean estOk = est == null || est.equals("Todos los estados") || Objects.equals(est, u.estado());
            return qOk && rolOk && estOk;
        });
    }

    private HBox buildActions(UserRow row) {
        Button btnEdit = new Button("✏");
        Button btnPwd  = new Button("🔑");
        Button btnDel  = new Button("🗑");

        btnEdit.getStyleClass().add("ghostBtn");
        btnPwd.getStyleClass().add("ghostBtn");
        btnDel.getStyleClass().add("ghostBtn");

        btnEdit.setOnAction(e -> System.out.println("Editar: " + row.nombre()));
        btnPwd.setOnAction(e -> System.out.println("Credenciales: " + row.nombre()));
        btnDel.setOnAction(e -> System.out.println("Eliminar: " + row.nombre()));

        HBox box = new HBox(6, btnEdit, btnPwd, btnDel);
        return box;
    }

    private void onNuevoUsuario() {
        // Base: por ahora log. Luego podrás abrir tu modal de creación.
        System.out.println("Nuevo Usuario… (abrir modal)");
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
    public record UserRow(String nombre,
                          String usuario,
                          String rol,
                          String estado,
                          LocalDateTime ultimoAcceso) {}
}
