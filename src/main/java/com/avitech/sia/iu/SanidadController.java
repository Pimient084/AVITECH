package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.GalponDAO;
import com.avitech.sia.db.Medicamento;
import com.avitech.sia.db.MedicamentoDAO;
import com.avitech.sia.db.PlanSanitarioDAO;
import com.avitech.sia.iu.sanidad.RegAplicacionController;
import com.avitech.sia.iu.sanidad.RegEventoController;
import com.avitech.sia.iu.sanidad.RegMedicamentoController;
import com.avitech.sia.iu.sanidad.dto.AplicacionDTO;
import com.avitech.sia.iu.sanidad.dto.EventoDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SanidadController {

    // DAOs
    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final GalponDAO galponDAO = new GalponDAO();
    private final PlanSanitarioDAO planSanitarioDAO = new PlanSanitarioDAO();

    // Top / sidebar
    @FXML
    private Label lblSystemStatus, lblHeader, lblUserInfo;

    // KPIs
    @FXML
    private Label kpiAplicaciones, kpiAplicacionesDelta, kpiMortalidad,
            kpiCasosActivos, kpiMedStock, kpiMedBajos;

    // Planes sanitarios
    @FXML
    private TableView<PlanRow> tblPlanes;
    @FXML
    private TableColumn<PlanRow, String> colPlan, colDesc, colEdad, colEstadoPlan;

    // Medicamentos
    @FXML
    private TableView<MedRow> tblMedicamentos;
    @FXML
    private TableColumn<MedRow, String> colMed, colStock, colInv;
    @FXML
    private TableColumn<MedRow, Double> colBar;

    // Filtros
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cbGalpon, cbMedicamento;
    @FXML
    private DatePicker dpDesde, dpHasta;
    @FXML
    private Button btnRegistrarAplicacion;
    @FXML
    private Button btnRegistrarEvento;
    @FXML
    private Button btnRegistrarMedicamento; // <--- THIS WAS MISSING

    @FXML
    private void initialize() {
        // Estado cabecera (puedes traerlo de config)
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");
        lblSystemStatus.setText("Sistema Online – MySQL Local");

        // ------ Configuración de columnas ------
        setupTableColumns();

        // ------ Carga de datos ------
        loadMedicamentos();
        loadFilterCombos();
        loadKpis();

        // ------ Planes (aún con datos dummy) ------
        ObservableList<PlanRow> planes = FXCollections.observableArrayList(
                new PlanRow("Programa Vacunación Ponedoras",
                        "Aplicar según edad y cronograma", "7–72 semanas", "Preventivo"),
                new PlanRow("Tratamiento Respiratorio",
                        "Síntomas respiratorios y recuperación", "Todos", "Curativo")
        );
        tblPlanes.setItems(planes);

        // ------ Filtros (valores iniciales) ------
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        // Planes
        colPlan.setCellValueFactory(d -> d.getValue().planProperty());
        colDesc.setCellValueFactory(d -> d.getValue().descProperty());
        colEdad.setCellValueFactory(d -> d.getValue().edadProperty());
        colEstadoPlan.setCellValueFactory(d -> d.getValue().estadoProperty());

        // Medicamentos
        colMed.setCellValueFactory(d -> d.getValue().nombreProperty());
        colStock.setCellValueFactory(d -> d.getValue().stockTextoProperty());
        colInv.setCellValueFactory(d -> d.getValue().inventarioProperty());
        colBar.setCellValueFactory(d -> d.getValue().nivelProperty().asObject());
        colBar.setCellFactory(ProgressBarTableCell.forTableColumn());
    }

    private void loadMedicamentos() {
        try {
            List<Medicamento> medicamentos = medicamentoDAO.getAll();
            ObservableList<MedRow> medRows = FXCollections.observableArrayList(
                    medicamentos.stream().map(MedRow::new).collect(Collectors.toList())
            );
            tblMedicamentos.setItems(medRows);
        } catch (Exception e) {
            showError("Error al cargar medicamentos", e);
        }
    }

    private void loadFilterCombos() {
        try {
            // Combo Galpones
            List<String> galpones = galponDAO.getActiveGalpones();
            galpones.add(0, "Todos");
            cbGalpon.setItems(FXCollections.observableArrayList(galpones));
            cbGalpon.getSelectionModel().selectFirst();

            // Combo Medicamentos
            List<String> medNombres = tblMedicamentos.getItems().stream()
                    .map(m -> m.nombre.get())
                    .sorted()
                    .collect(Collectors.toList());
            medNombres.add(0, "Todos");
            cbMedicamento.setItems(FXCollections.observableArrayList(medNombres));
            cbMedicamento.getSelectionModel().selectFirst();

        } catch (Exception e) {
            showError("Error al cargar filtros", e);
        }
    }

    private void loadKpis() {
        try {
            // KPIs de Sanidad
            kpiAplicaciones.setText(String.valueOf(planSanitarioDAO.getAplicacionesMes()));
            kpiMortalidad.setText(String.valueOf(planSanitarioDAO.getMuertesMes()));
            kpiAplicacionesDelta.setText("en los últimos 30 días");

            // KPIs de Medicamentos
            long totalMeds = tblMedicamentos.getItems().size();
            long bajos = tblMedicamentos.getItems().stream().filter(m -> m.nivel.get() < 0.25).count();
            kpiMedStock.setText(String.valueOf(totalMeds));
            kpiMedBajos.setText(bajos + " bajos");

            // Placeholder para casos activos
            kpiCasosActivos.setText("0");

        } catch (Exception e) {
            showError("Error al cargar KPIs", e);
        }
    }

    private void showError(String header, Exception e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, header + ": " + e.getMessage()).showAndWait();
    }

    /* ================= Acciones ================= */

    @FXML
    private void onRegistrarAplicacion() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/sanidad/modal_reg_aplicacion.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Aplicación de Medicamento");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnRegistrarAplicacion.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegAplicacionController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            AplicacionDTO result = controller.getResult();
            if (result != null) {
                System.out.println("Aplicación guardada: " + result);
                // TODO: persistir y refrescar listas/KPIs
            }
        } catch (IOException e) {
            showError("Error al abrir el diálogo", e);
        }
    }

    @FXML
    private void onRegistrarEvento() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/sanidad/modal_reg_evento.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Evento Sanitario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnRegistrarEvento.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegEventoController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            EventoDTO result = controller.getResult();
            if (result != null) {
                System.out.println("Evento guardado: " + result);
                // TODO: persistir y refrescar listas/KPIs
            }
        } catch (IOException e) {
            showError("Error al abrir el diálogo", e);
        }
    }

    @FXML
    private void onRegistrarMedicamento() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/sanidad/modal_reg_medicamento.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Nuevo Medicamento");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnRegistrarMedicamento.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            RegMedicamentoController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            Medicamento newMedicamento = controller.getResult();
            if (newMedicamento != null) {
                medicamentoDAO.insert(newMedicamento);
                loadMedicamentos(); // Refresh table
                loadFilterCombos(); // Refresh combo box if new meds added
                loadKpis();         // Refresh KPIs
            }
        } catch (Exception e) {
            showError("Error al registrar medicamento", e);
        }
    }

    @FXML
    private void onBuscar() {
        // TODO: aplicar filtros contra BD
        System.out.printf("Buscar: q=%s, galpón=%s, med=%s, desde=%s, hasta=%s%n",
                txtBuscar.getText(), cbGalpon.getValue(), cbMedicamento.getValue(),
                dpDesde.getValue(), dpHasta.getValue());
    }

    @FXML
    private void onLimpiar() {
        txtBuscar.clear();
        cbGalpon.getSelectionModel().selectFirst();
        cbMedicamento.getSelectionModel().selectFirst();
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        // TODO: recargar tabla completa
    }

    /* ================= Navegación ================= */

    @FXML
    private void goDashboard() {
        App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN");
    }

    @FXML
    private void goSupplies() {
        App.goTo("/fxml/suministros.fxml", "SIA Avitech — Suministros");
    }

    @FXML
    private void goHealth() { /* ya aquí */ }

    @FXML
    private void goProduction() {
        App.goTo("/fxml/produccion.fxml", "SIA Avitech — Producción");
    }

    @FXML
    private void goReports() {
        App.goTo("/fxml/reportes.fxml", "SIA Avitech — Reportes");
    }

    @FXML
    private void goAlerts() {
        App.goTo("/fxml/alertas.fxml", "SIA Avitech — Alertas");
    }

    @FXML
    private void goAudit() {
        App.goTo("/fxml/auditoria.fxml", "SIA Avitech — Auditoría");
    }

    @FXML
    private void goParams() {
        App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros");
    }

    @FXML
    private void goUsers() {
        App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios");
    }

    @FXML
    private void goBackup() {
        App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos");
    }

    @FXML
    private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — LOGIN");
    }

    /* ================= Row models ================= */

    public static class PlanRow {
        private final StringProperty plan = new SimpleStringProperty();
        private final StringProperty desc = new SimpleStringProperty();
        private final StringProperty edad = new SimpleStringProperty();
        private final StringProperty estado = new SimpleStringProperty();

        public PlanRow(String p, String d, String e, String s) {
            plan.set(p);
            desc.set(d);
            edad.set(e);
            estado.set(s);
        }

        public StringProperty planProperty() {
            return plan;
        }

        public StringProperty descProperty() {
            return desc;
        }

        public StringProperty edadProperty() {
            return edad;
        }

        public StringProperty estadoProperty() {
            return estado;
        }
    }

    public static class MedRow {
        private final StringProperty nombre = new SimpleStringProperty();
        private final IntegerProperty stock = new SimpleIntegerProperty();
        private final DoubleProperty nivel = new SimpleDoubleProperty(); // 0..1
        private final StringProperty inventario = new SimpleStringProperty("Ver Inventario");

        public MedRow(Medicamento med) {
            nombre.set(med.getNombre());
            stock.set(med.getStock());
            nivel.set(med.getStockLevel());
        }

        public StringProperty nombreProperty() {
            return nombre;
        }

        public StringProperty stockTextoProperty() {
            return new SimpleStringProperty(stock.get() + " unidades");
        }

        public DoubleProperty nivelProperty() {
            return nivel;
        }

        public StringProperty inventarioProperty() {
            return inventario;
        }
    }
}
