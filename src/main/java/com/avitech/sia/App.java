package com.avitech.sia;

import com.avitech.sia.db.DB;
import com.avitech.sia.db.UsuarioDAO;
import javafx.application.Application;
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
                System.err.println("Warning: CSS file not found: /css/theme.css");
            } else {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            mainStage.setTitle(title);
            mainStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cambiar de escena: " + fxml, e);
        }
    }
}
