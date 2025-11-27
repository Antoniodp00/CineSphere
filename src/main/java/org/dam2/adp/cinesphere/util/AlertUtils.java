package org.dam2.adp.cinesphere.util;

import javafx.scene.control.Alert;

/**
 * Utilidades para mostrar alertas en la interfaz de usuario.
 */
public class AlertUtils {

    /**
     * Muestra una alerta de información.
     * @param msg el mensaje a mostrar.
     */
    public static void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    /**
     * Muestra una alerta de error.
     * @param msg el mensaje a mostrar.
     */
    public static void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }
}
