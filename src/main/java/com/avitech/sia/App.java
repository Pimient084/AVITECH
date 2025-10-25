package com.avitech.sia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    private static Stage primaryStage;

    public static Stage PrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Cargar fuentes (asegúrate de tenerlas en resources/fonts/)
        Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-SemiBold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"), 12);

        primaryStage = stage;

        // Cargar la pantalla de inicio (login)
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(App.class.getResource("/css/theme.css").toExternalForm());

        stage.setTitle("SIA Avitech — Inicio de sesión");
        stage.setWidth(1280); // Set default width
        stage.setHeight(800); // Set default height
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método público para cambiar de escena (pantalla)
     * desde cualquier controlador.
     * @param fxmlPath ruta del archivo FXML
     * @param title título de la ventana
     */
    public static void goTo(String fxmlPath, String title) {
        try {
            boolean wasMaximized = primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(App.class.getResource("/css/theme.css").toExternalForm());

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(1280); // Enforce standard width
                primaryStage.setHeight(800); // Enforce standard height
            }
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cambiar de escena: " + fxmlPath, e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
