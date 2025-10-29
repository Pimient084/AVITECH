package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import com.avitech.sia.report.PdfReportService;
import com.avitech.sia.report.ReportRequest;
import com.avitech.sia.report.ReportType;
import com.avitech.sia.report.RecentReportsService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// Animaciones
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

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
    @FXML private RadioButton rbPdf; // rbExcel eliminado
    @FXML private CheckBox chkPreview;
    @FXML private Label lblInfoFiltros;
    @FXML private Button btnGenerar;

    // Botones de tipo (para marcar selección)
    @FXML private Button btnSanidad;
    @FXML private Button btnProduccion;
    @FXML private Button btnAlertas;
    @FXML private Button btnAuditoria;
    @FXML private Button btnUsuarios;

    // Recientes
    @FXML private TableView<RecentRow> tvRecientes;
    @FXML private TableColumn<RecentRow, String> colRepNombre, colRepFecha, colRepTam;
    @FXML private TableColumn<RecentRow, RecentRow> colRepAccion;

    // KPI/Stats (pueden no existir si el FXML no los define)
    @FXML private Label lblKpiMes, lblMasSolicitado, lblFormatos, lblTPromedio;
    @FXML private ProgressBar pbInv, pbProd, pbSan;

    // Estado interno
    private ReportType selectedType = ReportType.SANIDAD;

    // Servicio y formato
    private final RecentReportsService recentService = new RecentReportsService();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* ================== Navegación ================== */
    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech — SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { /* Already here */ }
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech — Alertas"); }
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

            // Abrir el PDF al generarse (si el sistema lo soporta), si falla mostrar la ruta
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(file.toFile());
                } else {
                    Alert ok = new Alert(Alert.AlertType.INFORMATION, "Reporte generado: " + file.toAbsolutePath());
                    ok.setHeaderText("Reporte PDF");
                    ok.showAndWait();
                }
            } catch (Exception ex) {
                // Si no puede abrir, mostrar un mensaje con la ruta
                Alert ok = new Alert(Alert.AlertType.INFORMATION, "Reporte generado: " + file.toAbsolutePath() + "\n(No se pudo abrir automáticamente: " + ex.getMessage() + ")");
                ok.setHeaderText("Reporte PDF");
                ok.showAndWait();
            }

            // refrescar recientes
            loadRecentReports();

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
    @FXML private void onExportarExcel() { /* eliminado funcionalmente: solo PDF */ }

    /* ============ Selección de tipo de reporte (tarjetas) ============ */
    @FXML private void selSanidad()   { setType(ReportType.SANIDAD,    "Tipo seleccionado: Sanidad"); }
    @FXML private void selProduccion(){ setType(ReportType.PRODUCCION, "Tipo seleccionado: Producción"); }
    @FXML private void selAlertas()   { setType(ReportType.ALERTAS,    "Tipo seleccionado: Alertas"); }
    @FXML private void selAuditoria() { setType(ReportType.AUDITORIA,  "Tipo seleccionado: Auditoría"); }
    @FXML private void selUsuarios()  { setType(ReportType.USUARIOS,   "Tipo seleccionado: Usuarios"); }

    private void setType(ReportType t, String msg) {
        if (t == null) return;
        // Si ya es el mismo tipo, solo actualizar estilos/label y salir
        if (t == this.selectedType) {
            lblInfoFiltros.setText(msg);
            updateButtonStyles();
            return;
        }
        this.selectedType = t;
        lblInfoFiltros.setText(msg);
        if (cbTipoReporte != null) cbTipoReporte.getSelectionModel().select(tipoToText(t));
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        // Protege si los botones no están inyectados (por ejemplo durante tests)
        if (btnSanidad == null) return;

        setSelectedClass(btnSanidad, selectedType == ReportType.SANIDAD);
        setSelectedClass(btnProduccion, selectedType == ReportType.PRODUCCION);
        setSelectedClass(btnAlertas, selectedType == ReportType.ALERTAS);
        setSelectedClass(btnAuditoria, selectedType == ReportType.AUDITORIA);
        setSelectedClass(btnUsuarios, selectedType == ReportType.USUARIOS);
    }

    private void setSelectedClass(Button b, boolean add) {
        if (b == null) return;
        if (add) {
            if (!b.getStyleClass().contains("type-selected")) b.getStyleClass().add("type-selected");
            playSelectAnim(b);
        } else {
            b.getStyleClass().removeIf(s -> s.equals("type-selected"));
            // normalizar escala si quedó alterada por animación previa
            b.setScaleX(1.0);
            b.setScaleY(1.0);
        }
    }

    private void playSelectAnim(Button b) {
        try {
            b.setScaleX(0.98);
            b.setScaleY(0.98);
            ScaleTransition st = new ScaleTransition(Duration.millis(140), b);
            st.setFromX(0.98); st.setToX(1.0);
            st.setFromY(0.98); st.setToY(1.0);
            st.play();
        } catch (Exception ignore) { /* animación opcional, no bloquear */ }
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

    // ====== Reportes Recientes ======
    private void setupRecentColumns() {
        if (tvRecientes == null) return;
        colRepNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colRepFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fecha()));
        colRepTam.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tam()));
        colRepAccion.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colRepAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnOpen = new Button("Abrir");
            private final HBox box = new HBox(6, btnOpen);
            {
                btnOpen.getStyleClass().add("secondary");
                btnOpen.setOnAction(e -> {
                    RecentRow row = getItem();
                    if (row == null) return;
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                            Desktop.getDesktop().open(row.path().toFile());
                        } else {
                            new Alert(Alert.AlertType.INFORMATION, "Archivo: " + row.path().toAbsolutePath()).showAndWait();
                        }
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + ex.getMessage()).showAndWait();
                    }
                });
            }
            @Override protected void updateItem(RecentRow value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    private void loadRecentReports() {
        if (tvRecientes == null) return;
        List<RecentReportsService.RecentReport> list = recentService.listRecent(20);
        List<RecentRow> rows = list.stream().map(r -> new RecentRow(
                r.name(),
                DF.format(r.modified().atZone(ZoneId.systemDefault()).toLocalDateTime()),
                humanSize(r.size()),
                r.path()
        )).collect(Collectors.toList());
        tvRecientes.getItems().setAll(rows);
    }

    private String humanSize(long size) {
        String[] units = {"B", "KB", "MB", "GB"};
        double s = size; int i = 0;
        while (s >= 1024 && i < units.length - 1) { s /= 1024; i++; }
        return (i == 0 ? (long)s : Math.round(s * 10) / 10.0) + " " + units[i];
    }

    public record RecentRow(String name, String fecha, String tam, Path path) { }

    @FXML
    private void initialize() {
        lblHeader.setText("Supervisor");
        lblSystemStatus.setText("Sistema Offline – MySQL Local");

        populateTipoCombo();

        // Listener para sincronizar selección del combo con las tarjetas
        if (cbTipoReporte != null) {
            cbTipoReporte.valueProperty().addListener((obs, oldV, newV) -> {
                ReportType t = mapTipo(newV);
                if (t != null) setType(t, "Tipo seleccionado: " + tipoToText(t));
            });
        }

        // Combos básicos (mock); luego se conectan a BD
        cbLote.getItems().addAll("Todos los lotes", "Lote 1", "Lote 2", "Lote 3");
        cbArticulo.getItems().addAll("Todos los artículos", "Maíz", "Trigo", "Alimento A");
        cbCategoria.getItems().addAll("Todas las categorías", "Insumos", "Alimento", "Vacunas");
        cbResponsable.getItems().addAll("Todos los responsables", "Juan", "Carla", "Admin");
        cbLote.getSelectionModel().selectFirst();
        cbArticulo.getSelectionModel().selectFirst();
        cbCategoria.getSelectionModel().selectFirst();
        cbResponsable.getSelectionModel().selectFirst();

        // KPI demo (protegido para cuando el FXML no tiene estos nodos)
        if (lblKpiMes != null) lblKpiMes.setText("47");
        if (lblMasSolicitado != null) lblMasSolicitado.setText("Producción");
        if (lblFormatos != null) lblFormatos.setText("PDF");
        if (lblTPromedio != null) lblTPromedio.setText("2.1 min");
        if (pbInv != null) pbInv.setProgress(0.45);
        if (pbProd != null) pbProd.setProgress(0.30);
        if (pbSan != null) pbSan.setProgress(0.25);

        // Estado inicial
        setType(ReportType.SANIDAD, "Tipo seleccionado: Sanidad");
        updateButtonStyles();
        if (rbPdf != null) rbPdf.setSelected(true);

        // recientes
        if (tvRecientes != null) tvRecientes.setPlaceholder(new Label("No hay reportes recientes"));
        setupRecentColumns();
        loadRecentReports();
    }
}
