package org.dam2.adp.cinesphere.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.CsvImporter;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.dam2.adp.cinesphere.model.Usuario;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista de configuración de la aplicación.
 */
public class SettingsController {

    @FXML private Tab tabUsuarios;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, Rol> colRol;
    @FXML private Button btnHacerAdmin;
    @FXML private Button btnHacerUser;
    @FXML private Button btnEliminarUsuario;
    @FXML private Button btnNuevaPelicula;
    @FXML private Button btnDescargarPlantilla;
    @FXML private TabPane tabPane;
    @FXML private Tab tabImportacion;
    @FXML private Button btnEliminarCuenta;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnGuardarPerfil;
    @FXML private Button btnImportarLocal;
    @FXML private Label lblEstadoLocal;
    @FXML private ComboBox<String> cbPredeterminados;
    @FXML private Button btnCargarPredeterminado;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final Logger logger = Logger.getLogger(SettingsController.class.getName());

    /**
     * Inicializa el controlador, cargando los datos del usuario y configurando los listeners.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando SettingsController...");
        cargarDatosUsuario();

        btnGuardarPerfil.setOnAction(e -> guardarPerfil());
        btnEliminarCuenta.setOnAction(e -> eliminarUsuario());
        configurarImportacion();
        gestionarPermisos();

        if (SessionManager.getInstance().getUsuarioActual().isAdmin()) {
            configurarTablaUsuarios();
            cargarListaUsuarios();
        }

        if (btnNuevaPelicula != null) {
            btnNuevaPelicula.setOnAction(e -> abrirFormularioPelicula());
        }

        btnDescargarPlantilla.setOnAction(e -> descargarPlantilla());
        logger.log(Level.INFO, "SettingsController inicializado.");
    }

    /**
     * Gestiona los permisos de visualización de las pestañas según el rol del usuario.
     */
    private void gestionarPermisos() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        if (u != null && !u.isAdmin()) {
            logger.log(Level.INFO, "Usuario no es administrador. Ocultando pestañas de administración.");
            tabPane.getTabs().remove(tabImportacion);
            tabPane.getTabs().remove(tabUsuarios);
        }
    }

    /**
     * Carga los datos del usuario actual en los campos del formulario.
     */
    private void cargarDatosUsuario() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        if (u != null) {
            txtUsuario.setText(u.getNombreUsuario());
            txtEmail.setText(u.getEmail());
            logger.log(Level.INFO, "Datos del usuario '" + u.getNombreUsuario() + "' cargados en el formulario.");
        }
    }

    /**
     * Guarda los cambios realizados en el perfil del usuario.
     */
    private void guardarPerfil() {
        logger.log(Level.INFO, "Intentando guardar perfil...");
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        u.setNombreUsuario(txtUsuario.getText());
        u.setEmail(txtEmail.getText());
        u.setPassw(txtPassword.getText());

        if (u.getNombreUsuario().isBlank() || u.getEmail().isBlank() || u.getPassw().isBlank()) {
            AlertUtils.error("Rellene todos los campos");
            logger.log(Level.WARNING, "Intento de guardar perfil con campos vacíos.");
            return;
        }
        try {
            usuarioDAO.update(u);
            AlertUtils.info("Perfil actualizado correctamente");
            txtPassword.clear();
            logger.log(Level.INFO, "Perfil del usuario '" + u.getNombreUsuario() + "' actualizado correctamente.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar el perfil del usuario", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina la cuenta del usuario actual.
     */
    private void eliminarUsuario() {
        logger.log(Level.INFO, "Iniciando proceso de eliminación de cuenta...");
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        try {
            if (usuarioDAO.delete(u)) {
                AlertUtils.info("Usuario eliminado correctamente");
                logger.log(Level.INFO, "Usuario '" + u.getNombreUsuario() + "' eliminado correctamente.");
                SessionManager.getInstance().cerrarSesion();
                Navigation.navigate("login.fxml");
            } else {
                AlertUtils.error("Error al eliminar usuario");
                logger.log(Level.SEVERE, "Error al eliminar el usuario '" + u.getNombreUsuario() + "'.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de SQL al eliminar el usuario", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Configura los listeners para la importación de archivos CSV.
     */
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

    /**
     * Abre un selector de archivos para importar un CSV local.
     */
    private void importarCsvLocal() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        File file = fileChooser.showOpenDialog(btnImportarLocal.getScene().getWindow());

        if (file != null) {
            lblEstadoLocal.setText(file.getName());
            logger.log(Level.INFO, "Archivo CSV local seleccionado para importar: " + file.getAbsolutePath());
            ejecutarImportacion(importer -> importer.importar(file.getAbsolutePath()));
        }
    }

    /**
     * Importa un archivo CSV predeterminado de los recursos de la aplicación.
     */
    private void importarCsvPredeterminado() {
        String seleccion = cbPredeterminados.getValue();
        if (seleccion != null) {
            String rutaRecurso = switch (seleccion) {
                case "IMDb Top Movies (Completo)" -> "src/main/resources/csv/IMDb_Data_final.csv";
                case "Estrenos 2023" -> "src/main/resources/csv/estrenos_2023.csv";
                case "Selección Cine Español" -> "src/main/resources/csv/cine_espanol.csv";
                case "Animación Clásica" -> "src/main/resources/csv/animacion_clasica.csv";
                default -> null;
            };

            if (rutaRecurso != null) {
                logger.log(Level.INFO, "Importando archivo CSV predeterminado: " + seleccion);
                ejecutarImportacion(importer -> importer.importar(rutaRecurso));
            } else {
                AlertUtils.error("No se ha encontrado el archivo CSV");
                logger.log(Level.SEVERE, "No se encontró la ruta del recurso para la selección: " + seleccion);
            }
        }
    }

    /**
     * Interfaz funcional para definir una acción de importación.
     */
    private interface ImportAction {
        void execute(CsvImporter importer) throws Exception;
    }

    /**
     * Ejecuta una acción de importación en un hilo separado.
     * @param action la acción de importación a ejecutar.
     */
    private void ejecutarImportacion(ImportAction action) {
        btnImportarLocal.setDisable(true);
        btnCargarPredeterminado.setDisable(true);
        logger.log(Level.INFO, "Ejecutando importación en un hilo separado...");

        Task<Void> task = new Task<>() {
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
            logger.log(Level.INFO, "Importación completada con éxito.");
        });

        task.setOnFailed(e -> {
            habilitarBotones();
            Throwable error = task.getException();
            logger.log(Level.SEVERE, "Error durante la importación", error);
            AlertUtils.error("Error al importar: " + error.getMessage());
        });

        new Thread(task).start();
    }

    /**
     * Habilita los botones de importación.
     */
    private void habilitarBotones() {
        btnImportarLocal.setDisable(false);
        btnCargarPredeterminado.setDisable(false);
    }

    /**
     * Configura la tabla de usuarios y sus listeners.
     */
    private void configurarTablaUsuarios() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        btnHacerAdmin.setOnAction(e -> cambiarRolSeleccionado(Rol.ADMIN));
        btnHacerUser.setOnAction(e -> cambiarRolSeleccionado(Rol.USER));
        btnEliminarUsuario.setOnAction(e -> eliminarUsuarioSeleccionado());
    }

    /**
     * Carga la lista de usuarios en la tabla.
     */
    private void cargarListaUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.findAll();
            tablaUsuarios.setItems(FXCollections.observableArrayList(lista));
            logger.log(Level.INFO, "Lista de usuarios cargada en la tabla.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al cargar la lista de usuarios", e);
            AlertUtils.error("Error al cargar usuarios: " + e.getMessage());
        }
    }

    /**
     * Cambia el rol del usuario seleccionado en la tabla.
     * @param nuevoRol el nuevo rol a asignar.
     */
    private void cambiarRolSeleccionado(Rol nuevoRol) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.error("Selecciona un usuario de la lista.");
            logger.log(Level.WARNING, "Intento de cambiar rol sin seleccionar un usuario.");
            return;
        }

        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes cambiar tu propio rol.");
            logger.log(Level.WARNING, "Intento de cambiar el rol del propio usuario.");
            return;
        }

        try {
            usuarioDAO.updateRol(seleccionado.getIdUsuario(), nuevoRol);
            seleccionado.setRol(nuevoRol);
            tablaUsuarios.refresh();
            AlertUtils.info("Rol de " + seleccionado.getNombreUsuario() + " actualizado a " + nuevoRol);
            logger.log(Level.INFO, "Rol del usuario '" + seleccionado.getNombreUsuario() + "' actualizado a " + nuevoRol);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar el rol del usuario", e);
            AlertUtils.error("Error al actualizar rol.");
        }
    }

    /**
     * Elimina el usuario seleccionado en la tabla.
     */
    private void eliminarUsuarioSeleccionado() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.error("Selecciona un usuario.");
            logger.log(Level.WARNING, "Intento de eliminar usuario sin seleccionar uno.");
            return;
        }

        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes eliminar tu propia cuenta desde aquí.");
            logger.log(Level.WARNING, "Intento de eliminar la propia cuenta desde la tabla de administración.");
            return;
        }

        if (AlertUtils.confirmation("Eliminar Usuario", "¿Estás seguro?", "Vas a eliminar a " + seleccionado.getNombreUsuario() + ". Esta acción es irreversible.")) {
            try {
                usuarioDAO.delete(seleccionado);
                tablaUsuarios.getItems().remove(seleccionado);
                AlertUtils.info("Usuario eliminado.");
                logger.log(Level.INFO, "Usuario '" + seleccionado.getNombreUsuario() + "' eliminado por un administrador.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error al eliminar el usuario seleccionado", e);
                AlertUtils.error("Error al eliminar usuario.");
            }
        }
    }

    /**
     * Abre el formulario para añadir una nueva película.
     */
    private void abrirFormularioPelicula() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pelicula_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Añadir Película - CineSphere Admin");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            logger.log(Level.INFO, "Abriendo formulario para añadir nueva película.");
            stage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo abrir el formulario de nueva película", e);
            AlertUtils.error("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    /**
     * Genera y descarga un archivo CSV de plantilla con las cabeceras correctas y un ejemplo.
     */
    private void descargarPlantilla() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plantilla CSV");
        fileChooser.setInitialFileName("plantilla_peliculas.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(btnDescargarPlantilla.getScene().getWindow());

        if (file != null) {
            logger.log(Level.INFO, "Descargando plantilla CSV en: " + file.getAbsolutePath());
            guardarArchivoPlantilla(file);
        }
    }

    /**
     * Guarda el archivo de plantilla CSV en la ubicación especificada.
     * @param file el archivo donde se guardará la plantilla.
     */
    private void guardarArchivoPlantilla(File file) {
        String cabeceras = "Title,Director,Stars,IMDb-Rating,Category,Duration,Censor-board-rating,ReleaseYear";
        String ejemplo = "Ejemplo de Película,\"Director Uno, Director Dos\",\"Actor A, Actor B\",8.5,\"Action, Drama\",120min,PG-13,2024";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(cabeceras);
            writer.newLine();
            writer.write(ejemplo);
            AlertUtils.info("Plantilla guardada correctamente en:\n" + file.getAbsolutePath());
            logger.log(Level.INFO, "Plantilla CSV guardada correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al guardar la plantilla CSV", e);
            AlertUtils.error("Error al guardar la plantilla: " + e.getMessage());
        }
    }
}
