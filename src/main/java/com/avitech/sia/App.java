package com.avitech.sia;

import com.avitech.sia.db.DB;
import com.avitech.sia.db.UsuarioDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Stage mainStage;
    public static UsuarioDAO.Usuario currentUser; // To hold the logged-in user

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        // The DB class is initialized statically, no need to call a method.
        goTo("/fxml/login.fxml", "SIA Avitech — Inicio de sesión");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Configura una ventana para pantalla completa (solo no modal) o como diálogo (modal) sin maximizar.
     * - Para Stage no modal (ventana principal): habilita maximizado.
     * - Para Stage modal (diálogos): NO maximiza, ajusta a escena y centra.
     */
    public static void configureFullScreen(Stage stage) {
        if (stage == null) return;
        try {
            stage.setFullScreen(false);
            stage.setResizable(true);

            if (stage.getModality() == Modality.NONE) {
                // Ventana principal u otras no modales: permitir maximizado
                stage.setMaximized(true);
                Platform.runLater(() -> {
                    try { stage.setMaximized(true); } catch (Exception ignored) {}
                });
            } else {
                // Diálogo modal: no maximizar
                stage.setMaximized(false);
                try { stage.sizeToScene(); } catch (Exception ignored) {}
                try { stage.centerOnScreen(); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public static void goTo(String fxml, String title) {
        Runnable nav = () -> {
            if (mainStage == null) {
                System.err.println("Main stage no inicializado. No se puede cambiar de escena.");
                return;
            }
            try {
                URL fxmlUrl = App.class.getResource(fxml);
                if (fxmlUrl == null) {
                    throw new IOException("FXML file not found: " + fxml);
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();

                // Asegurar que siempre hay una Scene y reusar la existente para no perder el estado maximizado
                Scene scene = mainStage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    mainStage.setScene(scene);
                    // Maximizar en la primera carga
                    configureFullScreen(mainStage);
                } else {
                    // Reforzar estado de ventana (mantener maximizado si ya lo estaba) ANTES de tocar el root
                    boolean wasMaximized = mainStage.isMaximized();
                    scene.setRoot(root);
                    mainStage.setResizable(true);
                    if (wasMaximized) {
                        try { mainStage.setMaximized(true); } catch (Exception ignored) {}
                        Platform.runLater(() -> { try { mainStage.setMaximized(true); } catch (Exception ignored) {} });
                    }
                }

                // Aplicar CSS de tema (una sola vez)
                URL cssUrl = App.class.getResource("/css/theme.css");
                if (cssUrl == null) {
                    // Try without leading slash as fallback
                    cssUrl = App.class.getResource("css/theme.css");
                }
                if (cssUrl == null) {
                    System.err.println("Warning: CSS file not found at /css/theme.css or css/theme.css");
                } else {
                    String css = cssUrl.toExternalForm();
                    if (!scene.getStylesheets().contains(css)) {
                        scene.getStylesheets().add(css);
                        System.out.println("Applied CSS: " + css);
                    } else {
                        System.out.println("CSS already applied: " + css);
                    }
                }

                mainStage.setTitle(title);

                if (!mainStage.isShowing()) mainStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                // Mostrar alerta en la UI thread
                Platform.runLater(() -> {
                    try {
                        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                                "Error al cambiar de escena: " + e.getMessage()).showAndWait();
                    } catch (Exception ex) {
                        // En caso de fallo al mostrar el diálogo, imprimir sólo el error
                        ex.printStackTrace();
                    }
                });
            }
        };

        if (Platform.isFxApplicationThread()) {
            nav.run();
        } else {
            Platform.runLater(nav);
        }
    }
}
