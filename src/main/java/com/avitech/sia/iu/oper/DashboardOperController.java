package com.avitech.sia.iu.oper;

import com.avitech.sia.App;
import com.avitech.sia.db.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardOperController {

    private static final Logger LOG = Logger.getLogger(DashboardOperController.class.getName());

    // DAOs
    protected final LoteDAO loteDAO = new LoteDAO();
    protected final ProduccionDAO produccionDAO = new ProduccionDAO();
    protected final SuministroDAO suministroDAO = new SuministroDAO();
    protected final GalponDAO galponDAO = new GalponDAO();

    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    // KPI labels
    @FXML private Label lblAvesActivas;
    @FXML private Label lblAvesActivasDelta;
    @FXML private Label lblHuevosDia;
    @FXML private Label lblHuevosDiaDelta;
    @FXML private Label lblSuministros;
    @FXML private Label lblSuministrosDelta;

    // Chart & list
    @FXML private LineChart<String, Number> chartSemanal;
    // Note: operador FXML no tiene panel derecho de alertas (listAlertas)

    // Progress bars y labels por galpón
    @FXML private ProgressBar pbG1, pbG2, pbG3, pbG4, pbG5, pbG6;
    @FXML private Label lblG1, lblG2, lblG3, lblG4, lblG5, lblG6;

    // Sidebar (si lo hubiese)
    @FXML private VBox sidebar;

    @FXML
    private void initialize() {
        if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Online – MySQL Local");
        if (lblHeader != null) lblHeader.setText("Operador");
        if (lblUserInfo != null) lblUserInfo.setText("Operador");

        // Cargar datos reales desde DAOs
        try {
            loadDashboardData();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al inicializar dashboard", e);
            if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Offline – Error de BD");
            new Alert(Alert.AlertType.ERROR, "Error al cargar datos del Dashboard: " + e.getMessage()).showAndWait();
        }
    }

    protected void loadDashboardData() {
        try {
            // KPI: Aves Activas
            int totalAves = loteDAO.getTotalActiveGallinas();
            if (lblAvesActivas != null) lblAvesActivas.setText(String.format("%,d gallinas", totalAves));
            if (lblAvesActivasDelta != null) lblAvesActivasDelta.setText("+0");

            // KPI: Producción Diaria de Huevos
            LocalDate today = LocalDate.now();
            int huevosHoy = produccionDAO.getDailyTotalHuevos(today);
            if (lblHuevosDia != null) lblHuevosDia.setText(String.format("%,d huevos", huevosHoy));
            if (lblHuevosDiaDelta != null) lblHuevosDiaDelta.setText("+0%");

            // KPI: Suministros Disponibles
            int distinctSuministros = suministroDAO.getDistinctItemCount();
            if (lblSuministros != null) lblSuministros.setText(String.format("%d ítems", distinctSuministros));
            if (lblSuministrosDelta != null) lblSuministrosDelta.setText("-0%");

            // Gráfico de Producción Semanal
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
            Map<LocalDate, Integer> weeklyData = produccionDAO.getWeeklyTotalHuevos(startOfWeek, today);
            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                String day = date.format(DateTimeFormatter.ofPattern("EEE"));
                int value = weeklyData.getOrDefault(date, 0);
                series.getData().add(new XYChart.Data<>(day, value));
            }
            if (chartSemanal != null) {
                chartSemanal.getData().clear();
                chartSemanal.getData().add(series);
                ((CategoryAxis) chartSemanal.getXAxis()).setLabel("Día");
                ((NumberAxis) chartSemanal.getYAxis()).setLabel("Huevos");
            }

            // Producción por Galpón
            Map<Integer, GalponDAO.GalponSummary> galponSummaries = galponDAO.getGalponProductionSummary(today);
            setGalponData(pbG1, lblG1, galponSummaries.get(1));
            setGalponData(pbG2, lblG2, galponSummaries.get(2));
            setGalponData(pbG3, lblG3, galponSummaries.get(3));
            setGalponData(pbG4, lblG4, galponSummaries.get(4));
            setGalponData(pbG5, lblG5, galponSummaries.get(5));
            setGalponData(pbG6, lblG6, galponSummaries.get(6));

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cargar datos del dashboard", e);
            if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Offline – Error de BD");
            new Alert(Alert.AlertType.ERROR, "Error al cargar datos del Dashboard: " + e.getMessage()).showAndWait();
        }
    }

    protected void setGalponData(ProgressBar pb, Label lbl, GalponDAO.GalponSummary summary) {
        if (pb == null || lbl == null) return;
        if (summary != null) {
            double pct = summary.capacidad() == 0 ? 0 : Math.min(1.0, summary.currentProduction() / (double) summary.capacidad());
            pb.setProgress(pct);
            lbl.setText(summary.currentProduction() + "/" + summary.capacidad() + " (" + Math.round(pct * 100) + "%)");
        } else {
            pb.setProgress(0);
            lbl.setText("N/A");
        }
    }

     @FXML
     private void goDashboard() {
         // We're already on the dashboard; refresh
         App.goTo("/fxml/oper/dashboard_oper.fxml", "SIA Avitech — Operador");
     }

     @FXML
     private void goSupplies() {
         App.goTo("/fxml/oper/suministros_oper.fxml", "SIA Avitech — Suministros");
     }

     @FXML
     private void goHealth() {
         App.goTo("/fxml/oper/sanidad_oper.fxml", "SIA Avitech — Sanidad");
     }

     @FXML
     private void goProduction() {
         App.goTo("/fxml/oper/produccion_oper.fxml", "SIA Avitech — Producción");
     }

     @FXML
     private void onExit() {
         App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
     }
}
