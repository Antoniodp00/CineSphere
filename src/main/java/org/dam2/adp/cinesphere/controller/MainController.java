package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    /**
     * Inicializa el controlador principal, configurando los listeners de los botones de navegación
     * y cargando la vista inicial.
     */
    @FXML
    public void initialize() {
        logger.log(Level.INFO, "Inicializando MainController...");
        Navigation.setMainController(this);

        btnPeliculas.setOnAction(e -> loadView("peliculas_lista.fxml"));
        btnMiLista.setOnAction(e -> loadView("milista.fxml"));
        btnEstadisticas.setOnAction(e -> loadView("estadisticas.fxml"));
        btnSettings.setOnAction(e -> loadView("settings.fxml"));

        btnLogout.setOnAction(e -> logout());

        loadView("peliculas_lista.fxml");
        logger.log(Level.INFO, "MainController inicializado y vista por defecto cargada.");
    }

    /**
     * Carga una vista FXML en el área de contenido principal.
     *
     * @param fxml el nombre del archivo FXML a cargar.
     */
    public void loadView(String fxml) {
        try {
            logger.log(Level.INFO, "Cargando vista: " + fxml);
            Node view = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al cargar la vista FXML: " + fxml, e);
        }
    }

    /**
     * Cierra la sesión del usuario actual y navega a la pantalla de login.
     */
    private void logout() {
        logger.log(Level.INFO, "Iniciando proceso de cierre de sesión...");
        SessionManager.getInstance().cerrarSesion();
        Navigation.switchScene("login.fxml");
    }
}
