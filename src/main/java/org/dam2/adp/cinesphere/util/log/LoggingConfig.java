package org.dam2.adp.cinesphere.util.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase de configuración para el sistema de logging de la aplicación.
 * Configura un formateador personalizado para añadir colores a la salida de la consola.
 */
public class LoggingConfig {

    /**
     * Inicializa la configuración del logger.
     * Este método debe ser llamado una sola vez al inicio de la aplicación.
     */
    public static void setup() {
        Logger rootLogger = Logger.getLogger("");

        // Eliminar los manejadores por defecto para evitar logs duplicados
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
            }
        }


        rootLogger.setLevel(Level.INFO);


        Logger.getLogger("org.dam2.adp.cinesphere").setLevel(Level.ALL);


        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter());
        consoleHandler.setLevel(Level.ALL);
        rootLogger.addHandler(consoleHandler);
    }
}
