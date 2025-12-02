package org.dam2.adp.cinesphere;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.util.log.LoggingConfig;
import org.dam2.adp.cinesphere.util.Navigation;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal de la aplicación CineSphere.
 */
public class Main extends Application {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Inicia la aplicación y configura la ventana principal.
     * @param stage El escenario principal de la aplicación.
     * @throws Exception Si ocurre un error durante el inicio.
     */
    @Override
    public void start(Stage stage) throws Exception {

        LoggingConfig.setup();

        Navigation.applyApplicationStyles();

        logger.log(Level.INFO, "Iniciando la aplicación CineSphere...");
        Navigation.setStage(stage);
        stage.setTitle("CineSphere");
        Navigation.switchScene("login.fxml");
        logger.log(Level.INFO, "Ventana principal configurada y escena de login cargada.");
    }

    /**
     * Se ejecuta al cerrar la aplicación, desconectando la base de datos.
     * @throws Exception Si ocurre un error durante el cierre.
     */
    @Override
    public void stop() throws Exception {
        logger.log(Level.INFO, "Cerrando la aplicación...");
        Conexion.getInstance().disconnect();
        super.stop();
        logger.log(Level.INFO, "Aplicación cerrada correctamente.");
    }

    /**
     * Método principal que lanza la aplicación.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
