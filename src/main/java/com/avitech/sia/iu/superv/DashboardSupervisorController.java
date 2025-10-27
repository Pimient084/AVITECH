package com.avitech.sia.iu.superv;

import com.avitech.sia.App;
import com.avitech.sia.db.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardSupervisorController {

    // DAOs
    protected final LoteDAO loteDAO = new LoteDAO();
    protected final ProduccionDAO produccionDAO = new ProduccionDAO();
    protected final SuministroDAO suministroDAO = new SuministroDAO();
    protected final AlertaDAO alertaDAO = new AlertaDAO();
    protected final GalponDAO galponDAO = new GalponDAO();

    // Topbar / user
    @FXML protected Label lblHeader;
    @FXML protected Label lblSystemStatus;
    @FXML protected Label lblUserInfo;

    // KPIs
    @FXML protected Label lblAvesActivas;
    @FXML protected Label lblAvesActivasDelta;
    @FXML protected Label lblHuevosDia;
    @FXML protected Label lblHuevosDiaDelta;
    @FXML protected Label lblSuministros;
    @FXML protected Label lblSuministrosDelta;
    @FXML protected Label lblAlertas;
    @FXML protected Label lblAlertasDelta;

    // Chart + alertas
    @FXML protected LineChart<String, Number> chartSemanal;
    @FXML protected ListView<String> listAlertas;

    // Galpones
    @FXML protected ProgressBar pbG1; @FXML protected Label lblG1;
    @FXML protected ProgressBar pbG2; @FXML protected Label lblG2;
    @FXML protected ProgressBar pbG3; @FXML protected Label lblG3;
    @FXML protected ProgressBar pbG4; @FXML protected Label lblG4;
    @FXML protected ProgressBar pbG5; @FXML protected Label lblG5;
    @FXML protected ProgressBar pbG6; @FXML protected Label lblG6;

    // Sidebar (para gestionar “activo”)
    @FXML protected VBox sidebar;

    @FXML
    protected void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Supervisor");
        lblUserInfo.setText("Supervisor");

        loadDashboardData();
    }

    protected void loadDashboardData() {
        try {
            // KPI: Aves Activas
            int totalAves = loteDAO.getTotalActiveGallinas();
            lblAvesActivas.setText(String.format("%,d gallinas", totalAves));
            lblAvesActivasDelta.setText("+0"); // No hay datos históricos para calcular delta

            // KPI: Producción Diaria de Huevos
            LocalDate today = LocalDate.now();
            int huevosHoy = produccionDAO.getDailyTotalHuevos(today);
            lblHuevosDia.setText(String.format("%,d huevos", huevosHoy));
            lblHuevosDiaDelta.setText("+0%"); // No hay datos históricos para calcular delta

            // KPI: Suministros Disponibles
            int distinctSuministros = suministroDAO.getDistinctItemCount();
            lblSuministros.setText(String.format("%d ítems", distinctSuministros));
            lblSuministrosDelta.setText("-0%"); // No hay datos históricos para calcular delta

            // KPI: Alertas de Stock Bajo
            int lowStockAlerts = alertaDAO.getActiveLowStockAlertsCount();
            lblAlertas.setText(String.valueOf(lowStockAlerts));
            lblAlertasDelta.setText("+0"); // No hay datos históricos para calcular delta

            // Gráfico de Producción Semanal
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
            Map<LocalDate, Integer> weeklyData = produccionDAO.getWeeklyTotalHuevos(startOfWeek, today);
            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                String day = date.format(DateTimeFormatter.ofPattern("EEE"));
                series.getData().add(new XYChart.Data<>(day, weeklyData.getOrDefault(date, 0)));
            }
            chartSemanal.getData().setAll(series);
            ((CategoryAxis) chartSemanal.getXAxis()).setLabel("Día");
            ((NumberAxis) chartSemanal.getYAxis()).setLabel("Huevos");

            // Alertas Recientes
            listAlertas.setItems(FXCollections.observableArrayList(alertaDAO.getRecentAlertsDescriptions(3)));

            // Producción por Galpón
            Map<Integer, GalponDAO.GalponSummary> galponSummaries = galponDAO.getGalponProductionSummary(today);
            setGalponData(pbG1, lblG1, galponSummaries.get(1));
            setGalponData(pbG2, lblG2, galponSummaries.get(2));
            setGalponData(pbG3, lblG3, galponSummaries.get(3));
            setGalponData(pbG4, lblG4, galponSummaries.get(4));
            setGalponData(pbG5, lblG5, galponSummaries.get(5));
            setGalponData(pbG6, lblG6, galponSummaries.get(6));

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al cargar datos del Dashboard: " + e.getMessage()).showAndWait();
            lblSystemStatus.setText("Sistema Offline – Error de BD");
        }
    }

    protected void setGalponData(ProgressBar pb, Label lbl, GalponDAO.GalponSummary summary) {
        if (summary != null) {
            double pct = summary.capacidad() == 0 ? 0 : Math.min(1.0, summary.currentProduction() / (double) summary.capacidad());
            pb.setProgress(pct);
            lbl.setText(summary.currentProduction() + "/" + summary.capacidad() + " (" + Math.round(pct * 100) + "%)");
        } else {
            pb.setProgress(0);
            lbl.setText("N/A");
        }
    }

    public void setHeader(String header) {
        if (lblHeader != null) lblHeader.setText(header);
        if (lblUserInfo != null) lblUserInfo.setText(header);
    }

    @FXML protected void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }

    /* ======== NAV (stubs) ======== */
    @FXML protected void goDashboard()  { /* Already here */ }
    @FXML protected void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech — Suministros"); }
    @FXML protected void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech — Sanidad"); }
    @FXML protected void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech — Producción"); }
    @FXML protected void goReports()    { App.goTo("/fxml/superv/reportes_super.fxml", "SIA Avitech — Reportes"); }
    @FXML protected void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech — Alertas"); }
    @FXML protected void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml", "SIA Avitech — Auditoría"); }
    @FXML protected void goParams()     { App.goTo("/fxml/superv/parametros_super.fxml", "SIA Avitech — Parámetros"); }
    @FXML protected void goUsers()      { App.goTo("/fxml/superv/usuarios_super.fxml", "SIA Avitech — Usuarios"); }
    @FXML protected void goBackup()     { App.goTo("/fxml/superv/respaldos_super.fxml", "SIA Avitech — Respaldos"); }

}
