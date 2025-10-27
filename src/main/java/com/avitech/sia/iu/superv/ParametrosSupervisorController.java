package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class ParametrosSupervisorController {

    @FXML private Label lblSystemStatus, lblHeader, lblUserInfo;
    @FXML private ToggleButton btnUnidades, btnCategorias, btnMedicamentos, btnLotes, btnUbicaciones;
    @FXML private StackPane contentStack;

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Offline – MySQL Local");
        lblHeader.setText("Supervisor");
        lblUserInfo.setText("Supervisor");

        // vista por defecto: cambiar a una que existe en resources/fxml/superv
        show("/fxml/superv/parametros_categorias_super.fxml"); // Ruta corregida
    }

    // Segmentos
    @FXML private void showUnidades()    { show("/fxml/superv/parametros_categorias_super.fxml"); }
    @FXML private void showCategorias()  { show("/fxml/superv/parametros_categorias_super.fxml"); }
    @FXML private void showMedicamentos(){ show("/fxml/superv/parametros_medicamentos_super.fxml"); }
    @FXML private void showLotes()       { show("/fxml/superv/parametros_lotes_super.fxml"); }
    @FXML private void showUbicaciones() { show("/fxml/superv/parametros_ubicaciones_super.fxml"); }

    private void show(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentStack.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ====== NAV (mismo patrón del proyecto) ====== */
    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech — SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml",     "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml",         "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml",      "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/superv/reportes_super.fxml",        "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml",         "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml",       "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { /* ya estás aquí */ }
    @FXML private void goUsers()      { App.goTo("/fxml/superv/usuarios_super.fxml",        "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/superv/respaldos_super.fxml",       "SIA Avitech — Respaldos"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml",           "SIA Avitech — Inicio de sesión"); }
}
