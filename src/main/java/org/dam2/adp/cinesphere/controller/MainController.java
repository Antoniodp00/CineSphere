package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

public class MainController {

    @FXML private StackPane contentArea;

    @FXML private Button btnPeliculas;
    @FXML private Button btnMiLista;
    @FXML private Button btnEstadisticas;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        Navigation.setMainController(this);

        btnPeliculas.setOnAction(e -> loadView("peliculas_lista.fxml"));
        btnMiLista.setOnAction(e -> loadView("milista.fxml"));
        btnEstadisticas.setOnAction(e -> loadView("estadisticas.fxml"));
        btnSettings.setOnAction(e -> loadView("settings.fxml"));

        btnLogout.setOnAction(e -> {
            SessionManager.getInstance().cerrarSesion();
            Navigation.switchScene("login.fxml");
        });

        loadView("peliculas_lista.fxml");
    }

    public void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
