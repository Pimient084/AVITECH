package com.avitech.sia.iu;

import com.avitech.sia.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class ModalUtil {

    public static <T> T openModal(Node owner, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Window ownerWindow = owner.getScene().getWindow();
            dialogStage.initOwner(ownerWindow);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            T controller = loader.getController();

            // Pass the stage to the controller if it has a setDialogStage method
            try {
                controller.getClass().getMethod("setDialogStage", Stage.class).invoke(controller, dialogStage);
            } catch (Exception e) {
                // Method not found, ignore
            }

            dialogStage.showAndWait();

            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
