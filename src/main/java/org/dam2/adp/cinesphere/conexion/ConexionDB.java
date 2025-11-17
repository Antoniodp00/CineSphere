package org.dam2.adp.cinesphere.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase Singleton para gestionar la conexión dual (PostgreSQL o SQLite)
 * leyendo los parámetros desde un archivo de configuración externo (config.properties).
 * Cumple con el requisito de Patrón Singleton y Configuración Externa.
 */
public class ConexionDB {

    // Logger para registrar eventos (ayuda a la depuración de transacciones)
    private static final Logger logger = LoggerFactory.getLogger(ConexionDB.class);

    // Instancia única (Singleton)
    private static ConexionDB instancia;

    // Conexión activa
    private Connection connection;

    // Configuración
    private Configuration config;
    private final String dbMode;

    // Constructor privado para evitar la instanciación externa (Patrón Singleton)
    private ConexionDB() throws Exception {
        // 1. Cargar la configuración
        try {
            // Carga el archivo config.properties desde src/main/resources
            config = new PropertiesConfiguration("config.properties");
            dbMode = config.getString("db.modo").toUpperCase();

            logger.info("Modo de BD seleccionado: {}", dbMode);

            // 2. Cargar el Driver JDBC apropiado
            String driver;
            if (dbMode.equals("POSTGRES")) {
                driver = config.getString("postgres.driver");
            } else if (dbMode.equals("SQLITE")) {
                driver = config.getString("sqlite.driver");
            } else {
                throw new IllegalArgumentException("Modo de BD '" + dbMode + "' no válido en config.properties.");
            }

            // Intenta cargar el driver. Si falla, lanza una excepción.
            Class.forName(driver);

        } catch (Exception e) {
            logger.error("Error FATAL al inicializar la conexión o cargar la configuración.", e);
            throw new Exception("Error al cargar la configuración o el driver JDBC.", e);
        }
    }

    /**
     * Método estático para obtener la única instancia de la clase (Singleton).
     */
    public static ConexionDB getInstancia() throws Exception {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Establece y devuelve la conexión a la base de datos configurada.
     * @return Objeto Connection activo.
     * @throws SQLException Si ocurre un error de conexión.
     */
    public Connection conectar() throws SQLException {
        // Revisa si la conexión es nula o está cerrada
        if (connection == null || connection.isClosed()) {
            logger.debug("Estableciendo nueva conexión en modo: {}", dbMode);
            String url;
            String user = null;
            String password = null;

            if (dbMode.equals("POSTGRES")) {
                url = config.getString("postgres.url");
                user = config.getString("postgres.user");
                password = config.getString("postgres.password");
                connection = DriverManager.getConnection(url, user, password);
            } else if (dbMode.equals("SQLITE")) {
                url = config.getString("sqlite.url");
                // NOTA: La lógica de crear la BD SQLite si no existe iría aquí
                connection = DriverManager.getConnection(url);
            }

            // Por defecto, autoCommit = true para operaciones individuales.
            // Para transacciones (Tarea P2.x), los DAOs deberán ponerlo en 'false'.
            connection.setAutoCommit(true);
            logger.info("Conexión a BD {} establecida correctamente.", dbMode);
        }
        return connection;
    }

    /**
     * Cierra la conexión activa si existe y no está ya cerrada.
     */
    public void cerrar() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    connection = null; // Marcar como nula para forzar una nueva conexión en el futuro
                    logger.info("Conexión a BD cerrada.");
                }
            } catch (SQLException e) {
                logger.error("Error al cerrar la conexión: {}", e.getMessage());
            }
        }
    }

    /**
     * Devuelve el modo de base de datos actual.
     */
    public String getDbMode() {
        return dbMode;
    }
}