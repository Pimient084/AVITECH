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

import java.io.File; // Para obtener el tamaño del archivo
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador base para la vista de Respaldos.
 * - Navegación lista
 * - KPIs y banner con valores dummy
 * - Tabla con acciones (descargar/restaurar/borrar)
 * Conectar a la BD más adelante.
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
            private final Hyperlink btnDesc = new Hyperlink("Descargar");
            private final Hyperlink btnRest = new Hyperlink("Restaurar");
            private final Hyperlink btnDel  = new Hyperlink("Borrar");
            private final HBox box = new HBox(12, btnDesc, btnRest, btnDel);
            {
                btnDesc.setOnAction(e -> onDescargar(getItemAtRow()));
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

        // datos de ejemplo
        seed();

        tblBackups.setItems(master);
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
        String backupFileName = "";
        String backupStatus = "Error";
        String backupSize = "0 KB";
        
        try {
            boolean success = BackupUtil.createBackup();
            if (success) {
                backupStatus = "Completado";
                // Intentar obtener el nombre del archivo de respaldo y su tamaño
                // Esto asume que BackupUtil.createBackup() imprime el path completo del archivo
                // Para una implementación más robusta, BackupUtil debería retornar el path del archivo creado
                File backupDir = new File("C:/MySQLBackups");
                File[] files = backupDir.listFiles((dir, name) -> name.startsWith("avitech_sia_db_backup_") && name.endsWith(".sql"));
                if (files != null && files.length > 0) {
                    // Encontrar el archivo más reciente
                    File latestFile = null;
                    long lastModified = Long.MIN_VALUE;
                    for (File file : files) {
                        if (file.lastModified() > lastModified) {
                            latestFile = file;
                            lastModified = file.lastModified();
                        }
                    }
                    if (latestFile != null) {
                        backupFileName = latestFile.getName();
                        long fileSizeKB = latestFile.length() / 1024; // Tamaño en KB
                        if (fileSizeKB > 1024) {
                            backupSize = String.format("%.1f MB", fileSizeKB / 1024.0);
                        } else {
                            backupSize = fileSizeKB + " KB";
                        }
                    }
                }
                showAlert(AlertType.INFORMATION, "Respaldo Exitoso", "El respaldo de la base de datos se creó correctamente en C:/MySQLBackups.");
            } else {
                showAlert(AlertType.ERROR, "Error en Respaldo", "No se pudo crear el respaldo de la base de datos. Revisa la consola para más detalles.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error en Respaldo", "Ocurrió una excepción al intentar crear el respaldo: " + e.getMessage());
            e.printStackTrace();
        }

        var now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));
        master.add(0, new BackupItem(backupFileName.isEmpty() ? "avitech_manual_" + System.currentTimeMillis() + ".sql" : backupFileName,
                now, "Manual", backupSize, backupStatus));
        tblBackups.refresh();
        refreshKpis();
        refreshBanner();
    }

    @FXML private void onActualizar() {
        // futuro: volver a consultar servicio
        tblBackups.refresh();
        refreshKpis();
    }

    @FXML private void onRestaurarGeneral() {
        // placeholder para abrir diálogo de restauración general
        info("Restauración general (placeholder)");
    }

    /* ==================== Acciones por fila ==================== */
    private void onDescargar(BackupItem it) { if (it != null) info("Descargando: " + it.archivo()); }
    private void onRestaurar(BackupItem it) { if (it != null) info("Restaurando: " + it.archivo()); }
    private void onBorrar(BackupItem it) {
        if (it == null) return;
        master.remove(it);
        refreshKpis();
    }

    /* ==================== Helpers ==================== */
    private void seed() {
        master.setAll(
                new BackupItem("avitech_auto_20241007_020000.bak", "07/10/2024, 02:00", "Automático", "2.4 GB", "Completado"),
                new BackupItem("avitech_auto_20241006_020000.bak", "06/10/2024, 02:00", "Automático", "2.3 GB", "Completado"),
                new BackupItem("avitech_manual_actualizacion_20241005.bak", "05/10/2024, 14:30", "Manual", "2.2 GB", "Completado"),
                new BackupItem("avitech_auto_20241003_020000.bak", "03/10/2024, 02:00", "Automático", "2.0 GB", "Error"),
                new BackupItem("avitech_auto_20241002_020000.bak", "02/10/2024, 02:00", "Automático", "2.1 GB", "Completado")
        );
    }

    private void refreshKpis() {
        long total = master.size();
        long ok    = master.stream().filter(b -> b.estado().equalsIgnoreCase("Completado")).count();
        long err   = master.stream().filter(b -> b.estado().equalsIgnoreCase("Error")).count();
        double spaceGb = master.stream().mapToDouble(b -> parseGb(b.tamano())).sum();

        kpiTotal.setText(String.valueOf(total));
        kpiOk.setText(String.valueOf(ok));
        kpiErr.setText(String.valueOf(err));
        kpiSpace.setText(String.format("%.1f GB", spaceGb));
    }

    private void refreshBanner() {
        var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
        lblAutoUltimo.setText("Último respaldo: " + LocalDateTime.now().minusDays(1).withHour(2).withMinute(0).format(fmt));
        lblAutoProximo.setText("Próximo respaldo: " + LocalDateTime.now().plusDays(1).withHour(2).withMinute(0).format(fmt));
        lblAutoTasa.setText("Tasa de éxito: 95.6% (4/5 respaldos)");
        lblAutoEstado.setText("Estado: Activo");
    }

    private double parseGb(String s) {
        try {
            var clean = s.trim().toLowerCase().replace("gb","").trim().replace(",",".");
            return Double.parseDouble(clean);
        } catch (Exception e) { return 0d; }
    }

    private void info(String msg) {
        // en real: usar diálogo propio
        System.out.println(msg);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* DTO record para mayor claridad */
    public record BackupItem(String archivo, String fecha, String tipo, String tamano, String estado) {}
}
