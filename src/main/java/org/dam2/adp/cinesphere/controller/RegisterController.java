package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.AlertUtils;

public class RegisterController {
    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpNacimiento;
    @FXML private Button btnRegistrar;
    @FXML private Hyperlink linkLogin;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void initialize() {
    btnRegistrar.setOnAction(e->registrar());
    linkLogin.setOnAction(e->Navigation.navigate("login.fxml"));

    }

    private void registrar(){
        String nombreUsuario = txtUsuario.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if(nombreUsuario.isBlank() || email.isBlank() || password.isBlank()){
            AlertUtils.error("Rellena todos los campos.");
            return;
        }

        try{
            if(usuarioDAO.findByName(nombreUsuario) != null){
                AlertUtils.error("El usuario ya existe.");
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

            Navigation.navigate("login.fxml");

        }catch(Exception ex){
            ex.printStackTrace();
            AlertUtils.error("Error al registrar usuario.");
        }
    }


}
