package org.dam2.adp.cinesphere.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.dam2.adp.cinesphere.DAO.*;
import org.dam2.adp.cinesphere.model.*;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class CsvImporter {

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final DirectorDAO directorDAO = new DirectorDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();

    private final Map<String, Director> cacheDirectores = new HashMap<>();
    private final Map<String, Actor> cacheActores = new HashMap<>();
    private final Map<String, Genero> cacheGeneros = new HashMap<>();
    private final Map<String, Clasificacion> cacheClasificaciones = new HashMap<>();

    public void importar(String csvPath) throws Exception {

        CSVParser parser = CSVParser.parse(
                new FileReader(csvPath, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
        );

        for (CSVRecord row : parser) {

            String titulo = row.get("Title").trim();
            int year = Integer.parseInt(row.get("ReleaseYear").trim());

            String ratingStr = row.get("IMDb-Rating").trim();
            Double rating = ratingStr.isEmpty() ? null : Double.parseDouble(ratingStr);

            String duracionStr = row.get("Duration").trim().replace("min", "");
            Integer duracion = duracionStr.isEmpty() ? null : Integer.parseInt(duracionStr);

            String clasifStr = row.get("Censor-board-rating").trim();
            Clasificacion clasificacion = obtenerClasificacion(clasifStr);

            Pelicula p = new Pelicula();
            p.setTituloPelicula(titulo);
            p.setYearPelicula(year);
            p.setRatingPelicula(rating);
            p.setDuracionPelicula(duracion);
            p.setNombreClasificacion(clasificacion.getNombreClasificacion());

            peliculaDAO.insert(p);

            String directoresCSV = row.get("Director");
            if (directoresCSV != null && !directoresCSV.isBlank()) {
                for (String nombre : dividir(directoresCSV)) {
                    Director d = obtenerDirector(nombre);
                    peliculaDirectorDAO.add(p.getIdPelicula(), d.getIdDirector());
                }
            }

            String actoresCSV = row.get("Stars");
            if (actoresCSV != null && !actoresCSV.isBlank()) {
                for (String nombre : dividir(actoresCSV)) {
                    Actor a = obtenerActor(nombre);
                    peliculaActorDAO.add(p.getIdPelicula(), a.getIdActor());
                }
            }

            String generosCSV = row.get("Category");
            if (generosCSV != null && !generosCSV.isBlank()) {
                for (String nombre : dividir(generosCSV)) {
                    Genero g = obtenerGenero(nombre);
                    peliculaGeneroDAO.add(p.getIdPelicula(), g.getIdGenero());
                }
            }
        }
    }

    private List<String> dividir(String cadena) {
        List<String> lista = new ArrayList<>();
        for (String tok : cadena.split(",")) {
            String limpio = tok.replaceAll("\\s+", "").trim();
            if (!limpio.isBlank()) lista.add(limpio);
        }
        return lista;
    }

    private Clasificacion obtenerClasificacion(String nombre) throws SQLException {
        if (cacheClasificaciones.containsKey(nombre)) return cacheClasificaciones.get(nombre);

        Clasificacion c = clasificacionDAO.findById(nombre);
        if (c == null) {
            c = new Clasificacion(nombre);
            clasificacionDAO.insert(c);
        }
        cacheClasificaciones.put(nombre, c);
        return c;
    }

    private Director obtenerDirector(String nombre) throws SQLException {
        if (cacheDirectores.containsKey(nombre)) return cacheDirectores.get(nombre);

        for (Director d : directorDAO.findAll()) {
            if (d.getNombreDirector().equalsIgnoreCase(nombre)) {
                cacheDirectores.put(nombre, d);
                return d;
            }
        }

        Director nuevo = directorDAO.insert(new Director(nombre));
        cacheDirectores.put(nombre, nuevo);
        return nuevo;
    }

    private Actor obtenerActor(String nombre) throws SQLException {
        if (cacheActores.containsKey(nombre)) return cacheActores.get(nombre);

        for (Actor a : actorDAO.findAll()) {
            if (a.getNombreActor().equalsIgnoreCase(nombre)) {
                cacheActores.put(nombre, a);
                return a;
            }
        }

        Actor nuevo = actorDAO.insert(new Actor(nombre));
        cacheActores.put(nombre, nuevo);
        return nuevo;
    }

    private Genero obtenerGenero(String nombre) throws SQLException {
        if (cacheGeneros.containsKey(nombre)) return cacheGeneros.get(nombre);

        for (Genero g : generoDAO.findAll()) {
            if (g.getNombreGenero().equalsIgnoreCase(nombre)) {
                cacheGeneros.put(nombre, g);
                return g;
            }
        }

        Genero nuevo = generoDAO.insert(new Genero(nombre));
        cacheGeneros.put(nombre, nuevo);
        return nuevo;
    }
}
