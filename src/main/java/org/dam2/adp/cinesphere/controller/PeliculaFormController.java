package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.DAO.*;
import org.dam2.adp.cinesphere.model.*;
import org.dam2.adp.cinesphere.util.AlertUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeliculaFormController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtYear;
    @FXML private TextField txtDuracion;
    @FXML private TextField txtRating;
    @FXML private TextField txtClasificacion;
    @FXML private TextField txtGeneros;
    @FXML private TextField txtDirectores;
    @FXML private TextArea txtActores;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // DAOs necesarios
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final DirectorDAO directorDAO = new DirectorDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();

    @FXML
    private void initialize() {
        btnGuardar.setOnAction(e -> guardarPelicula());
        btnCancelar.setOnAction(e -> cerrarVentana());
    }

    private void guardarPelicula() {
        try {
            // 1. Validaciones básicas
            if (txtTitulo.getText().isBlank() || txtYear.getText().isBlank()) {
                AlertUtils.error("El título y el año son obligatorios.");
                return;
            }

            String titulo = txtTitulo.getText().trim();
            int year = Integer.parseInt(txtYear.getText().trim());

            // Verificar duplicados
            if (peliculaDAO.findByTituloAndYear(titulo, year) != null) {
                AlertUtils.error("Esta película ya existe en la base de datos.");
                return;
            }

            // 2. Parseo de datos opcionales
            Integer duracion = null;
            if (!txtDuracion.getText().isBlank()) {
                duracion = Integer.parseInt(txtDuracion.getText().trim());
            }

            Double rating = null;
            if (!txtRating.getText().isBlank()) {
                rating = Double.parseDouble(txtRating.getText().trim());
            }

            // 3. Gestión de Clasificación
            String clasifNombre = txtClasificacion.getText().isBlank() ? "Not Rated" : txtClasificacion.getText().trim();
            Clasificacion clasificacion = clasificacionDAO.findById(clasifNombre);
            if (clasificacion == null) {
                clasificacion = new Clasificacion(clasifNombre);
                clasificacionDAO.insert(clasificacion);
            }

            // 4. Crear e Insertar Película
            Pelicula p = new Pelicula();
            p.setTituloPelicula(titulo);
            p.setYearPelicula(year);
            p.setRatingPelicula(rating);
            p.setDuracionPelicula(duracion);
            p.setNombreClasificacion(clasificacion.getNombreClasificacion());

            peliculaDAO.insert(p); // Aquí obtenemos el ID generado

            // 5. Gestión de relaciones (Listas)
            guardarGeneros(p, txtGeneros.getText());
            guardarDirectores(p, txtDirectores.getText());
            guardarActores(p, txtActores.getText());

            AlertUtils.info("Película guardada correctamente.");
            cerrarVentana();

        } catch (NumberFormatException e) {
            AlertUtils.error("Formato incorrecto en Año, Duración o Rating. Deben ser números.");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.error("Error de base de datos: " + e.getMessage());
        }
    }

    // --- Métodos Auxiliares para Relaciones ---

    private void guardarGeneros(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Genero g = generoDAO.findByName(nombre);
            if (g == null) {
                g = generoDAO.insert(new Genero(nombre));
            }
            peliculaGeneroDAO.add(p.getIdPelicula(), g.getIdGenero());
        }
    }

    private void guardarDirectores(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Director d = directorDAO.findByName(nombre);
            if (d == null) {
                d = directorDAO.insert(new Director(nombre));
            }
            peliculaDirectorDAO.add(p.getIdPelicula(), d.getIdDirector());
        }
    }

    private void guardarActores(Pelicula p, String texto) throws SQLException {
        for (String nombre : dividir(texto)) {
            Actor a = actorDAO.findByName(nombre);
            if (a == null) {
                a = actorDAO.insert(new Actor(nombre));
            }
            peliculaActorDAO.add(p.getIdPelicula(), a.getIdActor());
        }
    }

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

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}