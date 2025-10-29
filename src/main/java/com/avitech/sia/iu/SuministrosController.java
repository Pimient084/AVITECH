package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.Suministro;
import com.avitech.sia.db.SuministroDAO;
import com.avitech.sia.db.UsuarioDAO;
import com.avitech.sia.db.StockProduccion;
import com.avitech.sia.db.StockRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.stream.Collectors;

/** Controlador de Suministros conectado a la base de datos. */
@SuppressWarnings("unused")
public class SuministrosController {

    private static final Logger logger = LoggerFactory.getLogger(SuministrosController.class);

    /* topbar / sidebar */
    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    /* KPIs */
    @FXML private Label kpiMovHoy, kpiActivos, kpiStockBajo, kpiValor;

    /* filtros */
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbTipo, cbResp;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Label lblMostrando;

    /* tabla */
    @FXML private TableView<Mov> tblMovs;
    @FXML private TableColumn<Mov, String> colFecha, colItem, colCant, colUnidad, colTipo, colResp, colDet, colStock, colAccion;

    @FXML private Button btnEntrada;
    @FXML private Button btnSalida;

    private final SuministroDAO suministroDAO = new SuministroDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    // Repositorio para consultar stock en producción
    private final StockRepository stockRepository = new StockRepository();
    private final ObservableList<Mov> master = FXCollections.observableArrayList();
    private final ObservableList<Mov> filtered = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Online – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");

        /* combos / fechas */
        cbTipo.setItems(FXCollections.observableArrayList("Todos", "Entrada", "Salida"));
        cbTipo.getSelectionModel().selectFirst();
        // cbResp se llena dinámicamente en loadSuministros()
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());

        /* columnas */
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha));
        colItem.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().item));
        colCant.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().cantidad));
        colUnidad.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().unidad));
        colTipo.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().tipo));
        colResp.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().responsable));
        colDet.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().detalles));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().stock));

        /* botón en columna Acciones */
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

        loadSuministros();
        applyFilter();
        refreshKpis();
    }

    private void loadSuministros() {
        try {
            List<Suministro> suministros = suministroDAO.getAll();
            master.setAll(
                    suministros.stream()
                            .map(Mov::new)
                            .collect(Collectors.toList())
            );

            // Actualizar combo de responsables dinámicamente
            LinkedList<String> responsables = new LinkedList<>(usuarioDAO.getAllNombres());
            responsables.addFirst("Todos");
            cbResp.setItems(FXCollections.observableArrayList(responsables));
            cbResp.getSelectionModel().selectFirst();

        } catch (Exception e) {
            logger.error("Error al cargar los suministros", e);
            new Alert(Alert.AlertType.ERROR, "Error al cargar los suministros: " + e.getMessage()).showAndWait();
        }
    }


    /* ======= Navegación ======= */
    @FXML private void goDashboard()  { App.goTo("/fxml/dashboard_admin.fxml", "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()   { /* ya estás aquí */ }
    @FXML private void goHealth()     { App.goTo("/fxml/sanidad.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/produccion.fxml", "SIA Avitech — Producción"); }
    @FXML private void goReports()    { App.goTo("/fxml/reportes.fxml", "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()     { App.goTo("/fxml/alertas.fxml", "SIA Avitech — Alertas"); }
    @FXML private void goAudit()      { App.goTo("/fxml/auditoria.fxml", "SIA Avitech — Auditoría"); }
    @FXML private void goParams()     { App.goTo("/fxml/parametros.fxml", "SIA Avitech — Parámetros"); } // Ruta corregida
    @FXML private void goUsers()      { App.goTo("/fxml/usuarios.fxml", "SIA Avitech — Usuarios"); }
    @FXML private void goBackup()     { App.goTo("/fxml/respaldos.fxml", "SIA Avitech — Respaldos"); }
    @FXML private void onExit()       { App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión"); }

    /* ======= Acciones ======= */
    @FXML
    private void onEntrada() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_suministro_entrada.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Entrada de Suministro");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnEntrada.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            App.configureFullScreen(dialogStage);

            RegEntradaSuministroController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            ObservableList<String> responsables = FXCollections.observableArrayList(usuarioDAO.getAllNombres());
            controller.setResponsables(responsables);

            dialogStage.showAndWait();

            Suministro result = controller.getResult();
            if (result != null) {
                suministroDAO.insert(result);
                loadSuministros();
                applyFilter();
                refreshKpis();
            }
        } catch (Exception e) {
            logger.error("Error al abrir el diálogo de entrada", e);
            new Alert(Alert.AlertType.ERROR, "Error al abrir el diálogo de entrada: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onSalida() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_suministro_salida.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Registrar Salida de Suministro");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnSalida.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            App.configureFullScreen(dialogStage);

            RegSalidaSuministroController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            ObservableList<String> responsables = FXCollections.observableArrayList(usuarioDAO.getAllNombres());
            controller.setResponsables(responsables);

            dialogStage.showAndWait();

            Suministro result = controller.getResult();
            if (result != null) {
                suministroDAO.insert(result);
                loadSuministros();
                applyFilter();
                refreshKpis();
            }
        } catch (Exception e) {
            logger.error("Error al abrir el diálogo de salida", e);
            new Alert(Alert.AlertType.ERROR, "Error al abrir el diálogo de salida: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onVerStock()  {
        try {
            // Mostrar únicamente el stock calculado a partir de movimientos de suministros
            List<StockProduccion> stock = stockRepository.getStockSuministros();

            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/modal_stock_produccion.fxml"));
            Pane page = loader.load();

            StockProduccionController controller = loader.getController();
            controller.setStock(stock);
            // Ajustar el título del encabezado dentro del modal
            controller.setTitle("Stock de Suministros");

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            if (btnEntrada != null && btnEntrada.getScene() != null) {
                dialog.initOwner(btnEntrada.getScene().getWindow());
            }
            dialog.setTitle("Stock de Suministros");
            dialog.setScene(new Scene(page));
            App.configureFullScreen(dialog);
            dialog.showAndWait();

        } catch (Exception e) {
            logger.error("Error al cargar stock en producción", e);
            new Alert(Alert.AlertType.ERROR, "Error al cargar stock en producción: " + e.getMessage()).showAndWait();
        }
    }
    @FXML private void onMoverStock(){ /* flujo mover stock   */ }
    @FXML private void onExportar()  { /* export CSV/XLSX     */ }

    private void onVerItem(Mov m) {
        // En real: abrir detalle del ítem (o navegar a inventario con el ID)
        new Alert(Alert.AlertType.INFORMATION, "Ítem: " + m.item).showAndWait();
    }

    /* ======= Filtros ======= */
    @FXML
    private void applyFilter() {
        final String t = lower(txtSearch.getText());
        final String tipo = sel(cbTipo);
        final String resp = sel(cbResp);
        final LocalDate d1 = dpDesde.getValue();
        final LocalDate d2 = dpHasta.getValue();

        filtered.setAll(master.filtered(m ->
                (t.isEmpty() || m.itemLc.contains(t) || m.detallesLc.contains(t) || m.respLc.contains(t)) &&
                        ("Todos".equals(tipo) || m.tipo.equalsIgnoreCase(tipo)) &&
                        ("Todos".equals(resp) || m.responsable.equalsIgnoreCase(resp)) &&
                        (between(m.localDate, d1, d2))
        ));

        tblMovs.setItems(filtered);
        lblMostrando.setText(filtered.size() + " ítems");
    }

    private static boolean between(LocalDate f, LocalDate d1, LocalDate d2) {
        if (f == null) return true;
        boolean ok1 = (d1 == null) || !f.isBefore(d1);
        boolean ok2 = (d2 == null) || !f.isAfter(d2);
        return ok1 && ok2;
    }
    private static String lower(String s) { return s == null ? "" : s.toLowerCase().trim(); }
    private static String sel(ComboBox<String> cb) {
        String s = cb.getSelectionModel().getSelectedItem();
        return s == null ? "Todos" : s;
    }

    /* ======= KPIs ======= */
    private void refreshKpis() {
        long hoy = master.stream().filter(m -> m.localDate != null && m.localDate.equals(LocalDate.now())).count();
        long activos = master.stream().map(m -> m.item).distinct().count();
        
        // Placeholder: no hay datos de stock mínimo. Contamos salidas como antes.
        long bajos = master.stream().filter(m -> m.tipo.equalsIgnoreCase("Salida")).count(); 
        
        // Esto no es "valor de stock", es la suma de los valores de los movimientos.
        BigDecimal valor = master.stream()
                .filter(m -> m.valorTotal != null)
                .map(m -> m.valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Evitar el constructor obsoleto Locale(String,String) (deprecado en JDK 19+)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"));

        kpiMovHoy.setText(String.valueOf(hoy));
        kpiActivos.setText(String.valueOf(activos));
        kpiStockBajo.setText(String.valueOf(bajos));
        kpiValor.setText(currencyFormat.format(valor));
    }

    /* ======= DTO simple para la tabla ======= */
    public static class Mov {
        public final String fecha, item, cantidad, unidad, tipo, responsable, detalles, stock;
        public final String itemLc, detallesLc, respLc;
        public final LocalDate localDate;
        public final BigDecimal valorTotal;

        public Mov(Suministro s) {
            this.fecha = s.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.item = s.getItem();
            this.cantidad = String.valueOf(s.getCantidad());
            this.unidad = s.getUnidad();
            this.tipo = s.getTipo();
            this.responsable = s.getResponsable();

            String prov = s.getProveedor();
            String mot = s.getMotivo();
            List<String> partes = new LinkedList<>();
            if (prov != null && !prov.trim().isEmpty()) {
                partes.add("Proveedor: " + prov);
            }
            if (mot != null && !mot.trim().isEmpty()) {
                partes.add("Motivo: " + mot);
            }
            this.detalles = String.join(" | ", partes);
            this.stock = ""; // No disponible en la tabla Suministros

            this.itemLc = item.toLowerCase();
            this.detallesLc = detalles.toLowerCase();
            this.respLc = responsable.toLowerCase();
            this.localDate = s.getFecha();
            this.valorTotal = s.getValorTotal();
        }
    }
}
