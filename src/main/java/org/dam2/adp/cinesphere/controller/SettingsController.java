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

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para la vista de configuración de la aplicación.
 * (Actualmente sin implementación)
 */
public class SettingsController {

    @FXML private Tab tabUsuarios;
    @FXML private TableView tablaUsuarios;
    @FXML private TableColumn colId;
    @FXML private TableColumn colNombre;
    @FXML private TableColumn colEmail;
    @FXML private TableColumn colRol;
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

    @FXML
    private void initialize() {
        cargarDatosUsuario();

        // Eventos Usuario
        btnGuardarPerfil.setOnAction(e -> guardarPerfil());
        btnEliminarCuenta.setOnAction(e -> eliminarUsuario());
        // Eventos Importación
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
            tabPane.getTabs().remove(tabUsuarios);
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

    private void configurarTablaUsuarios() {
        // Enlazar columnas con atributos del modelo Usuario
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        // Botones de acción
        btnHacerAdmin.setOnAction(e -> cambiarRolSeleccionado(Rol.ADMIN));
        btnHacerUser.setOnAction(e -> cambiarRolSeleccionado(Rol.USER));
        btnEliminarUsuario.setOnAction(e -> eliminarUsuarioSeleccionado());
    }

    private void cargarListaUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.findAll();
            tablaUsuarios.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.error("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void cambiarRolSeleccionado(Rol nuevoRol) {
        Usuario seleccionado = (Usuario) tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.error("Selecciona un usuario de la lista.");
            return;
        }

        // Evitar cambiarse el rol a sí mismo (para no perder acceso accidentalmente)
        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes cambiar tu propio rol.");
            return;
        }

        try {
            usuarioDAO.updateRol(seleccionado.getIdUsuario(), nuevoRol);

            // Actualizar la tabla visualmente
            seleccionado.setRol(nuevoRol);
            tablaUsuarios.refresh();

            AlertUtils.info("Rol de " + seleccionado.getNombreUsuario() + " actualizado a " + nuevoRol);

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.error("Error al actualizar rol.");
        }
    }

    private void eliminarUsuarioSeleccionado() {
        Usuario seleccionado = (Usuario) tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.error("Selecciona un usuario.");
            return;
        }

        Usuario yo = SessionManager.getInstance().getUsuarioActual();
        if (seleccionado.getIdUsuario() == yo.getIdUsuario()) {
            AlertUtils.error("No puedes eliminar tu propia cuenta desde aquí.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Usuario");
        confirm.setHeaderText("¿Estás seguro?");
        confirm.setContentText("Vas a eliminar a " + seleccionado.getNombreUsuario() + ". Esta acción es irreversible.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                usuarioDAO.delete(seleccionado);
                tablaUsuarios.getItems().remove(seleccionado);
                AlertUtils.info("Usuario eliminado.");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.error("Error al eliminar usuario.");
            }
        }
    }

    private void abrirFormularioPelicula() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pelicula_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Añadir Película - CineSphere Admin");
            stage.setScene(new Scene(root));

            // Configuración Modal: Bloquea la ventana de atrás hasta que se cierre esta
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
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

        // Mostrar diálogo de guardado
        File file = fileChooser.showSaveDialog(btnDescargarPlantilla.getScene().getWindow());

        if (file != null) {
            guardarArchivoPlantilla(file);
        }
    }

    private void guardarArchivoPlantilla(File file) {
        // Cabeceras exactas que espera CsvImporter
        String cabeceras = "Title,Director,Stars,IMDb-Rating,Category,Duration,Censor-board-rating,ReleaseYear";

        // Fila de ejemplo basada en el formato de IMDb_Data_final.csv
        // Nota: Las listas (actores, directores) van entre comillas dobles si contienen comas.
        String ejemplo = "Ejemplo de Película,\"Director Uno, Director Dos\",\"Actor A, Actor B\",8.5,\"Action, Drama\",120min,PG-13,2024";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(cabeceras);
            writer.newLine();
            writer.write(ejemplo);

            AlertUtils.info("Plantilla guardada correctamente en:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.error("Error al guardar la plantilla: " + e.getMessage());
        }
    }
}

