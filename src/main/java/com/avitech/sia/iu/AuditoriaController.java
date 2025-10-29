package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.AuditoriaDAO;
import com.avitech.sia.db.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuditoriaController {

    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO(); // To get user names for display

    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbUsuario;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    @FXML private TableView<AuditoriaDAO.AuditoriaRecord> tblAuditoria;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colFecha;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colUsuario;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colAccion;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colModulo;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colDetalle;
    @FXML private TableColumn<AuditoriaDAO.AuditoriaRecord, String> colReferencia;

    private final ObservableList<AuditoriaDAO.AuditoriaRecord> masterData = FXCollections.observableArrayList();
    private FilteredList<AuditoriaDAO.AuditoriaRecord> filteredData;
    private SortedList<AuditoriaDAO.AuditoriaRecord> sortedData;

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Caches para id<->usuario
    private final Map<Integer, String> userNameById = new HashMap<>();
    private final Map<String, Integer> userIdByName = new HashMap<>();

    @FXML
    private void initialize() {
        // Estado/header (si hay usuario logueado)
        if (App.currentUser != null) {
            lblHeader.setText(App.currentUser.rol());
            lblUserInfo.setText(App.currentUser.usuario());
        } else {
            lblHeader.setText("Administrador");
            lblUserInfo.setText("Administrador");
        }
        // Nota: lblSystemStatus puede actualizarse según tu lógica de conexión
        if (lblSystemStatus != null && (lblSystemStatus.getText() == null || lblSystemStatus.getText().isBlank())) {
            lblSystemStatus.setText("Sistema Offline – MySQL Local");
        }

        // Cargar usuarios a cache/mapas y combo
        populateUsersAndComboBox();

        // Initialize TableColumns
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().fecha().format(DF)));
        colUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(
                Optional.ofNullable(userNameById.get(cellData.getValue().idUsuario())).orElse("Desconocido")));
        colAccion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().accion()));
        colModulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().modulo()));
        colDetalle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().detalle()));
        colReferencia.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().referencia()));

        // Listas filtradas y ordenadas
        filteredData = new FilteredList<>(masterData, r -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblAuditoria.comparatorProperty());
        tblAuditoria.setItems(sortedData);

        // Listeners de filtros
        tfSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        cbUsuario.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        dpFechaInicio.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        dpFechaFin.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Cargar datos
        loadAuditData();
    }

    @FXML
    private void loadAuditData() {
        try {
            masterData.setAll(auditoriaDAO.getAll());
            applyFilters();
        } catch (Exception e) {
            showError("Error al cargar datos de auditoría", e);
        }
    }

    // Handler alternativo con ActionEvent por compatibilidad FXML
    @FXML
    private void loadAuditData(javafx.event.ActionEvent e) {
        loadAuditData();
    }

    private void populateUsersAndComboBox() {
        try {
            userNameById.clear();
            userIdByName.clear();

            var usuarios = usuarioDAO.getAll();
            for (UsuarioDAO.Usuario u : usuarios) {
                userNameById.put(u.id(), u.usuario());
                userIdByName.put(u.usuario(), u.id());
            }
            List<String> items = new ArrayList<>();
            items.add("Todos los usuarios");
            items.addAll(userIdByName.keySet().stream().sorted().toList());
            cbUsuario.setItems(FXCollections.observableArrayList(items));
            cbUsuario.getSelectionModel().selectFirst();
        } catch (Exception e) {
            showError("Error al cargar usuarios para el filtro", e);
            // fallback mínimo
            cbUsuario.setItems(FXCollections.observableArrayList("Todos los usuarios"));
            cbUsuario.getSelectionModel().selectFirst();
        }
    }

    private void applyFilters() {
        String q = tfSearch.getText() != null ? tfSearch.getText().trim().toLowerCase() : "";
        String selectedUser = cbUsuario.getSelectionModel().getSelectedItem();
        LocalDate fi = dpFechaInicio.getValue();
        LocalDate ff = dpFechaFin.getValue();

        Integer userIdFilter = null;
        if (selectedUser != null && !selectedUser.isBlank() && !selectedUser.equals("Todos los usuarios")) {
            userIdFilter = userIdByName.get(selectedUser);
        }

        final Integer finalUserIdFilter = userIdFilter;
        final LocalDateTime fromDateTime = (fi != null) ? fi.atStartOfDay() : null;
        final LocalDateTime toDateTime = (ff != null) ? ff.atTime(LocalTime.MAX) : null;

        filteredData.setPredicate(r -> {
            if (r == null) return false;

            // Usuario
            if (finalUserIdFilter != null && r.idUsuario() != finalUserIdFilter) return false;

            // Rango de fechas
            LocalDateTime f = r.fecha();
            if (fromDateTime != null && f.isBefore(fromDateTime)) return false;
            if (toDateTime != null && f.isAfter(toDateTime)) return false;

            // Texto (accion, modulo, detalle, referencia, usuario)
            if (!q.isEmpty()) {
                String usuarioNombre = Optional.ofNullable(userNameById.get(r.idUsuario())).orElse("").toLowerCase();
                String accion = Optional.ofNullable(r.accion()).orElse("").toLowerCase();
                String modulo = Optional.ofNullable(r.modulo()).orElse("").toLowerCase();
                String detalle = Optional.ofNullable(r.detalle()).orElse("").toLowerCase();
                String referencia = Optional.ofNullable(r.referencia()).orElse("").toLowerCase();
                String fechaStr = r.fecha().format(DF).toLowerCase();

                boolean match = usuarioNombre.contains(q)
                        || accion.contains(q)
                        || modulo.contains(q)
                        || detalle.contains(q)
                        || referencia.contains(q)
                        || fechaStr.contains(q);
                if (!match) return false;
            }

            return true;
        });
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
    @FXML private void goAudit()      { /* Already here */ }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }
    @FXML private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }
}
