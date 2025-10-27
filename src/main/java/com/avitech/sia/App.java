package com.avitech.sia;

import com.avitech.sia.db.DB;
import com.avitech.sia.db.UsuarioDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
                Scene scene = new Scene(root);

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
                mainStage.setScene(scene);
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
