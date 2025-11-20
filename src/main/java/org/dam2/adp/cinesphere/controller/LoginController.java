package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Hyperlink linkRegistro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void initialize() {
        btnLogin.setOnAction(e -> login());
        linkRegistro.setOnAction(e -> Navigation.switchScene("register.fxml"));
    }

    private void login() {
        String nombreUsuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (nombreUsuario.isBlank() || password.isBlank()) {
            AlertUtils.error("Rellena todos los campos.");
            return;
        }

        try {
            Usuario u = usuarioDAO.findByName(nombreUsuario);

            if (u == null) {
                AlertUtils.error("El usuario no existe.");
                return;
            }

            if(!BCrypt.checkpw(password,u.getPassw())){
                AlertUtils.error("Contraseña incorrecta.");
                return;
            }

            SessionManager.getInstance().setUsuarioActual(u);

            Navigation.switchScene("main.fxml");


        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtils.error("Error al iniciar sesión.");
        }
    }
}
