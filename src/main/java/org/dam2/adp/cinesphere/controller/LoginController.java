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
 * Gestiona la autenticación y selección de base de datos.
 */
public class LoginController {

    @FXML private ComboBox<String> cbBaseDatos;
    @FXML private Button btnConectar;
    @FXML private Label lblEstadoConexion;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistro;

    private UsuarioDAO usuarioDAO;
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private boolean isConnected = false;

    /**
     * Inicializa el controlador, configurando los listeners de los componentes.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando LoginController...");

        configurarComboBaseDatos();

        btnConectar.setOnAction(e -> conectar());
        btnLogin.setOnAction(e -> intentarLogin());
        linkRegistro.setOnAction(e -> Navigation.switchScene("register.fxml"));

        logger.log(Level.INFO, "LoginController inicializado.");
    }

    /**
     * Configura los ítems y el listener del ComboBox de selección de BD.
     */
    private void configurarComboBaseDatos() {
        cbBaseDatos.getItems().addAll("PostgreSQL (Online)", "SQLite (Local)");
        cbBaseDatos.getSelectionModel().selectFirst();

        // Si cambia la selección, se fuerza la desconexión visual y lógica
        cbBaseDatos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                resetearEstadoConexion();
            }
        });
    }

    /**
     * Resetea la UI y el estado lógico a "Desconectado".
     */
    private void resetearEstadoConexion() {
        isConnected = false;
        lblEstadoConexion.setText("Estado: Desconectado (Selección cambiada)");
        lblEstadoConexion.setStyle("-fx-text-fill: #7f8c8d;");
        habilitarFormulario(false);
        logger.log(Level.INFO, "Selección de BD cambiada. Estado reseteado a desconectado.");
    }

    /**
     * Intenta establecer la conexión a la base de datos y actualiza la UI.
     */
    private void conectar() {
        if (establecerConexion()) {
            actualizarUiConectado(true);
        } else {
            actualizarUiConectado(false);
        }
    }

    /**
     * Actualiza los elementos visuales basándose en el resultado de la conexión.
     * @param exito true si la conexión fue exitosa.
     */
    private void actualizarUiConectado(boolean exito) {
        isConnected = exito;
        habilitarFormulario(exito);

        if (exito) {
            lblEstadoConexion.setText("Estado: Conectado");
            lblEstadoConexion.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            lblEstadoConexion.setText("Estado: Error de conexión");
            lblEstadoConexion.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    /**
     * Intenta iniciar sesión con los datos introducidos por el usuario.
     */
    private void intentarLogin() {
        if (!validarPreLogin()) return;

        String nombreUsuario = txtUsuario.getText();
        String password = txtPassword.getText();

        try {
            if (usuarioDAO == null) usuarioDAO = new UsuarioDAO();

            Usuario u = usuarioDAO.findByName(nombreUsuario);

            if (u != null && BCrypt.checkpw(password, u.getPassw())) {
                realizarLoginExitoso(u);
            } else {
                AlertUtils.error("Usuario o contraseña incorrectos.");
                logger.log(Level.WARNING, "Login fallido para: " + nombreUsuario);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error durante el inicio de sesión", ex);
            AlertUtils.error("Error crítico al iniciar sesión: " + ex.getMessage());
        }
    }

    /**
     * Valida las precondiciones para intentar un login.
     * @return true si las validaciones son correctas.
     */
    private boolean validarPreLogin() {
        if (!isConnected) {
            AlertUtils.error("Por favor, conecta a una base de datos primero.");
            return false;
        }
        if (txtUsuario.getText().isBlank() || txtPassword.getText().isBlank()) {
            AlertUtils.error("Rellena todos los campos.");
            return false;
        }
        return true;
    }

    /**
     * Procede con el login una vez validado el usuario.
     * @param u El usuario autenticado.
     */
    private void realizarLoginExitoso(Usuario u) {
        SessionManager.getInstance().setUsuarioActual(u);
        logger.log(Level.INFO, "Inicio de sesión exitoso: " + u.getNombreUsuario());
        Navigation.switchScene("main.fxml");
    }

    /**
     * Establece la conexión a la base de datos.
     * @return true si la conexión se ha establecido correctamente, false en caso contrario.
     */
    private boolean establecerConexion() {
        String seleccion = cbBaseDatos.getValue();

        String archivoConfig = seleccion.startsWith("SQLite")
                ? "config-sqlite.properties"
                : "config-postgres.properties";

        logger.log(Level.INFO, "Conectando a: " + seleccion);

        try {
            Conexion.getInstance().disconnect();
            Conexion.getInstance().connect(archivoConfig);
            DatabaseSchema.inicializar();
            crearAdminPorDefecto();
            logger.log(Level.INFO, "Conexión establecida.");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fallo de conexión", e);
            AlertUtils.error("No se pudo conectar:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Crea el usuario administrador por defecto si no existe.
     */
    private void crearAdminPorDefecto() {
            try {
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.findByName("admin") == null) {
                    Usuario admin = new Usuario();
                    admin.setNombreUsuario("admin");
                    admin.setEmail("admin@cinesphere.com");
                    admin.setPassw(BCrypt.hashpw("admin", BCrypt.gensalt()));
                    admin.setBornDate(LocalDate.now());
                    admin.setRol(Rol.ADMIN);
                    dao.insert(admin);
                    logger.log(Level.INFO, "Admin creado.");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error creando admin", e);
            }
    }

    /**
     * Habilita o deshabilita los campos del formulario de login.
     * @param habilitar true para habilitar, false para deshabilitar.
     */
    private void habilitarFormulario(boolean habilitar) {
        txtUsuario.setDisable(!habilitar);
        txtPassword.setDisable(!habilitar);
        btnLogin.setDisable(!habilitar);
        linkRegistro.setDisable(!habilitar);
    }
}
