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
 * Clase utilitaria encargada de importar películas y sus datos relacionados
 * desde archivos CSV a la base de datos.
 * <p>
 * Soporta la importación desde el sistema de archivos local y desde los recursos
 * empaquetados en la aplicación. Gestiona la creación de entidades relacionadas
 * (Actores, Directores, Géneros) evitando duplicados mediante cachés y verificaciones.
 * </p>
 */
public class CsvImporter {

    private static final Logger logger = Logger.getLogger(CsvImporter.class.getName());

    /**
     * Lista de cabeceras obligatorias que debe contener el archivo CSV para ser válido.
     */
    private static final List<String> CABECERAS_ESPERADAS = List.of(
            "Title", "Director", "Stars", "IMDb-Rating", "Category",
            "Duration", "Censor-board-rating", "ReleaseYear"
    );

    // Instancias de los DAOs para interactuar con la base de datos
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final DirectorDAO directorDAO = new DirectorDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();

    // DAOs para tablas intermedias (relaciones N:M)
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();

    // Cachés en memoria para reducir el número de consultas SELECT a la base de datos
    // Clave: Nombre de la entidad -> Valor: Objeto entidad con su ID
    private final Map<String, Director> cacheDirectores = new HashMap<>();
    private final Map<String, Actor> cacheActores = new HashMap<>();
    private final Map<String, Genero> cacheGeneros = new HashMap<>();
    private final Map<String, Clasificacion> cacheClasificaciones = new HashMap<>();

    /**
     * Importa películas desde un archivo ubicado en el sistema de archivos local.
     * Este método es útil cuando el usuario selecciona un archivo mediante un FileChooser.
     *
     * @param csvPath La ruta absoluta del archivo CSV en el disco.
     * @throws Exception Si ocurre un error de lectura (IO) o de base de datos (SQL).
     */
    public void importar(String csvPath) throws Exception {
        logger.log(Level.INFO, "Iniciando importación desde la ruta: " + csvPath);
        // Utilizamos try-with-resources para asegurar el cierre del Reader
        try (Reader reader = new FileReader(csvPath, StandardCharsets.UTF_8)) {
            importar(reader);
        }
    }

    /**
     * Importa películas desde un archivo ubicado en los recursos de la aplicación (classpath).
     * Este método es ideal para cargar datasets de ejemplo predeterminados.
     *
     * @param resourcePath La ruta relativa del recurso (ej. "/csv/archivo.csv").
     * @throws Exception Si el recurso no existe o hay errores de lectura/BD.
     */
    public void importarDesdeRecurso(String resourcePath) throws Exception {
        logger.log(Level.INFO, "Iniciando importación desde el recurso interno: " + resourcePath);
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            logger.log(Level.SEVERE, "No se encontró el recurso interno: " + resourcePath);
            throw new IllegalArgumentException("No se encontró el recurso interno: " + resourcePath);
        }
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            importar(reader);
        }
    }

    /**
     * Lógica central del proceso de importación. Es agnóstica al origen de los datos.
     * Parsea el contenido, valida la estructura y procesa fila por fila.
     *
     * @param reader Un objeto Reader (FileReader o InputStreamReader) con el contenido CSV.
     * @throws Exception Si el formato es inválido o hay errores graves.
     */
    public void importar(Reader reader) throws Exception {
        // Configuración del parser: detecta cabeceras, ignora espacios extra y mayúsculas/minúsculas en cabeceras
        CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build()
                .parse(reader);

        // 1. Validación de Estructura: Comprobar que existen las columnas necesarias
        Map<String, Integer> headerMap = parser.getHeaderMap();
        if (headerMap == null || headerMap.isEmpty()) {
            logger.log(Level.SEVERE, "El archivo CSV está vacío o no tiene cabeceras reconocibles.");
            throw new IllegalArgumentException("El archivo CSV está vacío o no tiene cabeceras reconocibles.");
        }

        for (String columna : CABECERAS_ESPERADAS) {
            if (!headerMap.containsKey(columna)) {
                logger.log(Level.SEVERE, "Formato inválido. Falta la columna obligatoria: " + columna);
                throw new IllegalArgumentException("Formato inválido. Falta la columna obligatoria: " + columna);
            }
        }
        logger.log(Level.INFO, "Validación de cabeceras completada con éxito.");

        // 2. Procesamiento Iterativo: Leer cada fila
        int processedRows = 0;
        for (CSVRecord row : parser) {
            try {
                // Validación básica: Si la fila no es consistente o no tiene título, se salta
                if (!row.isConsistent() || row.get("Title").isBlank()) {
                    logger.log(Level.WARNING, "Saltando fila inconsistente o sin título: " + row.getRecordNumber());
                    continue;
                }
                procesarFila(row);
                processedRows++;

            } catch (Exception e) {
                // Estrategia de tolerancia a fallos: Si una fila falla, se loguea y se continúa con la siguiente
                logger.log(Level.SEVERE, "Error importando fila " + row.getRecordNumber() + ": " + e.getMessage(), e);
            }
        }
        logger.log(Level.INFO, "Proceso de importación finalizado. Filas procesadas: " + processedRows);
    }

    /**
     * Procesa una única fila del CSV, mapea los datos a objetos y los persiste.
     * Realiza la comprobación de duplicados antes de insertar.
     *
     * @param row El registro CSV actual.
     * @throws SQLException Si falla alguna operación de base de datos.
     */
    private void procesarFila(CSVRecord row) throws SQLException {
        String titulo = row.get("Title").trim();

        // Parseo seguro de enteros (si falla el formato, devuelve null)
        Integer year = tryParseInt(row.get("ReleaseYear"));

        // Si no hay año, consideramos el dato inválido para nuestro sistema y saltamos la fila
        if (year == null) {
            logger.log(Level.WARNING, "Fila " + row.getRecordNumber() + " saltada: año de lanzamiento inválido.");
            return;
        }

        // --- CONTROL DE DUPLICADOS ---
        // Verificamos si la película ya existe en la BD (por Título y Año)
        if (peliculaDAO.findByTituloAndYear(titulo, year) != null) {
            logger.log(Level.FINER, "Película duplicada encontrada y omitida: " + titulo + " (" + year + ")");
            return;
        }

        Double rating = tryParseDouble(row.get("IMDb-Rating"));

        // Limpieza del campo duración (ej: de "130min" a "130")
        String duracionStr = row.get("Duration").replace("min", "").trim();
        Integer duracion = tryParseInt(duracionStr);

        // Gestión de la clasificación (ej: "PG-13")
        String clasifStr = row.get("Censor-board-rating");
        Clasificacion clasificacion = obtenerClasificacion(clasifStr);

        // Creación del objeto Película
        Pelicula p = new Pelicula();
        p.setTituloPelicula(titulo);
        p.setYearPelicula(year);
        p.setRatingPelicula(rating);
        p.setDuracionPelicula(duracion);
        p.setNombreClasificacion(clasificacion.getNombreClasificacion());

        // Inserción principal (obtiene el ID generado automáticamente)
        peliculaDAO.insert(p);
        logger.log(Level.FINE, "Película insertada: " + p.getTituloPelicula());

        // Procesamiento de relaciones Many-to-Many (Directores, Actores, Géneros)
        // Se pasan las cadenas crudas del CSV (ej: "Steven Spielberg, George Lucas")
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
    private void procesarDirectores(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Director d = obtenerDirector(nombre);
            peliculaDirectorDAO.add(p.getIdPelicula(), d.getIdDirector());
        }
    }

    /**
     * Procesa los actores de una película.
     * @param p la película.
     * @param rawData los datos en crudo de los actores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void procesarActores(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Actor a = obtenerActor(nombre);
            peliculaActorDAO.add(p.getIdPelicula(), a.getIdActor());
        }
    }

    /**
     * Procesa los géneros de una película.
     * @param p la película.
     * @param rawData los datos en crudo de los géneros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void procesarGeneros(Pelicula p, String rawData) throws SQLException {
        for (String nombre : splitAndClean(rawData)) {
            Genero g = obtenerGenero(nombre);
            peliculaGeneroDAO.add(p.getIdPelicula(), g.getIdGenero());
        }
    }

    /**
     * Obtiene o crea una Clasificación.
     * @param nombre el nombre de la clasificación.
     * @return la clasificación.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private Clasificacion obtenerClasificacion(String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) nombre = "Not Rated"; // Valor por defecto

        if (cacheClasificaciones.containsKey(nombre)) return cacheClasificaciones.get(nombre);

        // En tu DAO, findById busca por nombre (clave primaria string)
        Clasificacion c = clasificacionDAO.findById(nombre);
        if (c == null) {
            c = new Clasificacion(nombre);
            clasificacionDAO.insert(c);
            logger.log(Level.FINER, "Nueva clasificación creada: " + nombre);
        }
        cacheClasificaciones.put(nombre, c);
        return c;
    }

    /**
     * Obtiene o crea un Director.
     * @param nombre el nombre del director.
     * @return el director.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private Director obtenerDirector(String nombre) throws SQLException {
        if (cacheDirectores.containsKey(nombre)) return cacheDirectores.get(nombre);

        Director d = directorDAO.findByName(nombre);
        if (d == null) {
            d = new Director(nombre);
            // El insert actualiza el ID del objeto 'd'
            directorDAO.insert(d);
            logger.log(Level.FINER, "Nuevo director creado: " + nombre);
        }
        cacheDirectores.put(nombre, d);
        return d;
    }

    /**
     * Obtiene o crea un Actor.
     * @param nombre el nombre del actor.
     * @return el actor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private Actor obtenerActor(String nombre) throws SQLException {
        if (cacheActores.containsKey(nombre)) return cacheActores.get(nombre);

        Actor a = actorDAO.findByName(nombre);
        if (a == null) {
            a = new Actor(nombre);
            actorDAO.insert(a);
            logger.log(Level.FINER, "Nuevo actor creado: " + nombre);
        }
        cacheActores.put(nombre, a);
        return a;
    }

    /**
     * Obtiene o crea un Género.
     * @param nombre el nombre del género.
     * @return el género.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private Genero obtenerGenero(String nombre) throws SQLException {
        if (cacheGeneros.containsKey(nombre)) return cacheGeneros.get(nombre);

        Genero g = generoDAO.findByName(nombre);
        if (g == null) {
            g = new Genero(nombre);
            generoDAO.insert(g);
            logger.log(Level.FINER, "Nuevo género creado: " + nombre);
        }
        cacheGeneros.put(nombre, g);
        return g;
    }

    /**
     * Divide una cadena por comas, limpia espacios y elimina duplicados o vacíos.
     * Ej: "Action, Drama, Action" -> ["Action", "Drama"]
     * @param cadena la cadena a procesar.
     * @return un conjunto de cadenas limpias.
     */
    private Set<String> splitAndClean(String cadena) {
        if (cadena == null || cadena.isBlank()) return Collections.emptySet();

        Set<String> resultado = new HashSet<>();
        for (String parte : cadena.split(",")) {
            String limpio = parte.trim();
            if (!limpio.isEmpty()) {
                resultado.add(limpio);
            }
        }
        return resultado;
    }

    /**
     * Intenta parsear un entero de forma segura.
     * @param value el valor a parsear.
     * @return el número o null si el formato es incorrecto.
     */
    private Integer tryParseInt(String value) {
        try {
            if (value == null) return null;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error al parsear entero: '" + value + "'", e);
            return null;
        }
    }

    /**
     * Intenta parsear un double de forma segura.
     * @param value el valor a parsear.
     * @return el número o null si el formato es incorrecto.
     */
    private Double tryParseDouble(String value) {
        try {
            if (value == null) return null;
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error al parsear double: '" + value + "'", e);
            return null;
        }
    }
}
