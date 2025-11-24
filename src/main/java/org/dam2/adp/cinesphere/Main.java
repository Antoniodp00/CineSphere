package org.dam2.adp.cinesphere;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.mindrot.jbcrypt.BCrypt; // Importante para hashear la contraseña

import java.sql.SQLException;
import java.time.LocalDate;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Conectar a la BBDD
        Conexion.getInstance().connect("config-postgres.properties");

        // 2. Comprobar y crear admin si no existe (NUEVO PASO)
        crearAdminPorDefecto();

        // 3. Iniciar la interfaz gráfica
        Navigation.setStage(stage);
        stage.setTitle("CineSphere");
        Navigation.switchScene("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Verifica si existe el usuario 'admin'. Si no existe, lo crea.
     */
    private void crearAdminPorDefecto() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            // Buscamos si ya existe un usuario llamado "admin"
            Usuario adminExistente = usuarioDAO.findByName("admin");

            if (adminExistente == null) {
                System.out.println("--- Primer inicio detectado: Creando usuario ADMIN ---");

                Usuario admin = new Usuario();
                admin.setNombreUsuario("admin");
                admin.setEmail("admin@cinesphere.com"); // Email dummy

                // IMPORTANTE: Hashear la contraseña, igual que en el registro
                String passHasheada = BCrypt.hashpw("admin", BCrypt.gensalt());
                admin.setPassw(passHasheada);

                admin.setBornDate(LocalDate.now());

                // Asignamos el ROL DE ADMINISTRADOR
                admin.setRol(Rol.ADMIN);

                usuarioDAO.insert(admin);

                System.out.println("Usuario 'admin' creado con contraseña 'admin'.");
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar/crear el usuario admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}