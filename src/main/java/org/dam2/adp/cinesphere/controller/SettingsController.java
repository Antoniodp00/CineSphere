package org.dam2.adp.cinesphere.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.CsvImporter;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista de configuración y administración.
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
        }
    }

    /**
     * Guarda los cambios realizados en el perfil del usuario.
     */
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
            AlertUtils.info("Perfil actualizado correctamente");
            txtPassword.clear();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar perfil", e);
            AlertUtils.error("Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Elimina la cuenta del usuario actual.
     */
    private void eliminarUsuario() {
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        try {
            if (usuarioDAO.delete(u)) {
                AlertUtils.info("Usuario eliminado correctamente");
                SessionManager.getInstance().cerrarSesion();
                Navigation.switchScene("login.fxml");
            } else {
                AlertUtils.error("Error al eliminar usuario");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error SQL eliminar usuario", e);
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File file = fileChooser.showOpenDialog(btnImportarLocal.getScene().getWindow());

        if (file != null) {
            lblEstadoLocal.setText(file.getName());
            ejecutarImportacion(file.getAbsolutePath(), false);
        }
    }

    /**
     * Importa un archivo CSV predeterminado de los recursos de la aplicación.
     */
    private void importarCsvPredeterminado() {
        String seleccion = cbPredeterminados.getValue();
        if (seleccion != null) {

            String rutaRecurso = switch (seleccion) {
                case "IMDb Top Movies (Completo)" -> "/csv/IMDb_Data_final.csv";
                case "Estrenos 2023" -> "/csv/estrenos_2023.csv";
                case "Selección Cine Español" -> "/csv/cine_espanol.csv";
                case "Animación Clásica" -> "/csv/animacion_clasica.csv";
                default -> null;
            };

            if (rutaRecurso != null) {
                ejecutarImportacion(rutaRecurso, true);
            } else {
                AlertUtils.error("Recurso no encontrado para: " + seleccion);
            }
        }
    }

    /**
     * Ejecuta la importación en el hilo principal.
     * @param ruta Ruta del archivo o recurso.
     * @param esRecursoInterno true si está en el classpath, false si es disco local.
     */
    private void ejecutarImportacion(String ruta, boolean esRecursoInterno) {
        btnImportarLocal.setDisable(true);
        btnCargarPredeterminado.setDisable(true);
        logger.log(Level.INFO, "Iniciando importación síncrona desde: " + ruta);

        lblEstadoLocal.setText("Procesando...");

        try {

            if (esRecursoInterno) {
                CsvImporter.importarDesdeRecurso(ruta);
            } else {
                CsvImporter.importarLocal(ruta);
            }

            AlertUtils.info("Importación completada con éxito.");
            lblEstadoLocal.setText("Finalizado correctamente.");
            logger.log(Level.INFO, "Importación finalizada.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante la importación", e);
            AlertUtils.error("Error al importar: " + e.getMessage());
            lblEstadoLocal.setText("Error en la importación.");
        } finally {
            btnImportarLocal.setDisable(false);
            btnCargarPredeterminado.setDisable(false);
        }
    }

    /**
     * Configura la tabla de usuarios y sus listeners.
     */
    private void configurarTablaUsuarios() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaUsuarios.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        btnHacerAdmin.setDisable(true);
        btnHacerUser.setDisable(true);
        btnEliminarUsuario.setDisable(true);

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            btnHacerAdmin.setDisable(!isSelected);
            btnHacerUser.setDisable(!isSelected);
            btnEliminarUsuario.setDisable(!isSelected);
        });

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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error cargando usuarios", e);
        }
    }

    /**
     * Cambia el rol del usuario seleccionado en la tabla.
     * @param nuevoRol el nuevo rol a asignar.
     */
    private void cambiarRolSeleccionado(Rol nuevoRol) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.error("Selecciona un usuario.");
            return;
        }

        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes cambiar tu propio rol.");
            return;
        }

        try {
            usuarioDAO.updateRol(seleccionado.getIdUsuario(), nuevoRol);
            seleccionado.setRol(nuevoRol);
            tablaUsuarios.refresh();
            AlertUtils.info("Rol actualizado a " + nuevoRol);
        } catch (SQLException e) {
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
            return;
        }

        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes eliminarte a ti mismo aquí.");
            return;
        }

        if (AlertUtils.confirmacion("Eliminar Usuario", "¿Estás seguro?", "Esta acción es irreversible.")) {
            try {
                usuarioDAO.delete(seleccionado);
                tablaUsuarios.getItems().remove(seleccionado);
                AlertUtils.info("Usuario eliminado.");
            } catch (SQLException e) {
                AlertUtils.error("Error al eliminar.");
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
            stage.setTitle("Añadir Película");
            Scene scene = new Scene(root);
            Navigation.applyCineSphereStyles(scene); // Aplicar estilos centralizados
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error abriendo formulario", e);
            AlertUtils.error("Error: " + e.getMessage());
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
            guardarArchivoPlantilla(file);
        }
    }

    /**
     * Guarda el archivo de plantilla CSV en la ubicación especificada.
     * @param file el archivo donde se guardará la plantilla.
     */
    private void guardarArchivoPlantilla(File file) {
        String cabeceras = "Title,Director,Stars,IMDb-Rating,Category,Duration,Censor-board-rating,ReleaseYear";
        String ejemplo = "Ejemplo Pelicula,\"Director A\",\"Actor 1, Actor 2\",8.5,\"Action, Drama\",120min,PG-13,2024";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(cabeceras);
            writer.newLine();
            writer.write(ejemplo);
            AlertUtils.info("Plantilla guardada.");
        } catch (IOException e) {
            AlertUtils.error("Error guardando plantilla.");
        }
    }
}
