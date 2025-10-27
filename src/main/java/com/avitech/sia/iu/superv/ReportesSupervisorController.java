package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ReportesSupervisorController {

    // Sidebar / topbar
    @FXML private VBox sidebar;
    @FXML private Label lblUserInfo;
    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;

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


    /* ============ Acciones principales (stubs) ============ */
    @FXML private void onGenerar()      { /* TODO: construir payload con filtros y lanzar generación */ }
    @FXML private void onLimpiar() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cbLote.getSelectionModel().clearSelection();
        cbArticulo.getSelectionModel().clearSelection();
        cbCategoria.getSelectionModel().clearSelection();
        cbResponsable.getSelectionModel().clearSelection();
    }
    @FXML private void onExportarExcel() { /* TODO: exportar según selección */ }

    /* ============ Selección de tipo de reporte (tarjetas) ============ */
    @FXML private void selStockActual()           { /* setear tipo = STOCK */ }
    @FXML private void selRegistroArticulo()      { /* setear tipo = REG_ART */ }
    @FXML private void selRecibosInsumos()        { /* setear tipo = REC_INS */ }
    @FXML private void selConsumoAlimento()       { /* setear tipo = CON_ALI */ }
    @FXML private void selAplicacionesSanitarias(){ /* setear tipo = APP_SAN */ }
    @FXML private void selProduccionTam()         { /* setear tipo = PROD_TAM */ }
    @FXML private void selMortalidad()            { /* setear tipo = MORT */ }

    @FXML
    private void initialize() {
        // Tip utilitario: marca “Reportes” como activo en el sidebar si aplica una clase CSS
        lblHeader.setText("Supervisor");
        lblSystemStatus.setText("Sistema Offline – MySQL Local");

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
    }
}
