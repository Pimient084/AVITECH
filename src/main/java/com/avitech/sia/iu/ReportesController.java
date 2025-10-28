package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.report.PdfReportService;
import com.avitech.sia.report.ReportRequest;
import com.avitech.sia.report.ReportType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.nio.file.Path;

public class ReportesController {

    // Sidebar / topbar
    @FXML private VBox sidebar;
    @FXML private Label lblUserInfo;
    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;

    // Selector de tipo
    @FXML private ComboBox<String> cbTipoReporte;

    // Filtros
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private ComboBox<String> cbLote, cbArticulo, cbCategoria, cbResponsable;

    // Export/preview
    @FXML private RadioButton rbPdf, rbExcel;
    @FXML private CheckBox chkPreview;
    @FXML private Label lblInfoFiltros;
    @FXML private Button btnGenerar;

    // Recientes
    @FXML private TableView<?> tvRecientes;
    @FXML private TableColumn<?, ?> colRepNombre, colRepFecha, colRepTam, colRepAccion;

    // KPI/Stats
    @FXML private Label lblKpiMes, lblMasSolicitado, lblFormatos, lblTPromedio;
    @FXML private ProgressBar pbInv, pbProd, pbSan;

    // Estado interno
    private ReportType selectedType = ReportType.PRODUCCION;

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


    /* ============ Acciones principales ============ */
    @FXML private void onGenerar() {
        try {
            ReportType byCombo = mapTipo(cbTipoReporte != null ? cbTipoReporte.getValue() : null);
            if (byCombo != null) selectedType = byCombo;
            if (selectedType == null) {
                throw new IllegalStateException("Seleccione un tipo de reporte (tarjeta o combo)");
            }
            ReportRequest req = new ReportRequest(
                    selectedType,
                    dpDesde.getValue(),
                    dpHasta.getValue(),
                    cbLote.getValue(),
                    cbArticulo.getValue(),
                    cbCategoria.getValue(),
                    cbResponsable.getValue(),
                    chkPreview.isSelected()
            );

            PdfReportService service = new PdfReportService();
            Path file = service.generate(req);

            if (chkPreview.isSelected() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.toFile());
            } else {
                Alert ok = new Alert(Alert.AlertType.INFORMATION, "Reporte generado: " + file.toAbsolutePath());
                ok.setHeaderText("Reporte PDF");
                ok.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "No se pudo generar el reporte: " + e.getMessage());
            err.setHeaderText("Error en Reporte");
            err.showAndWait();
        }
    }
    @FXML private void onLimpiar() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cbLote.getSelectionModel().clearSelection();
        cbArticulo.getSelectionModel().clearSelection();
        cbCategoria.getSelectionModel().clearSelection();
        cbResponsable.getSelectionModel().clearSelection();
        cbLote.getSelectionModel().selectFirst();
        cbArticulo.getSelectionModel().selectFirst();
        cbCategoria.getSelectionModel().selectFirst();
        cbResponsable.getSelectionModel().selectFirst();
        lblInfoFiltros.setText("Filtros aplicados: —");
        if (cbTipoReporte != null) cbTipoReporte.getSelectionModel().select(tipoToText(selectedType));
    }
    @FXML private void onExportarExcel() { /* ignorado: solo PDF */ }

    /* ============ Selección de tipo de reporte (tarjetas) ============ */
    @FXML private void selSanidad()   { setType(ReportType.SANIDAD,    "Tipo seleccionado: Sanidad"); }
    @FXML private void selProduccion(){ setType(ReportType.PRODUCCION, "Tipo seleccionado: Producción"); }
    @FXML private void selAlertas()   { setType(ReportType.ALERTAS,    "Tipo seleccionado: Alertas"); }
    @FXML private void selAuditoria() { setType(ReportType.AUDITORIA,  "Tipo seleccionado: Auditoría"); }
    @FXML private void selUsuarios()  { setType(ReportType.USUARIOS,   "Tipo seleccionado: Usuarios"); }

    private void setType(ReportType t, String msg) {
        this.selectedType = t;
        lblInfoFiltros.setText(msg);
        if (cbTipoReporte != null) cbTipoReporte.getSelectionModel().select(tipoToText(t));
    }

    private void populateTipoCombo() {
        if (cbTipoReporte == null) return;
        cbTipoReporte.getItems().setAll(
                tipoToText(ReportType.SANIDAD),
                tipoToText(ReportType.PRODUCCION),
                tipoToText(ReportType.ALERTAS),
                tipoToText(ReportType.AUDITORIA),
                tipoToText(ReportType.USUARIOS)
        );
        cbTipoReporte.getSelectionModel().select(tipoToText(selectedType));
    }

    private String tipoToText(ReportType t) {
        if (t == null) return null;
        return switch (t) {
            case SANIDAD -> "Sanidad";
            case PRODUCCION -> "Producción";
            case ALERTAS -> "Alertas";
            case AUDITORIA -> "Auditoría";
            case USUARIOS -> "Usuarios";
        };
    }
    private ReportType mapTipo(String text) {
        if (text == null) return null;
        switch (text) {
            case "Sanidad": return ReportType.SANIDAD;
            case "Producción": return ReportType.PRODUCCION;
            case "Alertas": return ReportType.ALERTAS;
            case "Auditoría": return ReportType.AUDITORIA;
            case "Usuarios": return ReportType.USUARIOS;
            default: return null;
        }
    }

    @FXML
    private void initialize() {
        lblHeader.setText("Administrador");
        lblSystemStatus.setText("Sistema Offline – MySQL Local");

        populateTipoCombo();

        // Combos básicos (mock); luego se conectan a BD
        cbLote.getItems().addAll("Todos los lotes", "Lote 1", "Lote 2", "Lote 3");
        cbArticulo.getItems().addAll("Todos los artículos", "Maíz", "Trigo", "Alimento A");
        cbCategoria.getItems().addAll("Todas las categorías", "Insumos", "Alimento", "Vacunas");
        cbResponsable.getItems().addAll("Todos los responsables", "Juan", "Carla", "Admin");
        cbLote.getSelectionModel().selectFirst();
        cbArticulo.getSelectionModel().selectFirst();
        cbCategoria.getSelectionModel().selectFirst();
        cbResponsable.getSelectionModel().selectFirst();

        // KPI demo
        lblKpiMes.setText("47");
        lblMasSolicitado.setText("Producción");
        lblFormatos.setText("PDF / Excel");
        lblTPromedio.setText("2.1 min");
        pbInv.setProgress(0.45);
        pbProd.setProgress(0.30);
        pbSan.setProgress(0.25);

        // Estado inicial
        setType(ReportType.PRODUCCION, "Tipo seleccionado: Producción");
        if (rbPdf != null) rbPdf.setSelected(true);
    }
}
