package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dam2.adp.cinesphere.util.Utils;
import org.mindrot.jbcrypt.BCrypt;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.AlertUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista de registro de nuevos usuarios.
 */
public class RegisterController {
    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpNacimiento;
    @FXML private Button btnRegistrar;
    @FXML private Hyperlink linkLogin;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final int EDAD_MINIMA = 14;
    private static final Logger logger = Logger.getLogger(RegisterController.class.getName());

    /**
     * Inicializa el controlador, configurando los listeners para el botón de registro y el enlace de login.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando RegisterController...");
        btnRegistrar.setOnAction(e -> registrar());
        linkLogin.setOnAction(e -> Navigation.navigate("login.fxml"));
        logger.log(Level.INFO, "RegisterController inicializado.");
    }

    /**
     * Gestiona el proceso de registro de un nuevo usuario. Valida los datos introducidos,
     * crea el usuario en la base de datos y navega a la pantalla de login.
     */
    private void registrar(){
        logger.log(Level.INFO, "Iniciando proceso de registro...");
        String nombreUsuario = txtUsuario.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if(nombreUsuario.isBlank() || email.isBlank() || password.isBlank()){
            AlertUtils.error("Rellena todos los campos.");
            logger.log(Level.WARNING, "Intento de registro con campos vacíos.");
            return;
        }

        if (!Utils.esEdadValida(dpNacimiento.getValue(), EDAD_MINIMA)) {
            AlertUtils.error("Fecha no válida. Debes tener al menos " + EDAD_MINIMA + " años y la fecha no puede ser futura.");
            return;
        }

        if (!Utils.esEmailValido(txtEmail.getText())) {
            AlertUtils.error("Correo electrónico no es válido (ejemplo: usuario@dominio.com).");
            return;
        }
        try{
            if(usuarioDAO.findByName(nombreUsuario) != null){
                AlertUtils.error("El usuario ya existe.");
                logger.log(Level.WARNING, "Intento de registro para un usuario ya existente: " + nombreUsuario);
                return;
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

            Usuario u = new Usuario();
            u.setNombreUsuario(nombreUsuario);
            u.setEmail(email);
            u.setPassw(hashed);
            u.setBornDate(dpNacimiento.getValue());

            usuarioDAO.insert(u);

            AlertUtils.info("Usuario creado correctamente");
            logger.log(Level.INFO, "Usuario '" + nombreUsuario + "' creado correctamente.");

            Navigation.navigate("login.fxml");

        }catch(Exception ex){
            logger.log(Level.SEVERE, "Error durante el registro del usuario", ex);
            AlertUtils.error("Error al registrar usuario.");
        }
    }
}
