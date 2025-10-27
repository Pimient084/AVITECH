package com.avitech.sia.iu.oper;

import com.avitech.sia.App;
import com.avitech.sia.db.GalponDAO;
import com.avitech.sia.db.Produccion;
import com.avitech.sia.db.oper.ProduccionOperDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProduccionOperController {

    private static final Logger LOG = Logger.getLogger(ProduccionOperController.class.getName());

    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    @FXML private ComboBox<String> cbGalpon;
    @FXML private TextField tfProduccionTotal;
    @FXML private Button btnGuardarRegistro, btnCancelarRegistro;
    @FXML private TableView<ProduccionRow> tblDiario;

    // KPIs
    @FXML private Label lblKpiProduccionTotal, lblKpiProdDelta;
    @FXML private Label lblKpiPosturaPromedio, lblKpiPosturaDelta;
    @FXML private Label lblKpiPesoPromedio, lblKpiPesoDelta;
    @FXML private Label lblKpiMortalidadTotal, lblKpiMortalidadDelta;

    @FXML private ComboBox<String> cbFiltroGalpon;

    // Form fields (optional)
    @FXML private TextField tfPesoPromedio, tfMortalidad, tfHuevosB, tfHuevosA, tfHuevosAA, tfHuevosAAA;
    @FXML private Button btnGestionarLotes;

    // Columns (defined in FXML)
    @FXML private TableColumn<ProduccionRow, String> colGalpon, colClasif;
    @FXML private TableColumn<ProduccionRow, Number> colAves, colProduccion, colPostura, colPeso, colMortalidad;

    private final ProduccionOperDAO produccionDAO = new ProduccionOperDAO();
    private final GalponDAO galponDAO = new GalponDAO();

    private final ObservableList<ProduccionRow> masterData = FXCollections.observableArrayList();
    private Map<Integer, String> galponMap;

    @FXML
    private void initialize() {
        if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Online — MySQL Local");
        if (lblHeader != null) lblHeader.setText("Operador");
        if (lblUserInfo != null) lblUserInfo.setText("Operador");

        try {
            galponMap = galponDAO.getGalponMap();
            loadProduccionData();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cargar datos iniciales de producción", e);
            if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Offline – Error de BD");
            new Alert(Alert.AlertType.ERROR, "Error al cargar datos iniciales: " + e.getMessage()).showAndWait();
            return;
        }

        setupTableColumns();
        populateFilterComboBox();
        populateFormComboBox();
        updateKpis();
    }

    private void setupTableColumns() {
        if (colGalpon != null) colGalpon.setCellValueFactory(cellData -> new SimpleStringProperty(galponMap.get(cellData.getValue().getGalponId())));
        if (colAves != null) colAves.setCellValueFactory(cellData -> new SimpleIntegerProperty(0)); // placeholder
        if (colProduccion != null) colProduccion.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalHuevos()));
        if (colPostura != null) colPostura.setCellValueFactory(cellData -> new SimpleIntegerProperty(0));
        if (colPeso != null) colPeso.setCellValueFactory(cellData -> new SimpleIntegerProperty(0));
        if (colClasif != null) colClasif.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClasificacion()));
        if (colMortalidad != null) colMortalidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getMortalidad()));

        if (tblDiario != null) tblDiario.setItems(masterData);
    }

    private void loadProduccionData() throws Exception {
        List<Produccion> produccionList = produccionDAO.getAll();
        masterData.setAll(produccionList.stream().map(ProduccionRow::new).collect(Collectors.toList()));
    }

    private void populateFilterComboBox() {
        if (cbFiltroGalpon == null) return;
        ObservableList<String> galpones = FXCollections.observableArrayList("Todos los galpones");
        galpones.addAll(galponMap.values());
        cbFiltroGalpon.setItems(galpones);
        cbFiltroGalpon.getSelectionModel().selectFirst();
    }

    private void populateFormComboBox() {
        if (cbGalpon == null) return;
        cbGalpon.setItems(FXCollections.observableArrayList(galponMap.values()));
        if (!galponMap.isEmpty()) cbGalpon.getSelectionModel().selectFirst();
    }

    private void updateKpis() {
        int totalHuevos = masterData.stream().mapToInt(ProduccionRow::getTotalHuevos).sum();
        int totalMortalidad = masterData.stream().mapToInt(ProduccionRow::getMortalidad).sum();

        if (lblKpiProduccionTotal != null) lblKpiProduccionTotal.setText(String.format("%,d", totalHuevos));
        if (lblKpiMortalidadTotal != null) lblKpiMortalidadTotal.setText(String.valueOf(totalMortalidad));

        if (lblKpiPosturaPromedio != null) lblKpiPosturaPromedio.setText("85%");
        if (lblKpiPesoPromedio != null) lblKpiPesoPromedio.setText("62g");
        if (lblKpiProdDelta != null) lblKpiProdDelta.setText("+1.2%");
        if (lblKpiPosturaDelta != null) lblKpiPosturaDelta.setText("-0.5%");
        if (lblKpiPesoDelta != null) lblKpiPesoDelta.setText("+0.1g");
        if (lblKpiMortalidadDelta != null) lblKpiMortalidadDelta.setText(totalMortalidad + " aves");
    }

    @FXML
    private void onGuardarRegistro() {
        try {
            if (cbGalpon == null || cbGalpon.getValue() == null || tfProduccionTotal == null || tfProduccionTotal.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "El galpón y la producción total son obligatorios.").showAndWait();
                return;
            }

            // Convertir nombre de galpón a id
            int galponId = galponMap.entrySet().stream().filter(entry -> entry.getValue().equals(cbGalpon.getValue())).map(Map.Entry::getKey).findFirst().orElse(-1);

            Produccion newProduccion = new Produccion(
                    0,
                    LocalDate.now(),
                    galponId,
                    Integer.parseInt(tfProduccionTotal.getText()),
                    parseTextField(tfHuevosAA) + parseTextField(tfHuevosAAA),
                    parseTextField(tfHuevosA),
                    parseTextField(tfHuevosB),
                    0, 0,
                    parseTextField(tfMortalidad),
                    "operador"
            );

            produccionDAO.insert(newProduccion);

            // Refresh
            loadProduccionData();
            updateKpis();
            clearForm();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar registro de producción", e);
            new Alert(Alert.AlertType.ERROR, "Error al guardar el registro: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onCancelarRegistro() {
        clearForm();
    }

    @FXML
    private void onFiltrar() {
        String selectedGalpon = cbFiltroGalpon == null ? null : cbFiltroGalpon.getValue();
        if (selectedGalpon == null || selectedGalpon.equals("Todos los galpones")) {
            if (tblDiario != null) tblDiario.setItems(masterData);
        } else {
            ObservableList<ProduccionRow> filteredData = masterData.filtered(row -> galponMap.get(row.getGalponId()).equals(selectedGalpon));
            if (tblDiario != null) tblDiario.setItems(filteredData);
        }
    }

    @FXML
    private void onLimpiarFiltro() {
        if (cbFiltroGalpon != null) cbFiltroGalpon.getSelectionModel().selectFirst();
        if (tblDiario != null) tblDiario.setItems(masterData);
    }

    @FXML
    private void onGestionarLotes() {
        new Alert(Alert.AlertType.INFORMATION, "Gestionar Lotes - no implementado").showAndWait();
    }

    private void clearForm() {
        if (cbGalpon != null) cbGalpon.getSelectionModel().clearSelection();
        if (tfProduccionTotal != null) tfProduccionTotal.clear();
        if (tfPesoPromedio != null) tfPesoPromedio.clear();
        if (tfMortalidad != null) tfMortalidad.clear();
        if (tfHuevosB != null) tfHuevosB.clear();
        if (tfHuevosA != null) tfHuevosA.clear();
        if (tfHuevosAA != null) tfHuevosAA.clear();
        if (tfHuevosAAA != null) tfHuevosAAA.clear();
    }

    private int parseTextField(TextField tf) { return tf == null || tf.getText().isEmpty() ? 0 : Integer.parseInt(tf.getText()); }

    /* Row model */
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

        public int getGalponId() { return galponId; }
        public int getTotalHuevos() { return totalHuevos; }
        public int getMortalidad() { return mortalidad; }
        public String getClasificacion() { return String.format("L: %d, M: %d, S: %d", huevosL, huevosM, huevosS); }
    }

    @FXML private void goDashboard()  { App.goTo("/fxml/oper/dashboard_oper.fxml", "SIA Avitech — Operador"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/oper/suministros_oper.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/oper/sanidad_oper.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { /* ya estás aquí */ }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión"); }
}
