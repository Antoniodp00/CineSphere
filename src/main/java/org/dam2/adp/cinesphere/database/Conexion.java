package org.dam2.adp.cinesphere.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Conexion {

    private static Conexion instance;
    private Connection connection;

    private Conexion() {
    }

    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

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

            if (url.startsWith("jdbc:sqlite")) {
                connection = DriverManager.getConnection(url);
            } else {
                connection = DriverManager.getConnection(url, user, password);
            }

            System.out.println("Connected to the database: " + url);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database is not connected. Call connect() first.");
        }
        return connection;
    }
}
