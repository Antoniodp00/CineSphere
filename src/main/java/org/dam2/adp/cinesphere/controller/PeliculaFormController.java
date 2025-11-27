package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.DAO.*;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;
import org.dam2.adp.cinesphere.util.AlertUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de creación y edición de películas.
 */
public class PeliculaFormController {

    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtYear;
    @FXML
    private TextField txtDuracion;
    @FXML
    private TextField txtRating;
    @FXML
    private TextField txtClasificacion;
    @FXML
    private TextField txtGeneros;
    @FXML
    private TextField txtDirectores;
    @FXML
    private TextArea txtActores;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final DirectorDAO directorDAO = new DirectorDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();

    private static final Logger logger = Logger.getLogger(PeliculaFormController.class.getName());

    /**
     * Inicializa el controlador, configurando los listeners de los botones.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando PeliculaFormController...");
        btnGuardar.setOnAction(e -> guardarPelicula());
        btnCancelar.setOnAction(e -> cerrarVentana());
        logger.log(Level.INFO, "PeliculaFormController inicializado.");
    }

    /**
     * Guarda la película en la base de datos con los datos introducidos en el formulario.
     */
    private void guardarPelicula() {
        logger.log(Level.INFO, "Iniciando proceso de guardado de película...");
        Connection conn = Conexion.getInstance().getConnection();
        try {
            if (txtTitulo.getText().isBlank() || txtYear.getText().isBlank()) {
                AlertUtils.error("El título y el año son obligatorios.");
                logger.log(Level.WARNING, "Intento de guardar película con título o año vacíos.");
                return;
            }

            String titulo = txtTitulo.getText().trim();
            int year = Integer.parseInt(txtYear.getText().trim());

            if (peliculaDAO.findByTituloAndYear(titulo, year) != null) {
                AlertUtils.error("Esta película ya existe en la base de datos.");
                logger.log(Level.WARNING, "Intento de guardar una película duplicada: " + titulo + " (" + year + ")");
                return;
            }

            Integer duracion = null;
            if (!txtDuracion.getText().isBlank()) {
                duracion = Integer.parseInt(txtDuracion.getText().trim());
            }

            Double rating = null;
            if (!txtRating.getText().isBlank()) {
                rating = Double.parseDouble(txtRating.getText().trim());
            }

            String clasifNombre = txtClasificacion.getText().isBlank() ? "Not Rated" : txtClasificacion.getText().trim();
            Clasificacion clasificacion = clasificacionDAO.findById(clasifNombre);
            if (clasificacion == null) {
                clasificacion = new Clasificacion(clasifNombre);
                clasificacionDAO.insert(clasificacion);
                logger.log(Level.INFO, "Nueva clasificación creada: " + clasifNombre);
            }

            conn.setAutoCommit(false);
            logger.log(Level.INFO, "Transacción iniciada.");

            Pelicula p = new Pelicula();
            p.setTituloPelicula(titulo);
            p.setYearPelicula(year);
            p.setRatingPelicula(rating);
            p.setDuracionPelicula(duracion);
            p.setNombreClasificacion(clasificacion.getNombreClasificacion());

            peliculaDAO.insert(p);
            logger.log(Level.INFO, "Película insertada en la base de datos: " + p.getTituloPelicula());

            guardarGeneros(p, txtGeneros.getText());
            guardarDirectores(p, txtDirectores.getText());
            guardarActores(p, txtActores.getText());

            conn.commit();
            conn.setAutoCommit(true);
            logger.log(Level.INFO, "Transacción completada con éxito (commit).");

            AlertUtils.info("Película guardada correctamente.");
            cerrarVentana();

        } catch (Exception e) {
            try {
                logger.log(Level.SEVERE, "Fallo al guardar la película. Realizando Rollback...", e);
                conn.rollback();
                conn.setAutoCommit(true);
                logger.log(Level.INFO, "Rollback realizado con éxito.");
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error al realizar el rollback", ex);
            }
            AlertUtils.error("Error al guardar (se han deshecho los cambios): " + e.getMessage());
        }
    }

    /**
     * Guarda los géneros de la película.
     * @param p la película.
     * @param texto el texto con los géneros separados por comas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void guardarGeneros(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Genero g = generoDAO.findByName(nombre);
            if (g == null) {
                g = generoDAO.insert(new Genero(nombre));
                logger.log(Level.FINE, "Nuevo género creado y añadido a la película: " + nombre);
            }
            peliculaGeneroDAO.add(p.getIdPelicula(), g.getIdGenero());
        }
    }

    /**
     * Guarda los directores de la película.
     * @param p la película.
     * @param texto el texto con los directores separados por comas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void guardarDirectores(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Director d = directorDAO.findByName(nombre);
            if (d == null) {
                d = directorDAO.insert(new Director(nombre));
                logger.log(Level.FINE, "Nuevo director creado y añadido a la película: " + nombre);
            }
            peliculaDirectorDAO.add(p.getIdPelicula(), d.getIdDirector());
        }
    }

    /**
     * Guarda los actores de la película.
     * @param p la película.
     * @param texto el texto con los actores separados por comas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void guardarActores(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Actor a = actorDAO.findByName(nombre);
            if (a == null) {
                a = actorDAO.insert(new Actor(nombre));
                logger.log(Level.FINE, "Nuevo actor creado y añadido a la película: " + nombre);
            }
            peliculaActorDAO.add(p.getIdPelicula(), a.getIdActor());
        }
    }

    /**
     * Divide un texto en una lista de cadenas, separadas por comas.
     * @param texto el texto a dividir.
     * @return una lista de cadenas.
     */
    private List<String> dividir(String texto) {
        List<String> lista = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            for (String s : texto.split(",")) {
                String limpio = s.trim();
                if (!limpio.isEmpty()) lista.add(limpio);
            }
        }
        return lista;
    }

    /**
     * Cierra la ventana actual.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        logger.log(Level.INFO, "Cerrando ventana del formulario de película.");
        stage.close();
    }
}
