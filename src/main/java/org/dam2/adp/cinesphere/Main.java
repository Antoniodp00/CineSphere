package org.dam2.adp.cinesphere;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.util.Navigation;

/**
 * Clase principal de la aplicación CineSphere.
 */
public class Main extends Application {

    /**
     * Inicia la aplicación y configura la ventana principal.
     * @param stage El escenario principal de la aplicación.
     * @throws Exception Si ocurre un error durante el inicio.
     */
    @Override
    public void start(Stage stage) throws Exception {

        Navigation.setStage(stage);
        stage.setTitle("CineSphere");
        Navigation.switchScene("login.fxml");
    }

    /**
     * Se ejecuta al cerrar la aplicación, desconectando la base de datos.
     * @throws Exception Si ocurre un error durante el cierre.
     */
    @Override
    public void stop() throws Exception {

        System.out.println("Cerrando aplicación...");
        Conexion.getInstance().disconnect();
        super.stop();
    }

    /**
     * Método principal que lanza la aplicación.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        launch();
    }
}