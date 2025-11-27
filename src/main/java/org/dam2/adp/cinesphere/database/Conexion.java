package org.dam2.adp.cinesphere.database;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos.
 */
public class Conexion {

    private static Conexion instance;
    private Connection connection;

    private Conexion() {
    }

    /**
     * Obtiene la instancia única de Conexion.
     * @return la instancia de Conexion.
     */
    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

    /**
     * Conecta a la base de datos utilizando el archivo de configuración especificado.
     * @param configFile el nombre del archivo de configuración.
     */
    public void connect(String configFile) {
        if (connection != null) {
            return;
        }
        try {
            InputStream is = getClass().getResourceAsStream("/config/" + configFile);
            if (is == null) {
                throw new RuntimeException("Configuration file not found: " + configFile);
            }

            Properties properties = new Properties();
            properties.load(is);

            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");

            Class.forName(driver);

            // --- LÓGICA ESPECÍFICA PARA SQLITE ---
            if (url.startsWith("jdbc:sqlite")) {

                // Asegurar que la carpeta existe para evitar error de "path not found"
                // Asumiendo url tipo "jdbc:sqlite:database/archivo.db"
                File dbDir = new File("database");
                if (!dbDir.exists()) {
                    dbDir.mkdirs();
                }

                connection = DriverManager.getConnection(url);

                // Activar Foreign Keys en SQLite (por defecto están OFF)
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }

            } else {
                // Lógica PostgreSQL (o MySQL)
                connection = DriverManager.getConnection(url, user, password);
            }

            System.out.println("Connected to the database: " + url);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    /**
     * Obtiene la conexión a la base de datos.
     * @return la conexión a la base de datos.
     */
    public Connection getConnection() {
        if (connection == null) {
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
                System.out.println("Conexión a la base de datos cerrada.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
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
        } catch (Exception e) {
            return false;
        }
    }
}
