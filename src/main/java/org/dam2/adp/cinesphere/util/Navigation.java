package org.dam2.adp.cinesphere.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.controller.MainController;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para la navegación entre escenas en la aplicación.
 */
public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;
    private static final Logger logger = Logger.getLogger(Navigation.class.getName());

    private static final String STYLES_PATH = "/style.css";

    /**
     * Establece el escenario principal de la aplicación.
     * @param stage El escenario principal.
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
        logger.log(Level.INFO, "Primary stage establecido.");
    }

    /**
     * Establece el controlador principal de la aplicación.
     * @param controller El controlador principal.
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
        logger.log(Level.INFO, "Main controller establecido.");
    }

    /**
     * Navega a una vista dentro del área de contenido principal.
     * @param fxml El archivo FXML de la vista a cargar.
     */
    public static void navigate(String fxml) {
        logger.log(Level.INFO, "Navegando a sub-vista: " + fxml);
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            logger.log(Level.WARNING, "MainController no está establecido. Usando switchScene como fallback.");
            switchScene(fxml);
        }
    }

    /**
     * Cambia la escena actual por una nueva e inyecta el CSS personalizado.
     * @param fxml El archivo FXML de la escena a cargar.
     */
    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            logger.log(Level.SEVERE, "Primary stage no está establecido. No se puede cambiar de escena.");
            return;
        }
        try {
            logger.log(Level.INFO, "Cambiando a la escena completa: " + fxml);

            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/view/" + fxml));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            URL cssUrl = Navigation.class.getResource(STYLES_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                logger.log(Level.INFO, "Estilos personalizados cargados: " + STYLES_PATH);
            } else {
                logger.log(Level.WARNING, "No se encontró el archivo de estilos en: " + STYLES_PATH);
            }

            primaryStage.setScene(scene);

            // Cuando se carga una vista que no es de login/registro, se asume que es la principal.
            if (fxml.equals("login.fxml") || fxml.equals("register.fxml")) {
                primaryStage.setResizable(false);
                primaryStage.setMaximized(false); // Salir del modo maximizado si se vuelve al login
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
            } else {
                // Para la vista principal (MainController), maximizar la ventana.
                primaryStage.setResizable(true);
                primaryStage.setMaximized(true);
            }

            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al cargar FXML para la escena " + fxml, e);
        }
    }
}
