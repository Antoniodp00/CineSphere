package org.dam2.adp.cinesphere.database;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la conexión a la base de datos.
 */
public class Conexion {

    private static Conexion instance;
    private Connection connection;
    private static final Logger logger = Logger.getLogger(Conexion.class.getName());

    private Conexion() {
    }

    /**
     * Obtiene la instancia única de Conexion.
     * @return la instancia de Conexion.
     */
    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
            logger.log(Level.INFO, "Instancia de Conexion creada.");
        }
        return instance;
    }

    /**
     * Conecta a la base de datos utilizando el archivo de configuración especificado.
     * @param configFile el nombre del archivo de configuración.
     */
    public void connect(String configFile) {
        if (connection != null) {
            logger.log(Level.INFO, "La conexión a la base de datos ya existe. No se creará una nueva.");
            return;
        }
        try {
            InputStream is = getClass().getResourceAsStream("/config/" + configFile);
            if (is == null) {
                logger.log(Level.SEVERE, "No se encontró el archivo de configuración: " + configFile);
                throw new RuntimeException("Configuration file not found: " + configFile);
            }

            Properties properties = new Properties();
            properties.load(is);

            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");

            Class.forName(driver);

            if (url.startsWith("jdbc:sqlite")) {
                logger.log(Level.INFO, "Detectada base de datos SQLite.");
                File dbDir = new File("database");
                if (!dbDir.exists()) {
                    dbDir.mkdirs();
                    logger.log(Level.INFO, "Directorio de base de datos creado en: " + dbDir.getAbsolutePath());
                }

                connection = DriverManager.getConnection(url);

                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                    logger.log(Level.INFO, "PRAGMA foreign_keys = ON ejecutado para SQLite.");
                }

            } else {
                logger.log(Level.INFO, "Conectando a base de datos SQL estándar.");
                connection = DriverManager.getConnection(url, user, password);
            }

            logger.log(Level.INFO, "Conectado a la base de datos: " + url);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al conectar a la base de datos", e);
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    /**
     * Obtiene la conexión a la base de datos.
     * @return la conexión a la base de datos.
     */
    public Connection getConnection() {
        if (connection == null) {
            logger.log(Level.SEVERE, "La base de datos no está conectada. Se debe llamar a connect() primero.");
            throw new IllegalStateException("Database is not connected. Call connect() first.");
        }
        return connection;
    }

    /**
     * Cierra la conexión activa con la base de datos.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.log(Level.INFO, "Conexión a la base de datos cerrada.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al cerrar la conexión a la base de datos", e);
        } finally {
            connection = null;
        }
    }

    /**
     * Método auxiliar para detectar si estamos en modo SQLite.
     * Necesario para DatabaseSchema.java.
     * @return true si la conexión es SQLite, false en caso contrario.
     */
    public boolean isSQLite() {
        try {
            return connection != null && connection.getMetaData().getURL().startsWith("jdbc:sqlite");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "No se pudo determinar si la base de datos es SQLite.", e);
            return false;
        }
    }
}
