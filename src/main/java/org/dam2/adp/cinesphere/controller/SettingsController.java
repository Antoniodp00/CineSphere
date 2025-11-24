package org.dam2.adp.cinesphere.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.CsvImporter;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.dam2.adp.cinesphere.model.Usuario;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;

/**
 * Controlador para la vista de configuración de la aplicación.
 * (Actualmente sin implementación)
 */
public class SettingsController {

    @FXML private Button btnDescargarPlantilla;
    @FXML private TabPane tabPane;
    @FXML private Tab tabImportacion;
    @FXML
    private Button btnEliminarCuenta;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnGuardarPerfil;

    @FXML
    private Button btnImportarLocal;
    @FXML
    private Label lblEstadoLocal;
    @FXML
    private ComboBox<String> cbPredeterminados;
    @FXML
    private Button btnCargarPredeterminado;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void initialize() {
        cargarDatosUsuario();

        // Eventos Usuario
        btnGuardarPerfil.setOnAction(e -> guardarPerfil());
        btnEliminarCuenta.setOnAction(e -> eliminarUsuario());
        // Eventos Importación
        configurarImportacion();

        gestionarPermisos();
    }

    /**
     * Comprueba si el usuario tiene rol de ADMIN.
     * Si no lo es, elimina la pestaña de importación de la vista.
     */
    private void gestionarPermisos() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();

        // Si el usuario no existe o NO es administrador
        if (u != null && !u.isAdmin()) {
            // Eliminamos la pestaña del panel.
            // Es mejor eliminarla que deshabilitarla para que no la vean.
            tabPane.getTabs().remove(tabImportacion);
        }
    }

    private void cargarDatosUsuario() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        if (u != null) {
            txtUsuario.setText(u.getNombreUsuario());
            txtEmail.setText(u.getEmail());
        }
    }

    private void guardarPerfil() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        u.setNombreUsuario(txtUsuario.getText());
        u.setEmail(txtEmail.getText());
        u.setPassw(txtPassword.getText());

        if (u.getNombreUsuario().isBlank() || u.getEmail().isBlank() || u.getPassw().isBlank()) {
            AlertUtils.error("Rellene todos los campos");
            return;
        }
        try {
            usuarioDAO.update(u);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        AlertUtils.info("Perfil actualizado correctamente");
        txtPassword.clear();
    }

    private void eliminarUsuario() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        try {
            if (usuarioDAO.delete(u)) {
                AlertUtils.info("Usuario eliminado correctamente");
                SessionManager.getInstance().cerrarSesion();
                Navigation.navigate("login.fxml");
            } else {
                AlertUtils.error("Error al eliminar usuario");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void configurarImportacion() {
        btnImportarLocal.setOnAction(e -> importarCsvLocal());
        btnCargarPredeterminado.setOnAction(e -> importarCsvPredeterminado());

        cbPredeterminados.getItems().setAll(
                "IMDb Top Movies (Completo)",
                "Estrenos 2023",
                "Selección Cine Español",
                "Animación Clásica"
        );
        cbPredeterminados.getSelectionModel().selectFirst();
    }

    private void importarCsvLocal() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        File file = fileChooser.showOpenDialog(btnImportarLocal.getScene().getWindow());

        if (file != null) {
            lblEstadoLocal.setText(file.getName());
            ejecutarImportacion(importer -> importer.importar(file.getAbsolutePath()));
        }
    }

    private void importarCsvPredeterminado() {
        String seleccion = cbPredeterminados.getValue();
        if (seleccion != null) {
            String rutaRecurso = switch (seleccion) {
                case "IMDb Top Movies (Completo)" -> "/csv/IMDb_Data_final.csv";
                case "Estrenos 2023" -> "src/main/resources/csv/estrenos_2023.csv";
                case "Selección Cine Español" -> "src/main/resources/csv/cine_espanol.csv";
                case "Animación Clásica" -> "src/main/resources/csv/animacion_clasica.csv";
                default -> null;
            };

            if (rutaRecurso != null) {
                ejecutarImportacion(importer -> importer.importar(rutaRecurso));
            } else {
                AlertUtils.error("No se ha encontrado el archivo CSV");
            }
        }
    }

    private interface ImportAction {
        void execute(CsvImporter importer) throws Exception;
    }

    private void ejecutarImportacion(ImportAction action) {
        btnImportarLocal.setDisable(true);
        btnCargarPredeterminado.setDisable(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                CsvImporter importer = new CsvImporter();
                action.execute(importer);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            habilitarBotones();
            AlertUtils.info("Importación completada con éxito.");
            lblEstadoLocal.setText("No se ha seleccionado archivo");
        });

        task.setOnFailed(e -> {
            habilitarBotones();
            Throwable error = task.getException();
            error.printStackTrace();
            AlertUtils.error("Error al importar: " + error.getMessage());
        });

        new Thread(task).start();
    }

    private void habilitarBotones() {
        btnImportarLocal.setDisable(false);
        btnCargarPredeterminado.setDisable(false);
    }
}

