package org.dam2.adp.cinesphere;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.util.Navigation;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Conexion.connect("config-postgres.properties");
        Navigation.setStage(stage);
        Navigation.navigate("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}
