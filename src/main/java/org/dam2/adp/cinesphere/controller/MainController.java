package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.io.IOException;

/**
 * Controlador principal que gestiona la navegación y el contenido de la ventana principal.
 */
public class MainController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnPeliculas;
    @FXML
    private Button btnMiLista;
    @FXML
    private Button btnEstadisticas;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnLogout;

    /**
     * Inicializa el controlador principal, configurando los listeners de los botones de navegación
     * y cargando la vista inicial.
     */
    @FXML
    public void initialize() {
        Navigation.setMainController(this);

        btnPeliculas.setOnAction(e -> loadView("peliculas_lista.fxml"));
        btnMiLista.setOnAction(e -> loadView("milista.fxml"));
        btnEstadisticas.setOnAction(e -> loadView("estadisticas.fxml"));
        btnSettings.setOnAction(e -> loadView("settings.fxml"));

        btnLogout.setOnAction(e -> logout());

        loadView("peliculas_lista.fxml");
    }

    /**
     * Carga una vista FXML en el área de contenido principal.
     *
     * @param fxml el nombre del archivo FXML a cargar (ej. "peliculas_lista.fxml").
     */
    public void loadView(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cierra la sesión del usuario actual y navega a la pantalla de login.
     */
    private void logout() {
        SessionManager.getInstance().cerrarSesion();
        Navigation.switchScene("login.fxml");
    }
}
