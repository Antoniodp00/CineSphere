package org.dam2.adp.cinesphere.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.controller.MainController;

import java.io.IOException;

/**
 * Utilidades para la navegación entre escenas en la aplicación.
 */
public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;

    /**
     * Establece el escenario principal de la aplicación.
     * @param stage el escenario principal.
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Establece el controlador principal de la aplicación.
     * @param controller el controlador principal.
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Navega a una nueva vista dentro del controlador principal.
     * @param fxml el archivo FXML de la vista a cargar.
     */
    public static void navigate(String fxml) {
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            // Fallback for navigation before main controller is set (e.g., login -> register)
            switchScene(fxml);
        }
    }

    /**
     * Cambia la escena actual por una nueva.
     * @param fxml el archivo FXML de la nueva escena.
     */
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
