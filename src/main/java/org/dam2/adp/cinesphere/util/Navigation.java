package org.dam2.adp.cinesphere.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.controller.MainController;

import java.io.IOException;

public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    public static void navigate(String fxml) {
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            // Fallback for navigation before main controller is set (e.g., login -> register)
            switchScene(fxml);
        }
    }

    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            System.err.println("Primary stage is not set.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/view/" + fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
