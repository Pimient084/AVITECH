package com.avitech.sia.iu.oper;

import com.avitech.sia.App;
import com.avitech.sia.db.Suministro;
import com.avitech.sia.db.oper.SuministrosOperDAO;
import com.avitech.sia.db.oper.UsuarioOperDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class SuministrosOperController {

    private static final Logger LOG = Logger.getLogger(SuministrosOperController.class.getName());

    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    @FXML private TableView<Mov> tblMovs;
    @FXML private ComboBox<String> cbTipo;
    @FXML private Button btnEntrada, btnSalida;

    // Campos adicionales utilizados por el FXML de Operador
    @FXML private Label kpiMovHoy;
    @FXML private Label kpiActivos;
    @FXML private Label kpiStockBajo;
    @FXML private Label kpiValor;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbResp;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Label lblMostrando;

    // Columnas
    @FXML private TableColumn<Mov,String> colFecha, colItem, colCant, colUnidad, colTipo, colResp, colDet, colStock, colAccion;

    private final SuministrosOperDAO suministroDAO = new SuministrosOperDAO();
    private final UsuarioOperDAO usuarioDAO = new UsuarioOperDAO();
    private final ObservableList<Mov> master = FXCollections.observableArrayList();
    private final ObservableList<Mov> filtered = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (lblSystemStatus != null) lblSystemStatus.setText("Sistema Online — MySQL Local");
        if (lblHeader != null) lblHeader.setText("Operador");
        if (lblUserInfo != null) lblUserInfo.setText("Operador");

        cbTipo.setItems(FXCollections.observableArrayList("Todos","Entrada","Salida"));
        cbTipo.getSelectionModel().selectFirst();

        // Column factories (si las columnas están declaradas en el FXML)
        if (colFecha != null) colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha));
        if (colItem != null)  colItem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().item));
        if (colCant != null)  colCant.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cantidad));
        if (colUnidad != null) colUnidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unidad));
        if (colTipo != null)  colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo));
        if (colResp != null)  colResp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().responsable));
        if (colDet != null)   colDet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().detalles));
        if (colStock != null) colStock.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().stock));
        if (colAccion != null) {
            colAccion.setCellValueFactory(c -> new SimpleStringProperty("ver"));
            colAccion.setCellFactory(col -> new TableCell<>() {
                private final Hyperlink link = new Hyperlink("Ver ítem");
                { link.setOnAction(e -> onVerItem(getTableView().getItems().get(getIndex()))); }
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    setGraphic(empty ? null : link);
                    setText(null);
                }
            });
        }

        // Cargar datos
        loadSuministros();
        applyFilter();
        refreshKpis();

        // Fechas por defecto
        if (dpDesde != null) dpDesde.setValue(LocalDate.now().minusDays(30));
        if (dpHasta != null) dpHasta.setValue(LocalDate.now());
    }

    private void loadSuministros() {
        try {
            List<Suministro> suministros = suministroDAO.getAll();
            master.setAll(suministros.stream().map(Mov::new).collect(Collectors.toList()));
            if (tblMovs != null) tblMovs.setItems(master);

            // Llenar combo de responsables
            List<String> responsables = usuarioDAO.getAllNombres();
            if (responsables != null) {
                responsables.add(0, "Todos");
                if (cbResp != null) {
                    cbResp.setItems(FXCollections.observableArrayList(responsables));
                    cbResp.getSelectionModel().selectFirst();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al cargar suministros", e);
            new Alert(Alert.AlertType.ERROR, "Error al cargar los suministros: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onEntrada() {
        new Alert(Alert.AlertType.INFORMATION, "Registrar Entrada (Operador) - no implementado").showAndWait();
    }

    @FXML
    private void onSalida() {
        new Alert(Alert.AlertType.INFORMATION, "Registrar Salida (Operador) - no implementado").showAndWait();
    }

    @FXML
    private void onVerStock() { new Alert(Alert.AlertType.INFORMATION, "Ver Stock - no implementado").showAndWait(); }
    @FXML
    private void onMoverStock() { new Alert(Alert.AlertType.INFORMATION, "Mover Stock - no implementado").showAndWait(); }
    @FXML
    private void onExportar() { new Alert(Alert.AlertType.INFORMATION, "Exportar - no implementado").showAndWait(); }

    private void onVerItem(Mov m) { new Alert(Alert.AlertType.INFORMATION, "Ítem: " + m.item).showAndWait(); }

    @FXML
    private void applyFilter() {
        final String t = lower(txtSearch == null ? "" : txtSearch.getText());
        final String tipo = sel(cbTipo);
        final String resp = sel(cbResp);
        final LocalDate d1 = dpDesde == null ? null : dpDesde.getValue();
        final LocalDate d2 = dpHasta == null ? null : dpHasta.getValue();

        filtered.setAll(master.filtered(m ->
                (t.isEmpty() || m.itemLc.contains(t) || m.detallesLc.contains(t) || m.respLc.contains(t)) &&
                        ("Todos".equals(tipo) || m.tipo.equalsIgnoreCase(tipo)) &&
                        ("Todos".equals(resp) || m.responsable.equalsIgnoreCase(resp)) &&
                        (between(m.localDate, d1, d2))
        ));

        if (tblMovs != null) tblMovs.setItems(filtered);
        if (lblMostrando != null) lblMostrando.setText(filtered.size() + " ítems");
    }

    private static boolean between(LocalDate f, LocalDate d1, LocalDate d2) {
        if (f == null) return true;
        boolean ok1 = (d1 == null) || !f.isBefore(d1);
        boolean ok2 = (d2 == null) || !f.isAfter(d2);
        return ok1 && ok2;
    }

    private static String lower(String s) { return s == null ? "" : s.toLowerCase().trim(); }
    private static String sel(ComboBox<String> cb) { String s = cb == null ? "Todos" : cb.getSelectionModel().getSelectedItem(); return s == null ? "Todos" : s; }

    private void refreshKpis() {
        long hoy = master.stream().filter(m -> m.localDate != null && m.localDate.equals(LocalDate.now())).count();
        long activos = master.stream().map(m -> m.item).distinct().count();
        long bajos = master.stream().filter(m -> m.tipo.equalsIgnoreCase("Salida")).count();

        BigDecimal valor = master.stream().filter(m -> m.valorTotal != null).map(m -> m.valorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        if (kpiMovHoy != null) kpiMovHoy.setText(String.valueOf(hoy));
        if (kpiActivos != null) kpiActivos.setText(String.valueOf(activos));
        if (kpiStockBajo != null) kpiStockBajo.setText(String.valueOf(bajos));
        if (kpiValor != null) kpiValor.setText(currencyFormat.format(valor));
    }

    /* DTO local similar al Admin Mov */
    public static class Mov {
        public final String fecha, item, cantidad, unidad, tipo, responsable, detalles, stock;
        public final String itemLc, detallesLc, respLc;
        public final LocalDate localDate;
        public final BigDecimal valorTotal;

        public Mov(Suministro s) {
            this.fecha = s.getFecha() == null ? "" : s.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.item = s.getItem();
            this.cantidad = String.valueOf(s.getCantidad());
            this.unidad = s.getUnidad();
            this.tipo = s.getTipo();
            this.responsable = s.getResponsable();

            String prov = s.getProveedor();
            String mot = s.getMotivo();
            StringBuilder det = new StringBuilder();
            if (prov != null && !prov.trim().isEmpty()) {
                det.append("Proveedor: ").append(prov);
            }
            if (mot != null && !mot.trim().isEmpty()) {
                if (det.length() > 0) det.append(" | ");
                det.append("Motivo: ").append(mot);
            }
            this.detalles = det.toString();
            this.stock = "";

            this.itemLc = item == null ? "" : item.toLowerCase();
            this.detallesLc = detalles == null ? "" : detalles.toLowerCase();
            this.respLc = responsable == null ? "" : responsable.toLowerCase();
            this.localDate = s.getFecha();
            this.valorTotal = s.getValorTotal();
        }
    }

    @FXML private void goDashboard()  { App.goTo("/fxml/oper/dashboard_oper.fxml", "SIA Avitech — Operador"); }
    @FXML private void goSupplies()   { /* ya estás aquí */ }
    @FXML private void goHealth()     { App.goTo("/fxml/oper/sanidad_oper.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/oper/produccion_oper.fxml", "SIA Avitech — Producción"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión"); }
}
