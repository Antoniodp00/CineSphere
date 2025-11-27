package org.dam2.adp.cinesphere.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.controller.MainController;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para la navegación entre escenas en la aplicación.
 */
public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;
    private static final Logger logger = Logger.getLogger(Navigation.class.getName());

    /**
     * Establece el escenario principal de la aplicación.
     * @param stage el escenario principal.
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
        logger.log(Level.INFO, "Primary stage establecido.");
    }

    /**
     * Establece el controlador principal de la aplicación.
     * @param controller el controlador principal.
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
        logger.log(Level.INFO, "Main controller establecido.");
    }

    /**
     * Navega a una nueva vista dentro del controlador principal.
     * @param fxml el archivo FXML de la vista a cargar.
     */
    public static void navigate(String fxml) {
        logger.log(Level.INFO, "Navegando a: " + fxml);
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            logger.log(Level.WARNING, "MainController no está establecido. Usando switchScene como fallback.");
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
            logger.log(Level.SEVERE, "Primary stage no está establecido. No se puede cambiar de escena.");
            return;
        }
        try {
            logger.log(Level.INFO, "Cambiando a la escena: " + fxml);
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/view/" + fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al cargar FXML para la escena " + fxml, e);
        }
    }
}
