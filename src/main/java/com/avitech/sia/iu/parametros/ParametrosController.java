package com.avitech.sia.iu.parametros;

import com.avitech.sia.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class ParametrosController {

    @FXML private Label lblSystemStatus, lblHeader, lblUserInfo;
    @FXML private ToggleButton btnUnidades, btnCategorias, btnMedicamentos, btnLotes, btnUbicaciones;
    @FXML private StackPane contentStack;

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Offline – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");

        // vista por defecto
        show("/fxml/Parametros/parametros_unidades.fxml"); // Ruta corregida
    }

    // Segmentos
    @FXML private void showUnidades()    { show("/fxml/Parametros/parametros_unidades.fxml"); }
    @FXML private void showCategorias()  { show("/fxml/Parametros/parametros_categorias.fxml"); }
    @FXML private void showMedicamentos(){ show("/fxml/Parametros/parametros_medicamentos.fxml"); }
    @FXML private void showLotes()       { show("/fxml/Parametros/parametros_lotes.fxml"); }
    @FXML private void showUbicaciones() { show("/fxml/Parametros/parametros_ubicaciones.fxml"); }

    private void show(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentStack.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ====== NAV (mismo patrón del proyecto) ====== */
    @FXML private void goDashboard()  { App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/suministros.fxml",     "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/sanidad.fxml",         "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/produccion.fxml",      "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml",        "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml",         "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml",       "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { /* ya estás aquí */ }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml",        "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml",       "SIA Avitech — Respaldos"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml",           "SIA Avitech — Inicio de sesión"); }
}
