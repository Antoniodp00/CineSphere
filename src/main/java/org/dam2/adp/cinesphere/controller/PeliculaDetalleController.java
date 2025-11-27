package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaDAO;
import org.dam2.adp.cinesphere.model.MiLista;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.model.PeliculaEstado;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controlador para la vista de detalle de una película.
 */
public class PeliculaDetalleController {

    @FXML private Button btnEliminarPelicula;
    @FXML private ImageView imgPoster;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblRating;
    @FXML private Label lblClasificacion;
    @FXML private Label lblSinopsis;
    @FXML private FlowPane flowGeneros;
    @FXML private FlowPane flowDirectores;
    @FXML private FlowPane flowActores;
    @FXML private Button btnMiLista;
    @FXML private Button btnTrailer;
    @FXML private ComboBox<PeliculaEstado> cbEstado;
    @FXML private ComboBox<Integer> cbPuntuacion;

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final MiListaDAO miListaDAO = new MiListaDAO();

    private Pelicula pelicula;
    private Usuario usuario;

    /**
     * Inicializa el controlador, cargando los datos de la película seleccionada.
     */
    @FXML
    private void initialize() {
        usuario = SessionManager.getInstance().getUsuarioActual();
        Integer idPelicula = (Integer) SessionManager.getInstance().get("selectedPeliculaId");

        if (idPelicula == null) {
            Navigation.navigate("peliculas_lista.fxml");
            return;
        }

        setupComboBoxes();
        cargarDatos(idPelicula);

        if (usuario != null && usuario.isAdmin()) {
            btnEliminarPelicula.setVisible(true);
            btnEliminarPelicula.setOnAction(e -> eliminarPelicula());
        } else {
            btnEliminarPelicula.setVisible(false);
            btnEliminarPelicula.setManaged(false);
        }
    }

    /**
     * Configura los ComboBox de estado y puntuación.
     */
    private void setupComboBoxes() {
        cbEstado.getItems().setAll(PeliculaEstado.values());
        cbPuntuacion.getItems().setAll(
                IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList())
        );

        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cambiarEstado(newVal);
        });
        cbPuntuacion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cambiarPuntuacion(newVal);
        });
    }

    /**
     * Carga los datos de la película y los muestra en la interfaz.
     * @param idPelicula el ID de la película a cargar.
     */
    private void cargarDatos(int idPelicula) {
        try {
            pelicula = peliculaDAO.findByIdEager(idPelicula);
            if (pelicula == null) return;

            lblTitulo.setText(pelicula.getTituloPelicula());
            lblSubtitulo.setText(pelicula.getYearPelicula() + " • " + pelicula.getNombreClasificacion());
            lblRating.setText("★ " + (pelicula.getRatingPelicula() != null ? pelicula.getRatingPelicula() : "—"));
            lblClasificacion.setText(pelicula.getNombreClasificacion());
            lblSinopsis.setText("Sinopsis no disponible aún.");

            imgPoster.setImage(new Image(getClass().getResource("/img/noImage.png").toExternalForm()));

            pelicula.getGeneros().forEach(g -> flowGeneros.getChildren().add(chip(g.getNombreGenero())));
            pelicula.getDirectores().forEach(d -> flowDirectores.getChildren().add(chip(d.getNombreDirector())));
            pelicula.getActores().forEach(a -> flowActores.getChildren().add(chip(a.getNombreActor())));

            actualizarEstadoMiLista();

            btnMiLista.setOnAction(e -> toggleMiLista());
            btnTrailer.setOnAction(e -> abrirTrailer());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza el estado de la interfaz relacionado con "Mi Lista" (botones, ComboBoxes).
     * @throws Exception si ocurre un error al acceder a la base de datos.
     */
    private void actualizarEstadoMiLista() throws Exception {
        MiLista ml = miListaDAO.find(usuario.getIdUsuario(), pelicula.getIdPelicula());
        boolean enLista = ml != null;

        if (enLista) {
            btnMiLista.setText("En tu lista");
            cbEstado.setValue(ml.getEstado());
            cbPuntuacion.setValue(ml.getPuntuacion());
        } else {
            btnMiLista.setText("Añadir a mi lista");
            cbEstado.setValue(null);
            cbPuntuacion.setValue(null);
        }

        cbEstado.setDisable(!enLista);
        cbPuntuacion.setDisable(!enLista);
    }

    /**
     * Crea una etiqueta con estilo de "chip".
     * @param text el texto de la etiqueta.
     * @return una etiqueta con el estilo aplicado.
     */
    private Label chip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("chip");
        return l;
    }

    /**
     * Añade o elimina la película de "Mi Lista".
     */
    private void toggleMiLista() {
        try {
            MiLista ml = miListaDAO.find(usuario.getIdUsuario(), pelicula.getIdPelicula());
            if (ml == null) {
                miListaDAO.insert(new MiLista(
                        pelicula, usuario, PeliculaEstado.PENDIENTE, null, null, LocalDateTime.now()
                ));
            } else {
                miListaDAO.delete(usuario.getIdUsuario(), pelicula.getIdPelicula());
            }
            actualizarEstadoMiLista();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cambia el estado de la película en "Mi Lista".
     * @param estado el nuevo estado de la película.
     */
    private void cambiarEstado(PeliculaEstado estado) {
        try {
            miListaDAO.updateEstado(usuario.getIdUsuario(), pelicula.getIdPelicula(), estado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cambia la puntuación de la película en "Mi Lista".
     * @param puntuacion la nueva puntuación de la película.
     */
    private void cambiarPuntuacion(int puntuacion) {
        try {
            miListaDAO.updatePuntuacion(usuario.getIdUsuario(), pelicula.getIdPelicula(), puntuacion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre una búsqueda en YouTube con el trailer de la película.
     */
    private void abrirTrailer() {
        try {
            String url = "https://www.youtube.com/results?search_query=" +
                    pelicula.getTituloPelicula().replace(" ", "+") + "+trailer";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Elimina la película de la base de datos.
     */
    private void eliminarPelicula() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Película");
        alert.setHeaderText("¿Borrar '" + pelicula.getTituloPelicula() + "'?");
        alert.setContentText("Esta acción es irreversible y la eliminará de las listas de todos los usuarios.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                peliculaDAO.delete(pelicula.getIdPelicula());
                AlertUtils.info("Película eliminada.");
                Navigation.navigate("peliculas_lista.fxml");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.error("Error al eliminar: " + e.getMessage());
            }
        }
    }
}
