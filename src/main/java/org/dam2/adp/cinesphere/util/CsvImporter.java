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
            try {
                String titulo = row.get("Title").trim();
                int year = Integer.parseInt(row.get("ReleaseYear").trim());

                // Evitar duplicados
                if (peliculaDAO.findByTituloAndYear(titulo, year) != null) {
                    System.out.println("Película ya existe, saltando: " + titulo + " (" + year + ")");
                    continue;
                }

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
                for (String nombre : splitAndClean(directoresCSV)) {
                    Director d = obtenerDirector(nombre);
                    peliculaDirectorDAO.add(p.getIdPelicula(), d.getIdDirector());
                }

                String actoresCSV = row.get("Stars");
                for (String nombre : splitAndClean(actoresCSV)) {
                    Actor a = obtenerActor(nombre);
                    peliculaActorDAO.add(p.getIdPelicula(), a.getIdActor());
                }

                String generosCSV = row.get("Category");
                for (String nombre : splitAndClean(generosCSV)) {
                    Genero g = obtenerGenero(nombre);
                    peliculaGeneroDAO.add(p.getIdPelicula(), g.getIdGenero());
                }
            } catch (NumberFormatException e) {
                System.err.println("Error de formato en la fila " + row.getRecordNumber() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error procesando la fila " + row.getRecordNumber() + ": " + e.getMessage());
            }
        }
    }

    private Set<String> splitAndClean(String cadena) {
        if (cadena == null || cadena.isBlank()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (String tok : cadena.split(",")) {
            String limpio = tok.trim();
            if (!limpio.isBlank()) {
                set.add(limpio);
            }
        }
        return set;
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

        Director d = directorDAO.findByName(nombre);
        if (d == null) {
            d = directorDAO.insert(new Director(nombre));
        }
        cacheDirectores.put(nombre, d);
        return d;
    }

    private Actor obtenerActor(String nombre) throws SQLException {
        if (cacheActores.containsKey(nombre)) return cacheActores.get(nombre);

        Actor a = actorDAO.findByName(nombre);
        if (a == null) {
            a = actorDAO.insert(new Actor(nombre));
        }
        cacheActores.put(nombre, a);
        return a;
    }

    private Genero obtenerGenero(String nombre) throws SQLException {
        if (cacheGeneros.containsKey(nombre)) return cacheGeneros.get(nombre);

        Genero g = generoDAO.findByName(nombre);
        if (g == null) {
            g = generoDAO.insert(new Genero(nombre));
        }
        cacheGeneros.put(nombre, g);
        return g;
    }
}
