package com.avitech.sia.iu;

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

public class DashboardController {

    // DAOs
    private final LoteDAO loteDAO = new LoteDAO();
    private final ProduccionDAO produccionDAO = new ProduccionDAO();
    private final SuministroDAO suministroDAO = new SuministroDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final GalponDAO galponDAO = new GalponDAO();

    // Topbar / user
    @FXML private Label lblHeader;
    @FXML private Label lblSystemStatus;
    @FXML private Label lblUserInfo;

    // KPIs
    @FXML private Label lblAvesActivas;
    @FXML private Label lblAvesActivasDelta;
    @FXML private Label lblHuevosDia;
    @FXML private Label lblHuevosDiaDelta;
    @FXML private Label lblSuministros;
    @FXML private Label lblSuministrosDelta;
    @FXML private Label lblAlertas;
    @FXML private Label lblAlertasDelta;

    // Chart + alertas
    @FXML private LineChart<String, Number> chartSemanal;
    @FXML private ListView<String> listAlertas;

    // Galpones
    @FXML private ProgressBar pbG1; @FXML private Label lblG1;
    @FXML private ProgressBar pbG2; @FXML private Label lblG2;
    @FXML private ProgressBar pbG3; @FXML private Label lblG3;
    @FXML private ProgressBar pbG4; @FXML private Label lblG4;
    @FXML private ProgressBar pbG5; @FXML private Label lblG5;
    @FXML private ProgressBar pbG6; @FXML private Label lblG6;

    // Sidebar (para gestionar “activo”)
    @FXML private VBox sidebar;

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");

        loadDashboardData();
    }

    private void loadDashboardData() {
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

    private void setGalponData(ProgressBar pb, Label lbl, GalponDAO.GalponSummary summary) {
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

    @FXML private void onExit() {
        App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
    }

    /* ======== NAV (stubs) ======== */
    @FXML private void goDashboard()  { /* Already here */ }
    @FXML private void goSupplies()   { App.goTo("/fxml/suministros.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/sanidad.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/produccion.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml", "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml", "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml", "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }

}
