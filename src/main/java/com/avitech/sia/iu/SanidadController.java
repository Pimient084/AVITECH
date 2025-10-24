package com.avitech.sia.iu;


import com.avitech.sia.iu.sanidad.RegAplicacionController;
import com.avitech.sia.iu.sanidad.RegEventoController;
import com.avitech.sia.iu.sanidad.dto.AplicacionDTO;
import com.avitech.sia.iu.sanidad.dto.EventoDTO;

import com.avitech.sia.App;
import com.avitech.sia.DBUtil; // Import DBUtil
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;

import java.sql.Connection; // Import Connection
import java.sql.SQLException; // Import SQLException
import java.time.LocalDate;

public class SanidadController {

    // Top / sidebar
    @FXML private Label lblSystemStatus, lblHeader, lblUserInfo;

    // KPIs
    @FXML private Label kpiAplicaciones, kpiAplicacionesDelta, kpiMortalidad,
            kpiCasosActivos, kpiMedStock, kpiMedBajos;

    // Planes sanitarios
    @FXML private TableView<PlanRow> tblPlanes;
    @FXML private TableColumn<PlanRow, String> colPlan, colDesc, colEdad, colEstadoPlan;

    // Medicamentos
    @FXML private TableView<MedRow> tblMedicamentos;
    @FXML private TableColumn<MedRow, String> colMed, colStock, colInv;
    @FXML private TableColumn<MedRow, Double> colBar;

    // Filtros
    @FXML private TextField  txtBuscar;
    @FXML private ComboBox<String> cbGalpon, cbMedicamento;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Button btnRegistrarAplicacion;
    @FXML private Button btnRegistrarEvento;

    @FXML
    private void initialize() {
        // Estado cabecera (puedes traerlo de config)
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");
        
        checkDatabaseConnection(); // Check DB connection on initialization

        // ------ KPIs demo ------
        kpiAplicaciones.setText("3");
        kpiAplicacionesDelta.setText("2 completados");
        kpiMortalidad.setText("3");
        kpiCasosActivos.setText("0");
        kpiMedStock.setText("3");
        kpiMedBajos.setText("1 bajo");

        // ------ Planes ------
        colPlan.setCellValueFactory(d -> d.getValue().planProperty());
        colDesc.setCellValueFactory(d -> d.getValue().descProperty());
        colEdad.setCellValueFactory(d -> d.getValue().edadProperty());
        colEstadoPlan.setCellValueFactory(d -> d.getValue().estadoProperty());

        ObservableList<PlanRow> planes = FXCollections.observableArrayList(
                new PlanRow("Programa Vacunación Ponedoras",
                        "Aplicar según edad y cronograma", "7–72 semanas", "Preventivo"),
                new PlanRow("Tratamiento Respiratorio",
                        "Síntomas respiratorios y recuperación", "Todos", "Curativo")
        );
        tblPlanes.setItems(planes);

        // ------ Medicamentos ------
        colMed.setCellValueFactory(d -> d.getValue().nombreProperty());
        colStock.setCellValueFactory(d -> d.getValue().stockTextoProperty());
        colInv.setCellValueFactory(d -> d.getValue().inventarioProperty());
        colBar.setCellValueFactory(d -> d.getValue().nivelProperty().asObject());
        colBar.setCellFactory(ProgressBarTableCell.forTableColumn()); // barra verde

        ObservableList<MedRow> meds = FXCollections.observableArrayList(
                new MedRow("Vacuna Newcastle", 25, 0.75, "Ver en Inventario"),
                new MedRow("Antibiótico Respiratorio", 8, 0.18, "Ver en Inventario"), // bajo
                new MedRow("Vitamina E+Selenio", 12, 0.50, "Ver en Inventario")
        );
        tblMedicamentos.setItems(meds);

        // ------ Filtros (demo) ------
        cbGalpon.setItems(FXCollections.observableArrayList("Todos", "Galpón 1", "Galpón 2", "Galpón 3"));
        cbGalpon.getSelectionModel().selectFirst();
        cbMedicamento.setItems(FXCollections.observableArrayList("Todos", "Vacuna", "Antibiótico", "Vitamina"));
        cbMedicamento.getSelectionModel().selectFirst();
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
    }

    /* ================= Database Connection Check ================= */
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

    /* ================= Acciones ================= */

    @FXML
    private void onRegistrarAplicacion() {
        // defensivo: log si el botón no está inyectado
        // System.out.println("btnRegistrarAplicacion = " + btnRegistrarAplicacion);

        RegAplicacionController ctrl = ModalUtil.openModal(
                btnRegistrarAplicacion,
                "/fxml/sanidad/modal_reg_aplicacion.fxml",
                "Registrar Aplicación de Medicamento"
        );

        if (ctrl != null) {
            AplicacionDTO dto = ctrl.getResult();
            if (dto != null) {
                System.out.println("Aplicación guardada: " + dto);
                // TODO: persistir y refrescar listas/KPIs
            }
        }
    }

    @FXML
    private void onRegistrarEvento() {
        RegEventoController ctrl = ModalUtil.openModal(
                btnRegistrarEvento,
                "/fxml/sanidad/modal_reg_evento.fxml",
                "Registrar Evento Sanitario"
        );

        if (ctrl != null) {
            EventoDTO dto = ctrl.getResult();
            if (dto != null) {
                System.out.println("Evento guardado: " + dto);
                // TODO: persistir y refrescar listas/KPIs
            }
        }
    }

    @FXML private void onBuscar() {
        // TODO: aplicar filtros contra BD
        System.out.printf("Buscar: q=%s, galpón=%s, med=%s, desde=%s, hasta=%s%n",
                txtBuscar.getText(), cbGalpon.getValue(), cbMedicamento.getValue(),
                dpDesde.getValue(), dpHasta.getValue());
    }

    @FXML private void onLimpiar() {
        txtBuscar.clear();
        cbGalpon.getSelectionModel().selectFirst();
        cbMedicamento.getSelectionModel().selectFirst();
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        // TODO: recargar tabla completa
    }

    /* ================= Navegación ================= */

    @FXML private void goDashboard()  { App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/suministros.fxml",     "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { /* ya aquí */ }
    @FXML private void goProduction() { App.goTo("/fxml/produccion.fxml",      "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml",        "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml",         "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml",       "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml",      "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml",        "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml",       "SIA Avitech — Respaldos"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml",           "SIA Avitech — LOGIN"); }

    /* ================= Row models ================= */

    public static class PlanRow {
        private final StringProperty plan = new SimpleStringProperty();
        private final StringProperty desc = new SimpleStringProperty();
        private final StringProperty edad = new SimpleStringProperty();
        private final StringProperty estado = new SimpleStringProperty();
        public PlanRow(String p, String d, String e, String s) { plan.set(p); desc.set(d); edad.set(e); estado.set(s); }
        public StringProperty planProperty()   { return plan; }
        public StringProperty descProperty()   { return desc; }
        public StringProperty edadProperty()   { return edad; }
        public StringProperty estadoProperty() { return estado; }
    }

    public static class MedRow {
        private final StringProperty nombre = new SimpleStringProperty();
        private final IntegerProperty stock = new SimpleIntegerProperty();
        private final DoubleProperty  nivel = new SimpleDoubleProperty(); // 0..1
        private final StringProperty inventario = new SimpleStringProperty();
        public MedRow(String n, int s, double pct, String inv) { nombre.set(n); stock.set(s); nivel.set(pct); inventario.set(inv); }
        public StringProperty nombreProperty() { return nombre; }
        public StringProperty stockTextoProperty() { return new SimpleStringProperty(String.valueOf(stock.get()) + " frascos"); }
        public DoubleProperty  nivelProperty()  { return nivel; }
        public StringProperty inventarioProperty() { return inventario; }
    }
}
