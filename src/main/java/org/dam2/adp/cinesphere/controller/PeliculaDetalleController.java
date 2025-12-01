package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaDAO;
import org.dam2.adp.cinesphere.component.Chip;
import org.dam2.adp.cinesphere.component.RatingDisplay;
import org.dam2.adp.cinesphere.model.MiLista;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.model.PeliculaEstado;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.AlertUtils;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.dam2.adp.cinesphere.util.Utils;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controlador encargado de la gestión y visualización de los detalles de una película.
 * Permite la interacción del usuario con su lista personal, valoración y estado de visualización.
 */
public class PeliculaDetalleController {

    @FXML private Button btnEliminarPelicula;
    @FXML private ImageView imgPoster;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblClasificacion;
    @FXML private Label lblSinopsis;
    @FXML private FlowPane flowGeneros;
    @FXML private FlowPane flowDirectores;
    @FXML private FlowPane flowActores;
    @FXML private Button btnMiLista;
    @FXML private Button btnTrailer;
    @FXML private ComboBox<PeliculaEstado> cbEstado;
    @FXML private ComboBox<Integer> cbPuntuacion;
    @FXML private HBox ratingContainer;

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final MiListaDAO miListaDAO = new MiListaDAO();

    private Pelicula pelicula;
    private Usuario usuario;

    private static final Logger logger = Logger.getLogger(PeliculaDetalleController.class.getName());

    /**
     * Inicializa el controlador obteniendo la sesión actual y cargando los datos de la película seleccionada.
     * Configura la visibilidad de los controles administrativos si corresponde.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando PeliculaDetalleController...");
        usuario = SessionManager.getInstance().getUsuarioActual();
        Integer idPelicula = (Integer) SessionManager.getInstance().get("selectedPeliculaId");

        if (idPelicula == null) {
            logger.log(Level.WARNING, "No se encontró ID de película en sesión. Navegando a la lista.");
            Navigation.navigate("peliculas_lista.fxml");
            return;
        }

        flowGeneros.setHgap(10);
        flowGeneros.setVgap(10);
        flowDirectores.setHgap(10);
        flowDirectores.setVgap(10);
        flowActores.setHgap(10);
        flowActores.setVgap(10);

        setupComboBoxes();
        cargarDatos(idPelicula);

        if (usuario != null && usuario.isAdmin()) {
            btnEliminarPelicula.setVisible(true);
            btnEliminarPelicula.setOnAction(e -> eliminarPelicula());
        } else {
            btnEliminarPelicula.setVisible(false);
            btnEliminarPelicula.setManaged(false);
        }
        logger.log(Level.INFO, "PeliculaDetalleController inicializado.");
    }

    /**
     * Configura los valores y listeners de los componentes de selección.
     */
    private void setupComboBoxes() {
        cbEstado.getItems().setAll(PeliculaEstado.values());
        cbPuntuacion.getItems().setAll(
                IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList())
        );

        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cambiarEstado(newVal);
                gestionarAccesibilidadPuntuacion(newVal);

                if (newVal != PeliculaEstado.TERMINADA) {
                    cbPuntuacion.setValue(null);
                }
            }
        });

        cbPuntuacion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cambiarPuntuacion(newVal);
        });
    }

    /**
     * Controla la habilitación del selector de puntuación basándose en el estado de la película.
     */
    private void gestionarAccesibilidadPuntuacion(PeliculaEstado estado) {
        boolean esVista = (estado == PeliculaEstado.TERMINADA);
        cbPuntuacion.setDisable(!esVista);
        cbPuntuacion.setPromptText(esVista ? "Nota" : "Marca como terminada para puntuar");
    }

    /**
     * Recupera la información de la película desde la base de datos y actualiza la interfaz gráfica.
     */
    private void cargarDatos(int idPelicula) {
        logger.log(Level.INFO, "Cargando datos para la película ID: " + idPelicula);
        try {
            pelicula = peliculaDAO.findByIdEager(idPelicula);
            if (pelicula == null) {
                logger.log(Level.SEVERE, "No se encontró la película con ID: " + idPelicula);
                return;
            }

            lblTitulo.setText(pelicula.getTituloPelicula());
            lblSubtitulo.setText(pelicula.getYearPelicula() + " • " + pelicula.getNombreClasificacion());
            lblClasificacion.setText(pelicula.getNombreClasificacion());
            lblSinopsis.setText("Sinopsis no disponible aún.");

            if (ratingContainer != null) {
                ratingContainer.getChildren().clear();
                ratingContainer.getChildren().add(new RatingDisplay(pelicula.getRatingPelicula()));
            }

            String rutaImagen = pelicula.getGeneros().stream().findFirst()
                    .map(g -> Utils.obtenerRutaImagenPorGenero(g.getNombreGenero()))
                    .orElse("/img/noImage.png");

            try {
                imgPoster.setImage(new Image(getClass().getResource(rutaImagen).toExternalForm()));
            } catch (Exception e) {
                logger.log(Level.WARNING, "No se pudo cargar la imagen: " + rutaImagen);
                imgPoster.setImage(new Image(getClass().getResource("/img/noImage.png").toExternalForm()));
            }

            flowGeneros.getChildren().setAll(
                pelicula.getGeneros().stream().map(g -> new Chip(g.getNombreGenero())).collect(Collectors.toList())
            );
            flowDirectores.getChildren().setAll(
                pelicula.getDirectores().stream().map(d -> new Chip(d.getNombreDirector())).collect(Collectors.toList())
            );
            flowActores.getChildren().setAll(
                pelicula.getActores().stream().map(a -> new Chip(a.getNombreActor())).collect(Collectors.toList())
            );

            actualizarEstadoMiLista();
            btnMiLista.setOnAction(e -> toggleMiLista());
            btnTrailer.setOnAction(e -> abrirTrailer());

            logger.log(Level.INFO, "Datos de la película '" + pelicula.getTituloPelicula() + "' cargados correctamente.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar los datos de la película", e);
        }
    }

    /**
     * Sincroniza los controles de UI con el estado actual de la película en la lista del usuario.
     */
    private void actualizarEstadoMiLista() throws Exception {
        MiLista ml = miListaDAO.findAll(usuario.getIdUsuario(), pelicula.getIdPelicula());
        boolean enLista = ml != null;

        btnMiLista.setText(enLista ? "En tu lista" : "Añadir a mi lista");
        cbEstado.setDisable(!enLista);
        cbPuntuacion.setDisable(!enLista || (enLista && ml.getEstado() != PeliculaEstado.TERMINADA));

        if (enLista) {
            cbEstado.setValue(ml.getEstado());
            cbPuntuacion.setValue(ml.getPuntuacion());
        } else {
            cbEstado.setValue(null);
            cbPuntuacion.setValue(null);
        }
        logger.log(Level.INFO, "Estado de 'Mi Lista' actualizado. Película en lista: " + enLista);
    }

    /**
     * Alterna la presencia de la película actual en la lista personal del usuario.
     */
    private void toggleMiLista() {
        try {
            MiLista ml = miListaDAO.findAll(usuario.getIdUsuario(), pelicula.getIdPelicula());
            if (ml == null) {
                miListaDAO.insert(new MiLista(pelicula, usuario, PeliculaEstado.PENDIENTE, null, null, LocalDateTime.now()));
                logger.log(Level.INFO, "Película '" + pelicula.getTituloPelicula() + "' añadida a Mi Lista.");
            } else {
                miListaDAO.delete(usuario.getIdUsuario(), pelicula.getIdPelicula());
                logger.log(Level.INFO, "Película '" + pelicula.getTituloPelicula() + "' eliminada de Mi Lista.");
            }
            actualizarEstadoMiLista();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al añadir/eliminar de Mi Lista", e);
        }
    }

    /**
     * Actualiza el estado de visualización de la película en la base de datos.
     */
    private void cambiarEstado(PeliculaEstado estado) {
        try {
            miListaDAO.updateEstado(usuario.getIdUsuario(), pelicula.getIdPelicula(), estado);
            logger.log(Level.INFO, "Estado de la película '" + pelicula.getTituloPelicula() + "' cambiado a: " + estado);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar el estado de la película", e);
        }
    }

    /**
     * Actualiza la puntuación personal de la película en la base de datos.
     */
    private void cambiarPuntuacion(int puntuacion) {
        try {
            miListaDAO.updatePuntuacion(usuario.getIdUsuario(), pelicula.getIdPelicula(), puntuacion);
            logger.log(Level.INFO, "Puntuación de la película '" + pelicula.getTituloPelicula() + "' cambiada a: " + puntuacion);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar la puntuación de la película", e);
        }
    }

    /**
     * Inicia el navegador predeterminado del sistema para buscar el trailer de la película en YouTube.
     */
    private void abrirTrailer() {
        try {
            String url = "https://www.youtube.com/results?search_query=" + pelicula.getTituloPelicula().replace(" ", "+") + "+trailer";
            Desktop.getDesktop().browse(new URI(url));
            logger.log(Level.INFO, "Abriendo trailer para: " + pelicula.getTituloPelicula());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al abrir el trailer", ex);
        }
    }

    /**
     * Ejecuta el proceso de eliminación física de la película de la base de datos.
     */
    private void eliminarPelicula() {
        if (AlertUtils.confirmation("Eliminar Película", "¿Borrar '" + pelicula.getTituloPelicula() + "'?", "Esta acción es irreversible y la eliminará de las listas de todos los usuarios.")) {
            try {
                peliculaDAO.delete(pelicula.getIdPelicula());
                AlertUtils.info("Película eliminada.");
                logger.log(Level.INFO, "Película '" + pelicula.getTituloPelicula() + "' eliminada por un administrador.");
                Navigation.navigate("peliculas_lista.fxml");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error al eliminar la película", e);
                AlertUtils.error("Error al eliminar: " + e.getMessage());
            }
        }
    }
}
