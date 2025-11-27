package org.dam2.adp.cinesphere.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para mostrar alertas en la interfaz de usuario.
 */
public class AlertUtils {

    private static final Logger logger = Logger.getLogger(AlertUtils.class.getName());

    /**
     * Muestra una alerta de información.
     * @param msg el mensaje a mostrar.
     */
    public static void info(String msg) {
        logger.log(Level.INFO, "Mostrando alerta de información: " + msg);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    /**
     * Muestra una alerta de error.
     * @param msg el mensaje a mostrar.
     */
    public static void error(String msg) {
        logger.log(Level.SEVERE, "Mostrando alerta de error: " + msg);
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    /**
     * Muestra una alerta de confirmación y espera la respuesta del usuario.
     * @param title El título de la ventana de diálogo.
     * @param header El texto de la cabecera.
     * @param content El mensaje principal del diálogo.
     * @return true si el usuario presiona OK, false en caso contrario.
     */
    public static boolean confirmation(String title, String header, String content) {
        logger.log(Level.INFO, "Mostrando alerta de confirmación: " + header);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
