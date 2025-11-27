package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.database.DatabaseSchema;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la pantalla de login.
 */
public class LoginController {

    @FXML private ComboBox<String> cbBaseDatos;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistro;

    private UsuarioDAO usuarioDAO;
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    /**
     * Inicializa el controlador, configurando los listeners de los botones.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando LoginController...");
        cbBaseDatos.getItems().addAll("PostgreSQL", "SQLite (Local)");
        cbBaseDatos.getSelectionModel().selectFirst();

        btnLogin.setOnAction(e -> intentarLogin());

        linkRegistro.setOnAction(e -> {
            if (establecerConexion()) {
                Navigation.switchScene("register.fxml");
            }
        });
        logger.log(Level.INFO, "LoginController inicializado.");
    }

    /**
     * Intenta iniciar sesión con los datos introducidos por el usuario.
     */
    private void intentarLogin() {
        logger.log(Level.INFO, "Intento de inicio de sesión...");
        if (!establecerConexion()) {
            return;
        }

        String nombreUsuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (nombreUsuario.isBlank() || password.isBlank()) {
            AlertUtils.error("Rellena todos los campos.");
            logger.log(Level.WARNING, "Intento de login con campos vacíos.");
            return;
        }

        try {
            if (usuarioDAO == null) usuarioDAO = new UsuarioDAO();

            Usuario u = usuarioDAO.findByName(nombreUsuario);

            if (u == null) {
                AlertUtils.error("El usuario no existe en esta base de datos.");
                logger.log(Level.WARNING, "Intento de login para un usuario no existente: " + nombreUsuario);
                return;
            }

            if (!BCrypt.checkpw(password, u.getPassw())) {
                AlertUtils.error("Contraseña incorrecta.");
                logger.log(Level.WARNING, "Intento de login con contraseña incorrecta para el usuario: " + nombreUsuario);
                return;
            }

            SessionManager.getInstance().setUsuarioActual(u);
            logger.log(Level.INFO, "Inicio de sesión exitoso para el usuario: " + nombreUsuario);
            Navigation.switchScene("main.fxml");

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error durante el inicio de sesión", ex);
            AlertUtils.error("Error al iniciar sesión: " + ex.getMessage());
        }
    }

    /**
     * Establece la conexión a la base de datos, inicializa el esquema y crea el usuario administrador por defecto.
     * @return true si la conexión se ha establecido correctamente, false en caso contrario.
     */
    private boolean establecerConexion() {
        String seleccion = cbBaseDatos.getValue();
        String archivoConfig = seleccion.startsWith("SQLite") ? "config-sqlite.properties" : "config-postgres.properties";
        logger.log(Level.INFO, "Estableciendo conexión con la base de datos: " + seleccion);

        try {
            Conexion.getInstance().disconnect();
            Conexion.getInstance().connect(archivoConfig);
            DatabaseSchema.inicializar();
            crearAdminPorDefecto();
            logger.log(Level.INFO, "Conexión establecida y esquema inicializado correctamente.");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo conectar a la base de datos", e);
            AlertUtils.error("No se pudo conectar a la base de datos:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Crea el usuario administrador por defecto si no existe.
     */
    private void crearAdminPorDefecto() {
        UsuarioDAO dao = new UsuarioDAO();
        try {
            if (dao.findByName("admin") == null) {
                Usuario admin = new Usuario();
                admin.setNombreUsuario("admin");
                admin.setEmail("admin@cinesphere.com");
                admin.setPassw(BCrypt.hashpw("admin", BCrypt.gensalt()));
                admin.setBornDate(LocalDate.now());
                admin.setRol(Rol.ADMIN);

                dao.insert(admin);
                logger.log(Level.INFO, "Usuario 'admin' por defecto creado en la base de datos actual.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al comprobar o crear el usuario 'admin' por defecto", e);
        }
    }
}
