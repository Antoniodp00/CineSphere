package org.dam2.adp.cinesphere.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicializa el esquema de la base de datos con soporte para sincronización (Soft Delete).
 */
public class DatabaseSchema {

    private static final Logger logger = Logger.getLogger(DatabaseSchema.class.getName());

    // Definición constante de las columnas de sincronización para evitar repetición
    // TIMESTAMP: Guarda cuándo ocurrió el cambio.
    // eliminado: 0 = Activo, 1 = Borrado Lógico.
    private static final String SYNC_COLS = """
            ,
            ultima_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            eliminado INTEGER DEFAULT 0
            """;

    public static void inicializar() {
        try {
            Connection conn = Conexion.getInstance().getConnection();
            boolean isSQLite = Conexion.getInstance().isSQLite();
            logger.log(Level.INFO, "Iniciando esquema (Sync enabled) para " + (isSQLite ? "SQLite" : "PostgreSQL"));

            String AUTO_INCREMENT = isSQLite ? "INTEGER PRIMARY KEY AUTOINCREMENT" : "SERIAL PRIMARY KEY";

            Statement stmt = conn.createStatement();

            // 1. Tablas Catálogo (Ahora con Sync)
            // Se añaden las columnas para que el admin pueda gestionar el catálogo en local/nube

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clasificacion (nombreclasificacion VARCHAR(50) PRIMARY KEY" + SYNC_COLS + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS genero (idgenero " + AUTO_INCREMENT + ", nombregenero VARCHAR(100) NOT NULL" + SYNC_COLS + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS actor (idactor " + AUTO_INCREMENT + ", nombreactor VARCHAR(100) NOT NULL" + SYNC_COLS + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS director (iddirector " + AUTO_INCREMENT + ", nombredirector VARCHAR(100) NOT NULL" + SYNC_COLS + ")");

            logger.log(Level.FINE, "Tablas de catálogo verificadas.");

            // 2. Tabla Usuario
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuario (
                    idusuario %s,
                    nombreusuario VARCHAR(100) UNIQUE NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    passw VARCHAR(255) NOT NULL,
                    borndate DATE,
                    rol VARCHAR(20) DEFAULT 'USER'
                    %s
                )
            """.formatted(AUTO_INCREMENT, SYNC_COLS));

            logger.log(Level.FINE, "Tabla 'usuario' verificada.");

            // 3. Tabla Película
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pelicula (
                    idpelicula %s,
                    titulopelicula VARCHAR(200) NOT NULL,
                    yearpelicula INTEGER,
                    ratingpelicula DOUBLE PRECISION,
                    duracionpelicula INTEGER,
                    nombreclasificacion VARCHAR(50)
                    %s
                )
            """.formatted(AUTO_INCREMENT, SYNC_COLS));

            logger.log(Level.FINE, "Tabla 'pelicula' verificada.");

            // 4. Tablas Intermedias (Puras)
            // NO añadimos Soft Delete aquí. La sincronización depende de la entidad 'Pelicula'.
            // Si la Película tiene 'eliminado=1', sus relaciones se ignoran.

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculagenero (
                    idpelicula INTEGER NOT NULL,
                    idgenero INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, idgenero),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (idgenero) REFERENCES genero(idgenero) ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculaactor (
                    idpelicula INTEGER NOT NULL,
                    idactor INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, idactor),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (idactor) REFERENCES actor(idactor) ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS peliculadirector (
                    idpelicula INTEGER NOT NULL,
                    iddirector INTEGER NOT NULL,
                    PRIMARY KEY (idpelicula, iddirector),
                    FOREIGN KEY (idpelicula) REFERENCES pelicula(idpelicula) ON DELETE CASCADE,
                    FOREIGN KEY (iddirector) REFERENCES director(iddirector) ON DELETE CASCADE
                )
            """);

            logger.log(Level.FINE, "Tablas intermedias verificadas.");

            // 5. Tabla MiLista (CRÍTICA para sync)
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
                    %s
                )
            """.formatted(SYNC_COLS));

            logger.log(Level.FINE, "Tabla 'milista' verificada.");

            stmt.close();
            logger.log(Level.INFO, "--- Esquema inicializado (Sync Ready) ---");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al inicializar el esquema", e);
        }
    }
}