package com.avitech.sia.iu;

import com.avitech.sia.App;
import com.avitech.sia.db.BackupUtil; // Importar BackupUtil
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType; // Importar AlertType
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File; // Para obtener el tamaño del archivo
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

/**
 * Controlador base para la vista de Respaldos.
 * - Navegación lista
 * - KPIs y banner con valores reales
 * - Tabla con acciones (abrir/restaurar/borrar)
 */
public class RespaldosController {

    // topbar
    @FXML private Label lblSystemStatus;
    @FXML private Label lblHeader;
    @FXML private Label lblUserInfo;

    // banner auto
    @FXML private Label lblAutoUltimo, lblAutoProximo, lblAutoTasa, lblAutoEstado;

    // KPIs
    @FXML private Label kpiTotal, kpiOk, kpiErr, kpiSpace;

    // tabla
    @FXML private TableView<BackupItem> tblBackups;
    @FXML private TableColumn<BackupItem, String> colArchivo, colFecha, colTipo, colTam, colEstado, colAcciones;

    private final ObservableList<BackupItem> master = FXCollections.observableArrayList();

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    @FXML
    private void initialize() {
        lblSystemStatus.setText("Sistema Offline – MySQL Local");
        lblHeader.setText("Administrador");
        lblUserInfo.setText("Administrador");

        // columnas
        colArchivo.setCellValueFactory(new PropertyValueFactory<>("archivo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTam.setCellValueFactory(new PropertyValueFactory<>("tamano"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colAcciones.setCellValueFactory(param -> new SimpleStringProperty("acciones"));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Hyperlink btnAbrir = new Hyperlink("Abrir");
            private final Hyperlink btnRest = new Hyperlink("Restaurar");
            private final Hyperlink btnDel  = new Hyperlink("Borrar");
            private final HBox box = new HBox(12, btnAbrir, btnRest, btnDel);
            {
                btnAbrir.setOnAction(e -> onAbrirEnExplorer(getItemAtRow()));
                btnRest.setOnAction(e -> onRestaurar(getItemAtRow()));
                btnDel.setOnAction(e -> onBorrar(getItemAtRow()));
            }
            private BackupItem getItemAtRow() { return getIndex() >= 0 && getIndex() < tblBackups.getItems().size()
                    ? tblBackups.getItems().get(getIndex()) : null; }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });

        tblBackups.setItems(master);

        // cargar historial real
        loadHistory();
        refreshKpis();
        refreshBanner();
    }

    /* ==================== NAV ==================== */
    @FXML private void onExit()        { App.goTo("/fxml/login.fxml",            "SIA Avitech — Inicio de sesión"); }
    @FXML private void goDashboard()   { App.goTo("/fxml/dashboard_admin.fxml",  "SIA Avitech — ADMIN"); }
    @FXML private void goSupplies()    { App.goTo("/fxml/suministros.fxml",      "SIA Avitech — Suministros"); }
    @FXML private void goHealth()      { App.goTo("/fxml/sanidad.fxml",          "SIA Avitech — Sanidad"); }
    @FXML private void goProduction()  { App.goTo("/fxml/produccion.fxml",       "SIA Avitech — Producción"); }
    @FXML private void goReports()     { App.goTo("/fxml/reportes.fxml",         "SIA Avitech — Reportes"); }
    @FXML private void goAlerts()      { App.goTo("/fxml/alertas.fxml",          "SIA Avitech — Alertas"); }
    @FXML private void goAudit()       { App.goTo("/fxml/auditoria.fxml",        "SIA Avitech — Auditoría"); }
    @FXML private void goParams()      { App.goTo("/fxml/Parametros/parametros_unidades.fxml", "SIA Avitech — Parámetros"); }
    @FXML private void goUsers()       { App.goTo("/fxml/usuarios.fxml",         "SIA Avitech — Usuarios"); }

    /* ==================== Acciones Generales ==================== */
    @FXML private void onNuevoRespaldo() {
        try {
            File file = BackupUtil.createBackupAndReturnFile();
            if (file != null) {
                showAlert(AlertType.INFORMATION, "Respaldo Exitoso", "Respaldo creado en: " + file.getAbsolutePath());
                // añadir a la tabla
                master.add(0, buildItemFromFile(file, "Manual", "Completado"));
                tblBackups.refresh();
                refreshKpis();
                refreshBanner();
            } else {
                showAlert(AlertType.ERROR, "Error en Respaldo", "No se pudo crear el respaldo de la base de datos.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error en Respaldo", "Excepción al crear respaldo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onActualizar() {
        loadHistory();
        tblBackups.refresh();
        refreshKpis();
    }

    @FXML private void onRestaurarGeneral() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo .sql para restaurar");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL", "*.sql"));
        File dir = new File(BackupUtil.getBackupDir());
        if (dir.exists()) fc.setInitialDirectory(dir);
        File chosen = fc.showOpenDialog(tblBackups.getScene().getWindow());
        if (chosen != null) restoreWithConfirm(chosen);
    }

    /* ==================== Acciones por fila ==================== */
    private void onAbrirEnExplorer(BackupItem it) {
        if (it == null) return;
        try {
            // Abrir en explorer y seleccionar el archivo
            new ProcessBuilder("explorer.exe", "/select,", it.file.getAbsolutePath()).start();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Abrir archivo", "No se pudo abrir el explorador: " + e.getMessage());
        }
    }

    private void onRestaurar(BackupItem it) {
        if (it == null) return;
        restoreWithConfirm(it.file);
    }

    private void onBorrar(BackupItem it) {
        if (it == null) return;
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Eliminar respaldo");
        alert.setHeaderText("¿Eliminar respaldo?\n" + it.archivo());
        alert.setContentText("Esta acción eliminará el archivo del disco.");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (it.file.delete()) {
                master.remove(it);
                refreshKpis();
            } else {
                showAlert(AlertType.ERROR, "Eliminar", "No se pudo eliminar el archivo. Revisa permisos.");
            }
        }
    }

    private void restoreWithConfirm(File file) {
        Alert c = new Alert(AlertType.CONFIRMATION);
        c.setTitle("Restaurar Base de Datos");
        c.setHeaderText("Se restaurará la BD desde:\n" + file.getName());
        c.setContentText("Esto sobrescribirá datos actuales. ¿Continuar?");
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            boolean ok = BackupUtil.restoreBackup(file);
            if (ok) showAlert(AlertType.INFORMATION, "Restaurar", "Restauración completada.");
            else showAlert(AlertType.ERROR, "Restaurar", "Fallo al restaurar. Revisa consola.");
        }
    }

    /* ==================== Helpers ==================== */
    private void loadHistory() {
        master.clear();
        File[] files = BackupUtil.listBackups();
        Arrays.stream(files)
                .map(f -> buildItemFromFile(f, deriveTipo(f.getName()), "Completado"))
                .forEach(master::add);
    }

    private BackupItem buildItemFromFile(File f, String tipo, String estado) {
        String fecha = formatTimestamp(f.lastModified());
        String tam = humanSize(f.length());
        return new BackupItem(f.getName(), fecha, tipo, tam, estado, f);
    }

    private String deriveTipo(String name) {
        String n = name.toLowerCase();
        if (n.contains("auto")) return "Automático";
        return "Manual";
    }

    private String formatTimestamp(long millis) {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        return dt.format(FECHA_FMT);
    }

    private String humanSize(long bytes) {
        double b = bytes;
        if (b < 1024) return String.format("%d B", (long)b);
        double kb = b / 1024.0;
        if (kb < 1024) return String.format("%.0f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    private void refreshKpis() {
        long total = master.size();
        // Asumimos estado "Completado" para archivos existentes
        long ok = total;
        long err = 0;
        long totalBytes = Arrays.stream(BackupUtil.listBackups()).mapToLong(File::length).sum();
        String space = humanSize(totalBytes);

        kpiTotal.setText(String.valueOf(total));
        kpiOk.setText(String.valueOf(ok));
        kpiErr.setText(String.valueOf(err));
        kpiSpace.setText(space.replace(" MB", " MB").replace(" GB", " GB"));
    }

    private void refreshBanner() {
        File[] files = BackupUtil.listBackups();
        String ultimo = files.length > 0 ? formatTimestamp(files[0].lastModified()) : "—";
        lblAutoUltimo.setText("Último respaldo: " + ultimo);
        // Próximo respaldo (placeholder: +24h desde último o desde ahora)
        long base = files.length > 0 ? files[0].lastModified() : System.currentTimeMillis();
        String proximo = formatTimestamp(base + 24L*60*60*1000);
        lblAutoProximo.setText("Próximo respaldo: " + proximo);
        lblAutoTasa.setText("Tasa de éxito: N/A");
        lblAutoEstado.setText("Estado: Manual");
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* DTO record para mayor claridad */
    public record BackupItem(String archivo, String fecha, String tipo, String tamano, String estado, File file) {}
}
