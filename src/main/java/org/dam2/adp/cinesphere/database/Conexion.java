package org.dam2.adp.cinesphere.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Conexion {

    private static Connection connection;

    public static void connect(String configFile) {
        try {
            InputStream is = Conexion.class.getResourceAsStream("/config/" + configFile);

            if (is == null) {
                throw new RuntimeException("No se encontró el archivo de configuración: " + configFile);
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

            System.out.println("Conectado a la BD: " + url);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al conectar a la base de datos");
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
