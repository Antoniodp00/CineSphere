package org.dam2.adp.cinesphere.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicializa el esquema de la base de datos.
 */
public class DatabaseSchema {

    private static final Logger logger = Logger.getLogger(DatabaseSchema.class.getName());

    /**
     * Inicializa las tablas de la base de datos si no existen.
     */
    public static void inicializar() {
        try {
            Connection conn = Conexion.getInstance().getConnection();
            boolean isSQLite = Conexion.getInstance().isSQLite();
            logger.log(Level.INFO, "Iniciando inicialización del esquema para " + (isSQLite ? "SQLite" : "PostgreSQL"));

            String AUTO_INCREMENT = isSQLite ? "INTEGER PRIMARY KEY AUTOINCREMENT" : "SERIAL PRIMARY KEY";

            Statement stmt = conn.createStatement();

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clasificacion (nombreclasificacion VARCHAR(50) PRIMARY KEY)");
            logger.log(Level.FINE, "Tabla 'clasificacion' creada o ya existente.");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS genero (idgenero " + AUTO_INCREMENT + ", nombregenero VARCHAR(100) NOT NULL)");
            logger.log(Level.FINE, "Tabla 'genero' creada o ya existente.");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS actor (idactor " + AUTO_INCREMENT + ", nombreactor VARCHAR(100) NOT NULL)");
            logger.log(Level.FINE, "Tabla 'actor' creada o ya existente.");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS director (iddirector " + AUTO_INCREMENT + ", nombredirector VARCHAR(100) NOT NULL)");
            logger.log(Level.FINE, "Tabla 'director' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuario (
                    idusuario %s,
                    nombreusuario VARCHAR(100) UNIQUE NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    passw VARCHAR(255) NOT NULL,
                    borndate DATE,
                    rol VARCHAR(20) DEFAULT 'USER'
                )
            """.formatted(AUTO_INCREMENT));
            logger.log(Level.FINE, "Tabla 'usuario' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pelicula (
                    idpelicula %s,
                    titulopelicula VARCHAR(200) NOT NULL,
                    yearpelicula INTEGER,
                    ratingpelicula DOUBLE PRECISION,
                    duracionpelicula INTEGER,
                    nombreclasificacion VARCHAR(50)
                )
            """.formatted(AUTO_INCREMENT));
            logger.log(Level.FINE, "Tabla 'pelicula' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculagenero (
                    idpelicula INTEGER NOT NULL,
                    idgenero INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, idgenero),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (idgenero) REFERENCES genero(idgenero) ON DELETE CASCADE
                )
            """);
            logger.log(Level.FINE, "Tabla 'peliculagenero' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculaactor (
                    idpelicula INTEGER NOT NULL,
                    idactor INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, idactor),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (idactor) REFERENCES actor(idactor) ON DELETE CASCADE
                )
            """);
            logger.log(Level.FINE, "Tabla 'peliculaactor' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculadirector (
                    idpelicula INTEGER NOT NULL,
                    iddirector INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, iddirector),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (iddirector) REFERENCES director(iddirector) ON DELETE CASCADE
                )
            """);
            logger.log(Level.FINE, "Tabla 'peliculadirector' creada o ya existente.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS milista (
                    idusuario INTEGER NOT NULL,
                    idpelicula INTEGER NOT NULL,
                    estado VARCHAR(50),
                    puntuacion INTEGER,
                    urlimg VARCHAR(255),
                    fecha_anadido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (idusuario, idpelicula),
                    FOREIGN KEY (idusuario) REFERENCES usuario(idusuario) ON DELETE CASCADE,
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE
                )
            """);
            logger.log(Level.FINE, "Tabla 'milista' creada o ya existente.");

            stmt.close();
            logger.log(Level.INFO, "--- Esquema inicializado (" + (isSQLite ? "SQLite" : "PostgreSQL") + ") ---");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al inicializar el esquema de la base de datos", e);
        }
    }
}
