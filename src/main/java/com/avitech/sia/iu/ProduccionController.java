package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.GalponDAO;
import com.avitech.sia.db.LoteDAO;
import com.avitech.sia.db.Produccion;
import com.avitech.sia.db.ProduccionDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProduccionController {

    // DAOs
    private final ProduccionDAO produccionDAO = new ProduccionDAO();
    private final GalponDAO galponDAO = new GalponDAO();
    private final LoteDAO loteDAO = new LoteDAO();

    // Model
    private final ObservableList<ProduccionRow> masterData = FXCollections.observableArrayList();
    private Map<Integer, String> galponMap;

    // KPIs
    @FXML
    private Label lblKpiProduccionTotal, lblKpiPosturaPromedio, lblKpiPesoPromedio, lblKpiMortalidadTotal;
    @FXML
    private Label lblKpiProdDelta, lblKpiPosturaDelta, lblKpiPesoDelta, lblKpiMortalidadDelta;

    // Table
    @FXML
    private TableView<ProduccionRow> tblDiario;
    @FXML
    private TableColumn<ProduccionRow, String> colGalpon, colClasif;
    @FXML
    private TableColumn<ProduccionRow, Number> colAves, colProduccion, colPostura, colPeso, colMortalidad;

    // Filters
    @FXML
    private ComboBox<String> cbFiltroGalpon;

    // Form
    @FXML
    private ComboBox<String> cbGalpon;
    @FXML
    private TextField tfProduccionTotal, tfPesoPromedio, tfMortalidad, tfHuevosB, tfHuevosA, tfHuevosAA, tfHuevosAAA;
    @FXML
    private Button btnGestionarLotes;

    @FXML
    public void initialize() {
        // Load data
        try {
            galponMap = galponDAO.getGalponMap();
            loadProduccionData();
        } catch (Exception e) {
            showError("Error al cargar datos iniciales", e);
            return;
        }

        // Setup UI
        setupTableColumns();
        populateFilterComboBox();
        populateFormComboBox();
        updateKpis();

        // Set initial state
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");
    }

    private void setupTableColumns() {
        colGalpon.setCellValueFactory(cellData -> new SimpleStringProperty(galponMap.get(cellData.getValue().getGalponId())));
        colAves.setCellValueFactory(cellData -> new SimpleIntegerProperty(0)); // Placeholder
        colProduccion.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalHuevos()));
        colPostura.setCellValueFactory(cellData -> new SimpleIntegerProperty(0)); // Placeholder
        colPeso.setCellValueFactory(cellData -> new SimpleIntegerProperty(0)); // Placeholder
        colClasif.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClasificacion()));
        colMortalidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getMortalidad()));
    }

    private void loadProduccionData() throws Exception {
        List<Produccion> produccionList = produccionDAO.getAll();
        masterData.setAll(produccionList.stream().map(ProduccionRow::new).collect(Collectors.toList()));
        tblDiario.setItems(masterData);
    }

    private void populateFilterComboBox() {
        ObservableList<String> galpones = FXCollections.observableArrayList("Todos los galpones");
        galpones.addAll(galponMap.values());
        cbFiltroGalpon.setItems(galpones);
        cbFiltroGalpon.getSelectionModel().selectFirst();
    }

    private void populateFormComboBox() {
        cbGalpon.setItems(FXCollections.observableArrayList(galponMap.values()));
    }

    private void updateKpis() {
        int totalHuevos = masterData.stream().mapToInt(ProduccionRow::getTotalHuevos).sum();
        int totalMortalidad = masterData.stream().mapToInt(ProduccionRow::getMortalidad).sum();

        lblKpiProduccionTotal.setText(String.format("%,d", totalHuevos));
        lblKpiMortalidadTotal.setText(String.valueOf(totalMortalidad));

        // Placeholder KPIs
        lblKpiPosturaPromedio.setText("85%");
        lblKpiPesoPromedio.setText("62g");
        lblKpiProdDelta.setText("+1.2%");
        lblKpiPosturaDelta.setText("-0.5%");
        lblKpiPesoDelta.setText("+0.1g");
        lblKpiMortalidadDelta.setText(totalMortalidad + " aves");
    }

    @FXML
    private void onGuardarRegistro() {
        try {
            // Validation
            if (cbGalpon.getValue() == null || tfProduccionTotal.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "El galpón y la producción total son obligatorios.").showAndWait();
                return;
            }

            // Create new Produccion object
            Produccion newProduccion = new Produccion(
                    0,
                    LocalDate.now(),
                    galponMap.entrySet().stream().filter(entry -> entry.getValue().equals(cbGalpon.getValue())).map(Map.Entry::getKey).findFirst().orElse(-1),
                    Integer.parseInt(tfProduccionTotal.getText()),
                    parseTextField(tfHuevosAA) + parseTextField(tfHuevosAAA), // L
                    parseTextField(tfHuevosA), // M
                    parseTextField(tfHuevosB), // S
                    0, 0, // temperatura, humedad placeholders
                    parseTextField(tfMortalidad),
                    "admin" // Placeholder for responsable
            );

            // Insert into DB
            produccionDAO.insert(newProduccion);

            // Refresh UI
            loadProduccionData();
            updateKpis();
            clearForm();

        } catch (Exception e) {
            showError("Error al guardar el registro", e);
        }
    }

    @FXML
    private void onCancelarRegistro() {
        clearForm();
    }

    @FXML
    private void onFiltrar() {
        String selectedGalpon = cbFiltroGalpon.getValue();
        if (selectedGalpon == null || selectedGalpon.equals("Todos los galpones")) {
            tblDiario.setItems(masterData);
        } else {
            ObservableList<ProduccionRow> filteredData = masterData.filtered(row -> galponMap.get(row.getGalponId()).equals(selectedGalpon));
            tblDiario.setItems(filteredData);
        }
    }

    @FXML
    private void onLimpiarFiltro() {
        cbFiltroGalpon.getSelectionModel().selectFirst();
        tblDiario.setItems(masterData);
    }

    @FXML
    private void onGestionarLotes() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_lote.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Gestionar Lotes");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnGestionarLotes.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegLoteController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            // Optionally pass existing lote data if editing

            dialogStage.showAndWait();

            // After modal closes, refresh relevant data
            galponMap = galponDAO.getGalponMap(); // Refresh galpon map
            loadProduccionData(); // Reload production data
            populateFilterComboBox(); // Repopulate filter combo
            populateFormComboBox(); // Repopulate form combo
            updateKpis(); // Update KPIs

        } catch (IOException e) {
            showError("Error al abrir el diálogo de lotes", e);
        } catch (Exception e) {
            showError("Error al gestionar lotes", e);
        }
    }

    private void clearForm() {
        cbGalpon.getSelectionModel().clearSelection();
        tfProduccionTotal.clear();
        tfPesoPromedio.clear();
        tfMortalidad.clear();
        tfHuevosB.clear();
        tfHuevosA.clear();
        tfHuevosAA.clear();
        tfHuevosAAA.clear();
    }

    private int parseTextField(TextField tf) {
        return tf.getText().isEmpty() ? 0 : Integer.parseInt(tf.getText());
    }

    private void showError(String header, Exception e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, header + ": " + e.getMessage()).showAndWait();
    }

    /* ================== Row Model ================== */
    public static class ProduccionRow {
        private final int galponId;
        private final int totalHuevos;
        private final int huevosL;
        private final int huevosM;
        private final int huevosS;
        private final int mortalidad;

        public ProduccionRow(Produccion p) {
            this.galponId = p.getGalpon();
            this.totalHuevos = p.getTotalHuevos();
            this.huevosL = p.getHuevosL();
            this.huevosM = p.getHuevosM();
            this.huevosS = p.getHuevosS();
            this.mortalidad = p.getMortalidad();
        }

        public int getGalponId() {
            return galponId;
        }

        public int getTotalHuevos() {
            return totalHuevos;
        }

        public int getMortalidad() {
            return mortalidad;
        }

        public String getClasificacion() {
            return String.format("L: %d, M: %d, S: %d", huevosL, huevosM, huevosS);
        }
    }

    /* ================== Navigation ================== */
    @FXML private Label lblSystemStatus, lblHeader, lblUserInfo;
    @FXML private void goDashboard()  { App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/suministros.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/sanidad.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { /* Already here */ }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml", "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml", "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml", "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión"); }
}
