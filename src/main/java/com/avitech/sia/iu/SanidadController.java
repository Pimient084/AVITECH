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
import com.avitech.sia.iu.sanidad.PlanSanitarioFormController;
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
import javafx.event.ActionEvent;
import com.avitech.sia.db.PlanCatalog;
import com.avitech.sia.db.PlanCatalogDAO;
import java.sql.SQLException;

public class SanidadController {

    // DAOs
    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final GalponDAO galponDAO = new GalponDAO();
    private final PlanSanitarioDAO planSanitarioDAO = new PlanSanitarioDAO();
    private final PlanCatalogDAO planCatalogDAO = new PlanCatalogDAO();

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

        // ------ Planes (desde BD) ------
        loadPlanes();

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

    private boolean isDuplicateError(Exception ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException sqlEx) {
                String state = sqlEx.getSQLState();
                String msg = String.valueOf(sqlEx.getMessage()).toLowerCase();
                if ("23000".equals(state) || msg.contains("duplicate")) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
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
            App.configureFullScreen(dialogStage);

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
            App.configureFullScreen(dialogStage);

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
            App.configureFullScreen(dialogStage);

            RegMedicamentoController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            Medicamento newMedicamento = controller.getResult();
            if (newMedicamento != null) {
                try {
                    medicamentoDAO.insert(newMedicamento);
                    loadMedicamentos(); // Refresh table
                    loadFilterCombos(); // Refresh combo box if new meds added
                    loadKpis();         // Refresh KPIs
                } catch (Exception e) {
                    if (isDuplicateError(e)) {
                        new Alert(Alert.AlertType.WARNING, "Ya existe un medicamento con ese nombre. Cambia el nombre e intenta otra vez.").showAndWait();
                    } else {
                        showError("Error al registrar medicamento", e);
                    }
                }
            }
        } catch (Exception e) {
            showError("Error al registrar medicamento", e);
        }
    }

    private PlanRow getSelectedPlan() {
        return tblPlanes.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onEditarPlanSanitario() {
        PlanRow selected = getSelectedPlan();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona un plan para editar.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Sanidad/modal_plan_sanitario.fxml"));
            Pane page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Plan Sanitario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblPlanes.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            App.configureFullScreen(dialogStage);

            PlanSanitarioFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setInitial(selected.planProperty().get(), selected.descProperty().get(), selected.edadProperty().get(), selected.estadoProperty().get());

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                PlanCatalog edit = new PlanCatalog(
                        selected.getId(),
                        controller.getPlan(),
                        controller.getDesc(),
                        controller.getEdad(),
                        controller.getEstado()
                );
                try {
                    planCatalogDAO.update(edit);
                } catch (Exception ex) {
                    if (isDuplicateError(ex)) {
                        new Alert(Alert.AlertType.WARNING, "Ya existe un plan con ese nombre. Cambia el nombre e intenta otra vez.").showAndWait();
                    } else {
                        showError("Error al actualizar el plan sanitario", ex);
                    }
                    return;
                }
                loadPlanes();
            }
        } catch (Exception e) {
            showError("Error al editar plan sanitario", e);
        }
    }

    @FXML
    private void onEliminarPlanSanitario() {
        PlanRow selected = getSelectedPlan();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona un plan para eliminar.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el plan '" + selected.planProperty().get() + "'?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirmar eliminación");
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.OK) return;
        try {
            planCatalogDAO.deleteById(selected.getId());
            loadPlanes();
        } catch (Exception e) {
            showError("Error al eliminar plan sanitario", e);
        }
    }

    private void loadPlanes() {
        try {
            List<PlanCatalog> planesBD = planCatalogDAO.listAll();
            ObservableList<PlanRow> rows = FXCollections.observableArrayList(
                    planesBD.stream().map(p -> new PlanRow(
                            p.getId(),
                            p.getNombre(),
                            p.getDescripcion(),
                            p.getEdad(),
                            p.getEstado()
                    )).collect(Collectors.toList())
            );
            tblPlanes.setItems(rows);
        } catch (Exception e) {
            // Si hay error (tabla no existe o BD vacía), inicializamos lista vacía y mostramos aviso
            tblPlanes.setItems(FXCollections.observableArrayList());
            showError("No se pudieron cargar los planes sanitarios", e);
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

    @FXML
    public void onAgregarPlanSanitario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Sanidad/modal_plan_sanitario.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Agregar Plan Sanitario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tblPlanes.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            App.configureFullScreen(dialogStage);

            PlanSanitarioFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                // Persistir en BD
                PlanCatalog nuevo = new PlanCatalog(
                        controller.getPlan(),
                        controller.getDesc(),
                        controller.getEdad(),
                        controller.getEstado()
                );
                try {
                    planCatalogDAO.insert(nuevo);
                } catch (Exception ex) {
                    if (isDuplicateError(ex)) {
                        new Alert(Alert.AlertType.WARNING, "Ya existe un plan con ese nombre. Cambia el nombre e intenta otra vez.").showAndWait();
                    } else {
                        showError("Error al guardar el plan sanitario", ex);
                    }
                    return;
                }
                // Refrescar tabla desde BD para mantener consistencia
                loadPlanes();
            }
        } catch (Exception e) {
            showError("Error al agregar plan sanitario", e);
        }
    }

    // Sobre-carga sin argumentos para validadores FXML estrictos
    @FXML
    public void onAgregarPlanSanitario() {
        onAgregarPlanSanitario(null);
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
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty plan = new SimpleStringProperty();
        private final StringProperty desc = new SimpleStringProperty();
        private final StringProperty edad = new SimpleStringProperty();
        private final StringProperty estado = new SimpleStringProperty();

        public PlanRow(int id, String p, String d, String e, String s) {
            this.id.set(id);
            plan.set(p);
            desc.set(d);
            edad.set(e);
            estado.set(s);
        }

        public int getId() { return id.get(); }
        public IntegerProperty idProperty() { return id; }
        public StringProperty planProperty() { return plan; }
        public StringProperty descProperty() { return desc; }
        public StringProperty edadProperty() { return edad; }
        public StringProperty estadoProperty() { return estado; }
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
