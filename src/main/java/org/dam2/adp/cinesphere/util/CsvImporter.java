package org.dam2.adp.cinesphere.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.dam2.adp.cinesphere.DAO.*;
import org.dam2.adp.cinesphere.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria encargada de importar películas y sus datos relacionados.
 * Procesa el archivo CSV e inserta los datos normalizados en la base de datos.
 */
public class CsvImporter {

    private static final Logger logger = Logger.getLogger(CsvImporter.class.getName());

    private static final List<String> CABECERAS_ESPERADAS = List.of(
            "Title", "Director", "Stars", "IMDb-Rating", "Category",
            "Duration", "Censor-board-rating", "ReleaseYear"
    );

    private static final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private static final DirectorDAO directorDAO = new DirectorDAO();
    private static final ActorDAO actorDAO = new ActorDAO();
    private static final GeneroDAO generoDAO = new GeneroDAO();
    private static final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();

    private static final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private static final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private static final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();

    private static final Map<String, Director> cacheDirectores = new HashMap<>();
    private static final Map<String, Actor> cacheActores = new HashMap<>();
    private static final Map<String, Genero> cacheGeneros = new HashMap<>();
    private static final Map<String, Clasificacion> cacheClasificaciones = new HashMap<>();

    /**
     * Importa un archivo CSV desde una ruta local.
     * @param csvPath la ruta del archivo CSV.
     * @throws Exception si ocurre un error durante la importación.
     */
    public static void importarLocal(String csvPath) throws Exception {
        logger.log(Level.INFO, "Iniciando importación desde la ruta: " + csvPath);
        try (Reader reader = new FileReader(csvPath, StandardCharsets.UTF_8)) {
            importar(reader);
        }
    }

    /**
     * Importa un archivo CSV desde los recursos de la aplicación.
     * @param resourcePath la ruta del recurso CSV.
     * @throws Exception si ocurre un error durante la importación.
     */
    public static void importarDesdeRecurso(String resourcePath) throws Exception {
        logger.log(Level.INFO, "Iniciando importación desde recurso: " + resourcePath);
        InputStream is = CsvImporter.class.getResourceAsStream(resourcePath);
        if (is == null) throw new IllegalArgumentException("Recurso no encontrado: " + resourcePath);
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            importar(reader);
        }
    }

    /**
     * Procesa un Reader que contiene datos CSV.
     * @param reader el Reader con los datos CSV.
     * @throws Exception si ocurre un error durante la importación.
     */
    public static void importar(Reader reader) throws Exception {
        CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build()
                .parse(reader);

        Map<String, Integer> headerMap = parser.getHeaderMap();
        if (headerMap == null || headerMap.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV está vacío o sin cabeceras.");
        }

        for (String columna : CABECERAS_ESPERADAS) {
            if (!headerMap.containsKey(columna)) {
                throw new IllegalArgumentException("Falta columna obligatoria: " + columna);
            }
        }

        int processedRows = 0;
        for (CSVRecord row : parser) {
            try {
                if (!row.isConsistent() || row.get("Title").isBlank()) continue;
                procesarFila(row);
                processedRows++;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en fila " + row.getRecordNumber(), e);
            }
        }
        logger.log(Level.INFO, "Importación finalizada. Filas procesadas: " + processedRows);
    }

    /**
     * Procesa una fila de un archivo CSV.
     * @param row el registro CSV a procesar.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static void procesarFila(CSVRecord row) throws SQLException {
        String titulo = row.get("Title").trim();
        Integer year = pasarAEntero(row.get("ReleaseYear"));


        if (year == null) return;
        if (peliculaDAO.findByTituloAndYear(titulo, year) != null) return;


        Double rating = pasarADouble(row.get("IMDb-Rating"));
        String duracionStr = row.get("Duration").replace("min", "").trim();
        Integer duracion = pasarAEntero(duracionStr);
        String clasifStr = row.get("Censor-board-rating");

        Clasificacion clasificacion = obtenerClasificacion(clasifStr);

        Pelicula p = new Pelicula();
        p.setTituloPelicula(titulo);
        p.setYearPelicula(year);
        p.setRatingPelicula(rating);
        p.setDuracionPelicula(duracion);
        p.setClasificacion(clasificacion);

        peliculaDAO.insert(p);


        procesarDirectores(p, row.get("Director"));
        procesarActores(p, row.get("Stars"));
        procesarGeneros(p, row.get("Category"));
    }

    /**
     * Procesa los directores de una película.
     * @param p la película.
     * @param rawData los datos en crudo de los directores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static void procesarDirectores(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Director d = obtenerDirector(nombre);
            peliculaDirectorDAO.insert(p.getIdPelicula(), d.getIdDirector());
        }
    }

    /**
     * Procesa los actores de una película.
     * @param p la película.
     * @param rawData los datos en crudo de los actores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static void procesarActores(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Actor a = obtenerActor(nombre);
            peliculaActorDAO.insert(p.getIdPelicula(), a.getIdActor());
        }
    }

    /**
     * Procesa los géneros de una película.
     * @param p la película.
     * @param rawData los datos en crudo de los géneros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static void procesarGeneros(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Genero g = obtenerGenero(nombre);
            peliculaGeneroDAO.insert(p.getIdPelicula(), g.getIdGenero());
        }
    }

    /**
     * Obtiene o crea una clasificación.
     * @param nombre el nombre de la clasificación.
     * @return la clasificación.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static Clasificacion obtenerClasificacion(String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) nombre = "Not Rated";

        if (cacheClasificaciones.containsKey(nombre)) return cacheClasificaciones.get(nombre);

        Clasificacion c = clasificacionDAO.findById(nombre);
        if (c == null) {
            c = new Clasificacion(nombre);
            clasificacionDAO.insert(c);
        }
        cacheClasificaciones.put(nombre, c);
        return c;
    }

    /**
     * Obtiene o crea un director.
     * @param nombre el nombre del director.
     * @return el director.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static Director obtenerDirector(String nombre) throws SQLException {
        if (cacheDirectores.containsKey(nombre)) return cacheDirectores.get(nombre);

        Director d = directorDAO.findByName(nombre);
        if (d == null) {
            d = new Director(nombre);
            directorDAO.insert(d);
        }
        cacheDirectores.put(nombre, d);
        return d;
    }

    /**
     * Obtiene o crea un actor.
     * @param nombre el nombre del actor.
     * @return el actor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static Actor obtenerActor(String nombre) throws SQLException {
        if (cacheActores.containsKey(nombre)) return cacheActores.get(nombre);

        Actor a = actorDAO.findByName(nombre);
        if (a == null) {
            a = new Actor(nombre);
            actorDAO.insert(a);
        }
        cacheActores.put(nombre, a);
        return a;
    }

    /**
     * Obtiene o crea un género.
     * @param nombre el nombre del género.
     * @return el género.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private static Genero obtenerGenero(String nombre) throws SQLException {
        if (cacheGeneros.containsKey(nombre)) return cacheGeneros.get(nombre);

        Genero g = generoDAO.findByName(nombre);
        if (g == null) {
            g = new Genero(nombre);
            generoDAO.insert(g);
        }
        cacheGeneros.put(nombre, g);
        return g;
    }

    /**
     * Divide una cadena por comas, limpia espacios y elimina duplicados.
     * Utiliza LinkedHashSet para mantener el orden de aparición original.
     * @param cadena la cadena a procesar.
     * @return un conjunto de cadenas limpias.
     */
    private static Set<String> splitAndClean(String cadena) {
        if (cadena == null || cadena.isBlank()) return Collections.emptySet();

        Set<String> resultado = new LinkedHashSet<>();
        for (String parte : cadena.split(",")) {
            String limpio = parte.trim();
            if (!limpio.isEmpty()) {
                resultado.add(limpio);
            }
        }
        return resultado;
    }

    /**
     * Intenta convertir una cadena a un entero.
     * @param value la cadena a convertir.
     * @return el entero, o null si no se puede convertir.
     */
    private static Integer pasarAEntero(String value) {
        try {
            if (value == null) return null;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Intenta convertir una cadena a un doble.
     * @param value la cadena a convertir.
     * @return el doble, o null si no se puede convertir.
     */
    private static Double pasarADouble(String value) {
        try {
            if (value == null) return null;
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
