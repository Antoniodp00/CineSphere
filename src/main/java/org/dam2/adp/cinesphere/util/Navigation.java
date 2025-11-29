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

    // --- CAMBIO 1: DEFINIR LA RUTA DE TU CSS PERSONALIZADO ---
    // Asegúrate de que "styles.css" esté en la raíz de src/main/resources
    // Si lo tienes en una carpeta, cambia esto a "/css/styles.css" o "/org/dam2/.../styles.css"
    private static final String STYLES_PATH = "/style.css";

    public static void setStage(Stage stage) {
        primaryStage = stage;
        logger.log(Level.INFO, "Primary stage establecido.");
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
        logger.log(Level.INFO, "Main controller establecido.");
    }

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
     * Cambia la escena actual por una nueva e INYECTA EL CSS PERSONALIZADO.
     */
    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            logger.log(Level.SEVERE, "Primary stage no está establecido. No se puede cambiar de escena.");
            return;
        }
        try {
            logger.log(Level.INFO, "Cambiando a la escena completa: " + fxml);

            // Nota: Asumo que tus vistas están en /view/. Ajusta si es diferente.
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/view/" + fxml));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // --- CAMBIO 2: CARGAR EL CSS PERSONALIZADO (OVERRIDE) ---
            URL cssUrl = Navigation.class.getResource(STYLES_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                logger.log(Level.INFO, "Estilos personalizados cargados: " + STYLES_PATH);
            } else {
                logger.log(Level.WARNING, "No se encontró el archivo de estilos en: " + STYLES_PATH);
            }
            // --------------------------------------------------------

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al cargar FXML para la escena " + fxml, e);
        }
    }
}