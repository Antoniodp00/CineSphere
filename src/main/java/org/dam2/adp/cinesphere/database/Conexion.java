package org.dam2.adp.cinesphere.database;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase Singleton para gestionar la conexión dual (PostgreSQL o SQLite)
 * leyendo los parámetros desde un archivo de configuración externo (config.properties).
 * Cumple con el requisito de Patrón Singleton y Configuración Externa.
 */
public class Conexion {

    private static Connection conn = null;

    private Conexion() {}

    public static void connect(String configPath) throws Exception {
        if (conn != null && !conn.isClosed()) return;

        Properties props = new Properties();
        props.load(new FileInputStream(configPath));

        String driver = props.getProperty("db.driver");
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user", "");
        String pass = props.getProperty("db.password", "");

        Class.forName(driver);

        if (user.isEmpty())
            conn = DriverManager.getConnection(url);
        else
            conn = DriverManager.getConnection(url, user, pass);

        // Activar FK en SQLite
        if (url.contains("sqlite"))
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
    }

    public static Connection getConnection() {
        return conn;
    }

    public static void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
