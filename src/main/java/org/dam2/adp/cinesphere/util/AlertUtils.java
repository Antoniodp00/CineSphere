package org.dam2.adp.cinesphere.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para mostrar alertas en la interfaz de usuario.
 */
public class AlertUtils {

    private static final Logger logger = Logger.getLogger(AlertUtils.class.getName());

    /**
     * Muestra una alerta de error.
     * Si el mensaje es largo, usa un área de texto desplazable.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    public static void error(String mensaje) {
        mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error del Sistema", "Ha ocurrido un error", mensaje);
    }

    /**
     * Muestra una alerta de información simple.
     *
     * @param mensaje El mensaje informativo.
     */
    public static void info(String mensaje) {
        // Para info simple, el alert estándar suele bastar, pero lo hacemos redimensionable por si acaso
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * Método interno para construir una alerta robusta con TextArea.
     */
    private static void mostrarAlertaPersonalizada(Alert.AlertType tipo, String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);

        // Hacemos que el diálogo sea redimensionable
        alert.setResizable(true);

        // Crear un TextArea para el contenido
        // Esto permite seleccionar texto, copiarlo y hacer scroll
        TextArea textArea = new TextArea(contenido);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        // Configuración de tamaño para que se vea bien
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        // Insertamos el TextArea en un contenedor expandible
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("Detalle del mensaje:"), 0, 0);
        expContent.add(textArea, 0, 1);

        // Reemplazamos el panel de diálogo estándar
        alert.getDialogPane().setContent(expContent);

        // Truco para asegurar que la ventana se centre sobre la aplicación
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
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
