package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RespaldosSupervisorController {

    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online 13 MySQL Local");
        lblHeader.setText("Supervisor");
        lblUserInfo.setText("Supervisor");
    }

    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech  SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech  Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech  Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech  Producci\u00f3n"); }
    @FXML private void goReports()    { App.goTo("/fxml/superv/reportes_super.fxml", "SIA Avitech  Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech  Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml", "SIA Avitech  Auditor\u00eda"); }
    @FXML private void goParams()     { App.goTo("/fxml/superv/parametros_super.fxml", "SIA Avitech  Par\u00e1metros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/superv/usuarios_super.fxml", "SIA Avitech  Usuarios"); }
    @FXML private void goBackup()     { /* ya est\u00e1s aqu\u00ed */ }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml", "SIA Avitech  Inicio de sesi\u00f3n"); }
}

