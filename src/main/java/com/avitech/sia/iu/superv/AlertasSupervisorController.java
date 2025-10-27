package com.avitech.sia.iu.superv;

import com.avitech.sia.App; // usa el mismo helper de navegación que ya tienes
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller base para la vista de Alertas.
 * Deja lista la navegación, filtros mínimos y la tabla con una columna de acción.
 * La carga real desde BD se conecta más adelante.
 */
public class AlertasSupervisorController {

    /* ====== sidebar / topbar ====== */
    @FXML private Label lblSystemStatus;
    @FXML private Label lblUserInfo;
    @FXML private Label lblHeader;

    /* ====== KPIs ====== */
    @FXML private Label kpiCritico;
    @FXML private Label kpiBajo;
    @FXML private Label kpiAdv;
    @FXML private Label kpiTotal;

    /* ====== Filtros ====== */
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbCriticidad;
    @FXML private ComboBox<String> cbUbicacion;

    /* ====== Tabla ====== */
    @FXML private TableView<ItemAlerta> tblAlertas;
    @FXML private TableColumn<ItemAlerta, String> colArticulo;
    @FXML private TableColumn<ItemAlerta, String> colCategoria;
    @FXML private TableColumn<ItemAlerta, String> colStockActual;
    @FXML private TableColumn<ItemAlerta, String> colStockMin;
    @FXML private TableColumn<ItemAlerta, String> colPorcentaje;
    @FXML private TableColumn<ItemAlerta, String> colDias;
    @FXML private TableColumn<ItemAlerta, String> colUbicacion;
    @FXML private TableColumn<ItemAlerta, String> colAccion;

    @FXML private Label lblCantidadTabla;
    @FXML private Label lblActualizado;
    @FXML private Label lblResCritico, lblResBajo, lblResAdvertencia;
    @FXML private Label lblBanner;

    private final ObservableList<ItemAlerta> master = FXCollections.observableArrayList();
    private final ObservableList<ItemAlerta> filtered = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Offline – MySQL Local");
        lblHeader.setText("Supervisor");
        lblUserInfo.setText("Supervisor");

        // Combos dummy
        cbCategoria.setItems(FXCollections.observableArrayList("Todas", "Alimentos", "Medicamentos", "Limpieza", "Vacunas", "Suplem."));
        cbCriticidad.setItems(FXCollections.observableArrayList("Todas", "Crítico", "Bajo", "Advertencia"));
        cbUbicacion.setItems(FXCollections.observableArrayList("Todas", "Almacén Principal", "Farmacia", "Almacén Secundario", "Patio de Materiales"));

        cbCategoria.getSelectionModel().selectFirst();
        cbCriticidad.getSelectionModel().selectFirst();
        cbUbicacion.getSelectionModel().selectFirst();

        // Tabla
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("diasRestantes"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        // Columna de acción con botón "Ir al ítem"
        colAccion.setCellValueFactory(param -> new SimpleStringProperty("Ir al ítem"));
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink("Ir al ítem");
            { link.setOnAction(e -> onGoItem(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : link);
                setText(null);
            }
        });

        // Datos de ejemplo (hasta conectar BD)
        seedSample();

        // Mostrar filtrado inicial
        applyFilter();

        // KPIs
        refreshKpis();

        // Fecha de actualización
        lblActualizado.setText("Actualizado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    /* ================== Navegación ================== */
    @FXML private void goDashboard()  { App.goTo("/fxml/superv/dashboard_super.fxml", "SIA Avitech — SUPERVISOR"); }
    @FXML private void goSupplies()   { App.goTo("/fxml/superv/suministros_super.fxml", "SIA Avitech — Suministros"); }
    @FXML private void goHealth()     { App.goTo("/fxml/superv/sanidad_super.fxml", "SIA Avitech — Sanidad"); }
    @FXML private void goProduction() { App.goTo("/fxml/superv/produccion_super.fxml", "SIA Avitech — Producción"); }
    // @FXML private void goReports()    { App.goTo("/fxml/superv/reportes_super.fxml", "SIA Avitech — Reportes"); } // REMOVED
    @FXML private void goAlerts()     { App.goTo("/fxml/superv/alertas_super.fxml", "SIA Avitech — Alertas"); }
    // @FXML private void goAudit()      { App.goTo("/fxml/superv/auditoria_super.fxml", "SIA Avitech — Auditoría"); } // REMOVED
    // @FXML private void goParams()     { App.goTo("/fxml/superv/parametros_super.fxml", "SIA Avitech — Parámetros"); } // REMOVED
    // @FXML private void goUsers()      { App.goTo("/fxml/superv/usuarios_super.fxml", "SIA Avitech — Usuarios"); } // REMOVED
    // @FXML private void goBackup()     { App.goTo("/fxml/superv/respaldos_super.fxml", "SIA Avitech — Respaldos"); } // REMOVED
    @FXML private void onExit() {      App.goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");}

    /* ================== Acciones UI ================== */

    @FXML
    private void onRefresh() {
        // En real: recargar desde servicio/DAO y recalcular KPIs.
        applyFilter();
        refreshKpis();
        lblActualizado.setText("Actualizado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    @FXML
    private void onVerCriticos() {
        cbCriticidad.getSelectionModel().select("Crítico");
        applyFilter();
    }

    @FXML
    private void onAplicarFiltros() { applyFilter(); }

    @FXML
    private void onLimpiarFiltros() {
        txtSearch.clear();
        cbCategoria.getSelectionModel().selectFirst();
        cbCriticidad.getSelectionModel().selectFirst();
        cbUbicacion.getSelectionModel().selectFirst();
        applyFilter();
    }

    private void onGoItem(ItemAlerta item) {
        // En real: navegación al detalle del artículo en Suministros con el ID.
        goSupplies();
    }

    /* ================== Lógica local (dummy) ================== */

    private void seedSample() {
        master.setAll(
                new ItemAlerta("Alimento Balanceado Postura", "Alimentos", "450 kg", "1000 kg", "45%", "2 días", "Almacén Principal", "Crítico"),
                new ItemAlerta("Enrofloxacina 10%", "Medicamentos", "20 frascos", "40 frascos", "50%", "15 días", "Farmacia", "Bajo"),
                new ItemAlerta("Vitaminas AD3E", "Vitaminas", "12 frascos", "25 frascos", "48%", "8 días", "Farmacia", "Bajo"),
                new ItemAlerta("Alimento Pre-inicio", "Alimentos", "75 kg", "200 kg", "38%", "4 días", "Almacén Secundario", "Crítico"),
                new ItemAlerta("Desinfectante Clorado", "Limpieza", "30 litros", "50 litros", "60%", "12 días", "Almacén de Químicos", "Advertencia"),
                new ItemAlerta("Vacuna Newcastle", "Vacunas", "100 dosis", "450 dosis", "22%", "6 días", "Refrigerador Farmacia", "Crítico"),
                new ItemAlerta("Vitro para Carne", "Molinos", "20 sacos", "80 sacos", "25%", "3 días", "Patio de Materiales", "Crítico"),
                new ItemAlerta("Probióticos", "Suplementos", "6 envases", "15 envases", "40%", "10 días", "Farmacia", "Bajo")
        );
    }

    private void applyFilter() {
        String text = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String cat  = sel(cbCategoria);
        String cri  = sel(cbCriticidad);
        String ubi  = sel(cbUbicacion);

        filtered.setAll(master.filtered(it ->
                (text.isEmpty() || it.articulo.toLowerCase().contains(text) || it.categoria.toLowerCase().contains(text) || it.ubicacion.toLowerCase().contains(text)) &&
                        (cat.equals("Todas") || it.categoria.equalsIgnoreCase(cat)) &&
                        (cri.equals("Todas") || it.nivel.equalsIgnoreCase(cri)) &&
                        (ubi.equals("Todas") || it.ubicacion.equalsIgnoreCase(ubi))
        ));

        tblAlertas.setItems(filtered);
        lblCantidadTabla.setText("(" + filtered.size() + ")");
    }

    private String sel(ComboBox<String> cb) {
        var s = cb.getSelectionModel().getSelectedItem();
        return (s == null) ? "Todas" : s;
    }

    private void refreshKpis() {
        long crit = master.stream().filter(i -> i.nivel.equalsIgnoreCase("Crítico")).count();
        long bajo = master.stream().filter(i -> i.nivel.equalsIgnoreCase("Bajo")).count();
        long adv  = master.stream().filter(i -> i.nivel.equalsIgnoreCase("Advertencia")).count();

        kpiCritico.setText(String.valueOf(crit));
        kpiBajo.setText(String.valueOf(bajo));
        kpiAdv.setText(String.valueOf(adv));
        kpiTotal.setText(String.valueOf(master.size()));

        lblResCritico.setText("Crítico — " + crit + " artículos");
        lblResBajo.setText("Bajo — " + bajo + " artículos");
        lblResAdvertencia.setText("Advertencia — " + adv + " artículos");

        lblBanner.setText(crit + " artículo(s) en stock crítico requieren atención inmediata");
    }

    /* ================== DTO sencillo para la tabla ================== */
    public static class ItemAlerta {
        public final String articulo, categoria, stockActual, stockMinimo, porcentaje, diasRestantes, ubicacion, nivel;
        public ItemAlerta(String articulo, String categoria, String stockActual, String stockMinimo,
                          String porcentaje, String diasRestantes, String ubicacion, String nivel) {
            this.articulo = articulo; this.categoria = categoria; this.stockActual = stockActual;
            this.stockMinimo = stockMinimo; this.porcentaje = porcentaje; this.diasRestantes = diasRestantes;
            this.ubicacion = ubicacion; this.nivel = nivel;
        }
        public String getArticulo() { return articulo; }
        public String getCategoria() { return categoria; }
        public String getStockActual() { return stockActual; }
        public String getStockMinimo() { return stockMinimo; }
        public String getPorcentaje() { return porcentaje; }
        public String getDiasRestantes() { return diasRestantes; }
        public String getUbicacion() { return ubicacion; }
        public String getNivel() { return nivel; }
    }
}
