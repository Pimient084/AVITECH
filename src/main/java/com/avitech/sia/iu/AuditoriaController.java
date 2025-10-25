package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.AuditoriaDAO;
import com.avitech.sia.db.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Administrador"); // TODO: Set actual logged-in user
        lblUserInfo.setText("Administrador"); // TODO: Set actual logged-in user

        // Initialize TableColumns
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().fecha().format(DF)));
        colUsuario.setCellValueFactory(cellData -> {
            try {
                // This is inefficient for large datasets, consider caching user names
                return new SimpleStringProperty(usuarioDAO.findById(cellData.getValue().idUsuario())
                        .map(UsuarioDAO.Usuario::usuario)
                        .orElse("Desconocido"));
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("Error");
            }
        });
        colAccion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().accion()));
        colModulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().modulo()));
        colDetalle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().detalle()));
        colReferencia.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().referencia()));

        tblAuditoria.setItems(masterData);

        loadAuditData();
        populateUserComboBox();
    }

    @FXML
    private void loadAuditData() {
        try {
            masterData.setAll(auditoriaDAO.getAll());
        } catch (Exception e) {
            showError("Error al cargar datos de auditoría", e);
        }
    }

    private void populateUserComboBox() {
        try {
            List<String> userNames = usuarioDAO.getAllNombres();
            List<String> comboBoxItems = new ArrayList<>();
            comboBoxItems.add("Todos los usuarios");
            comboBoxItems.addAll(userNames);
            cbUsuario.setItems(FXCollections.observableArrayList(comboBoxItems));
            cbUsuario.getSelectionModel().selectFirst();
        } catch (Exception e) {
            showError("Error al cargar usuarios para el filtro", e);
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
    @FXML private void goAudit()      { /* Already here */ }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }
    @FXML private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }
}
