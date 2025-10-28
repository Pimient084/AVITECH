package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import com.avitech.sia.report.PdfReportService;
import com.avitech.sia.report.ReportRequest;
import com.avitech.sia.report.ReportType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.nio.file.Path;

public class ReportesSupervisorController {

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
    private ReportType selectedType = ReportType.STOCK_ACTUAL;

    /* ================== Navegación ================== */
    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech — SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { /* Already here */ }
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech — Alertas"); }
    // @FXML private void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml", "SIA Avitech — Auditoría"); } // REMOVED
    // @FXML private void goParams()     { App.goTo("/fxml/superv/parametros_super.fxml", "SIA Avitech — Parámetros"); } // REMOVED
    // @FXML private void goUsers()      { App.goTo("/fxml/superv/usuarios_super.fxml", "SIA Avitech — Usuarios"); } // REMOVED
    // @FXML private void goBackup()     { App.goTo("/fxml/superv/respaldos_super.fxml", "SIA Avitech — Respaldos"); } // REMOVED
    @FXML private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }


    /* ============ Acciones principales ============ */
    @FXML private void onGenerar() {
        try {
            ReportType byCombo = mapTipo(cbTipoReporte != null ? cbTipoReporte.getValue() : null);
            if (byCombo != null) selectedType = byCombo;
            if (selectedType == null) {
                throw new IllegalStateException("Seleccione un tipo de reporte (tarjetas o combo)");
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
    @FXML private void selStockActual()            { setType(ReportType.STOCK_ACTUAL, "Tipo seleccionado: Stock Actual"); }
    @FXML private void selRegistroArticulo()       { setType(ReportType.REGISTRO_ARTICULO, "Tipo seleccionado: Registro por Artículo"); }
    @FXML private void selRecibosInsumos()         { setType(ReportType.RECIBOS_INSUMOS, "Tipo seleccionado: Recibos de Insumos"); }
    @FXML private void selConsumoAlimento()        { warnNoDisponible(); }
    @FXML private void selAplicacionesSanitarias() { warnNoDisponible(); }
    @FXML private void selProduccionTam()          { warnNoDisponible(); }
    @FXML private void selMortalidad()             { warnNoDisponible(); }

    private void setType(ReportType t, String msg) {
        this.selectedType = t;
        lblInfoFiltros.setText(msg);
        if (cbTipoReporte != null) cbTipoReporte.getSelectionModel().select(tipoToText(t));
    }
    private void warnNoDisponible() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Este tipo de reporte estará disponible próximamente. Por ahora solo PDF: Stock, Registro y Recibos.");
        a.setHeaderText("Reporte no disponible");
        a.showAndWait();
    }

    private void populateTipoCombo() {
        if (cbTipoReporte == null) return;
        cbTipoReporte.getItems().setAll(
                tipoToText(ReportType.STOCK_ACTUAL),
                tipoToText(ReportType.REGISTRO_ARTICULO),
                tipoToText(ReportType.RECIBOS_INSUMOS)
        );
        cbTipoReporte.getSelectionModel().select(tipoToText(selectedType));
    }

    private String tipoToText(ReportType t) {
        if (t == null) return null;
        return switch (t) {
            case STOCK_ACTUAL -> "Stock Actual";
            case REGISTRO_ARTICULO -> "Registro por Artículo";
            case RECIBOS_INSUMOS -> "Recibos de Insumos";
        };
    }
    private ReportType mapTipo(String text) {
        if (text == null) return null;
        switch (text) {
            case "Stock Actual": return ReportType.STOCK_ACTUAL;
            case "Registro por Artículo": return ReportType.REGISTRO_ARTICULO;
            case "Recibos de Insumos": return ReportType.RECIBOS_INSUMOS;
            default: return null;
        }
    }

    @FXML
    private void initialize() {
        // Tip utilitario: marca “Reportes” como activo en el sidebar si aplica una clase CSS
        lblHeader.setText("Supervisor");
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
        setType(ReportType.STOCK_ACTUAL, "Tipo seleccionado: Stock Actual");
        if (rbPdf != null) rbPdf.setSelected(true);
    }
}
