package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.dam2.adp.cinesphere.DAO.GeneroDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaDAO;
import org.dam2.adp.cinesphere.model.Genero;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.dam2.adp.cinesphere.util.Utils;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de lista de películas.
 */
public class PeliculaListaController {

    @FXML private ComboBox<Integer> cbYear;
    @FXML private ComboBox<Double> cbRating;
    @FXML private ComboBox<String> cbGenero;

    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiar;

    @FXML private FlowPane flowPeliculas;
    @FXML private ScrollPane scroll;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;

    @FXML private Button btnFirst, btnPrev, btnNext, btnLast;
    @FXML private Label lblPage;

    private Integer filtroYear = null;
    private Double filtroRating = null;
    private Integer filtroGeneroId = null;

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    // PeliculaGeneroDAO eliminado porque ya no se usa aquí (optimización)

    private int page = 1;
    private int pageSize = 18; // Default
    private int totalPages = 1;

    private static final Logger logger = Logger.getLogger(PeliculaListaController.class.getName());

    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando PeliculaListaController...");

        // Listener responsive
        scroll.widthProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal.doubleValue() > 0) {
                int oldSize = pageSize;
                ajustarPageSize(newVal.doubleValue());

                if (oldSize != pageSize) {
                    actualizarTotalPaginas();
                    cargarPagina(page);
                }
            }
        });

        // Rellenar combos
        for (int y = 2024; y >= 1950; y--) cbYear.getItems().add(y);
        cbRating.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);

        try {
            generoDAO.findAll().forEach(g -> cbGenero.getItems().add(g.getNombreGenero()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar géneros", e);
        }

        // Listeners botones
        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnBuscar.setOnAction(e -> buscar(txtBuscar.getText()));

        btnFirst.setOnAction(e -> cambiarPagina(1));
        btnPrev.setOnAction(e -> cambiarPagina(page - 1));
        btnNext.setOnAction(e -> cambiarPagina(page + 1));
        btnLast.setOnAction(e -> cambiarPagina(totalPages));

        // Carga inicial
        ajustarPageSize(scroll.getWidth());
        actualizarTotalPaginas();
        cargarPagina(1);

        logger.log(Level.INFO, "PeliculaListaController inicializado.");
    }

    private void cambiarPagina(int nuevaPagina) {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPages) {
            page = nuevaPagina;
            cargarPagina(page);
        }
    }

    private void ajustarPageSize(double scrollWidth) {
        if (scrollWidth <= 0) {
            pageSize = 18;
            return;
        }

        int cardWidth = 190;
        int numColumns = (int) Math.floor(scrollWidth / cardWidth);
        if (numColumns == 0) numColumns = 1;

        pageSize = numColumns * 3;

    }

    private void actualizarTotalPaginas() {
        try {
            int total = peliculaDAO.countPeliculas(filtroYear, filtroRating, filtroGeneroId);
            totalPages = (int) Math.ceil((double) total / pageSize);
            if (totalPages == 0) totalPages = 1;
            // logger.log(Level.INFO, "Total páginas: " + totalPages);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error contando páginas", e);
            totalPages = 1;
        }
    }

    private void cargarPagina(int pagina) {
        try {
            flowPeliculas.getChildren().clear();
            List<Pelicula> lista;

            if (filtroYear != null || filtroRating != null || filtroGeneroId != null) {
                lista = peliculaDAO.findFiltered(filtroYear, filtroRating, filtroGeneroId, pagina, pageSize);
            } else {
                lista = peliculaDAO.findPage(pagina, pageSize);
            }

            for (Pelicula p : lista) {
                flowPeliculas.getChildren().add(crearCardPelicula(p));
            }
            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar página", e);
        }
    }

    private void buscar(String filtro) {
        try {
            // Nota: Idealmente esto también debería ser paginado en BBDD
            List<Pelicula> todas = peliculaDAO.findAllLazy();
            List<Pelicula> filtradas = todas.stream()
                    .filter(p -> p.getTituloPelicula().toLowerCase().contains(filtro.toLowerCase()))
                    .toList();

            flowPeliculas.getChildren().clear();
            filtradas.forEach(p -> flowPeliculas.getChildren().add(crearCardPelicula(p)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error buscando", e);
        }
    }

    private VBox crearCardPelicula(Pelicula p) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        card.setOnMouseClicked(event -> verDetalle(p.getIdPelicula()));

        String textoGeneros = "Sin género";
        String rutaImagen = "/img/noImage.png";

        if (p.getGeneros() != null && !p.getGeneros().isEmpty()) {
            textoGeneros = p.getGeneros().stream()
                    .map(Genero::getNombreGenero)
                    .limit(2)
                    .collect(Collectors.joining(", "));

            // Usamos el primer género para la imagen placeholder
            rutaImagen = Utils.obtenerRutaImagenPorGenero(p.getGeneros().get(0).getNombreGenero());
        }

        ImageView img = new ImageView();
        img.setFitWidth(150);
        img.setFitHeight(220);
        img.setPreserveRatio(false);

        try {
            // Background loading: TRUE
            String fullPath = getClass().getResource(rutaImagen).toExternalForm();
            img.setImage(new Image(fullPath, 0, 0, true, true, true));
        } catch (Exception e) {
            img.setImage(new Image(getClass().getResource("/img/noImage.png").toExternalForm()));
        }

        Label lblTitulo = new Label(p.getTituloPelicula());
        lblTitulo.getStyleClass().add("movie-title");
        lblTitulo.setTooltip(new Tooltip(p.getTituloPelicula()));

        Label lblGeneros = new Label(textoGeneros);
        lblGeneros.getStyleClass().add("movie-info");

        Label lblYear = new Label(p.getYearPelicula() != null ? p.getYearPelicula().toString() : "—");
        lblYear.getStyleClass().add("movie-info");

        Label lblRating = new Label(p.getRatingPelicula() != null ? "★ " + p.getRatingPelicula() : "★ —");
        lblRating.getStyleClass().add("movie-rating");

        if (p.getRatingPelicula() != null && p.getRatingPelicula() >= 8.0) {
            lblRating.setStyle("-fx-text-fill: #f1c40f;");
        }

        card.getChildren().addAll(img, lblTitulo, lblGeneros, lblYear, lblRating);
        return card;
    }

    private void verDetalle(int idPelicula) {
        SessionManager.getInstance().set("selectedPeliculaId", idPelicula);
        Navigation.navigate("peliculas_detalle.fxml");
    }

    private void aplicarFiltros() {
        filtroYear = cbYear.getValue();
        filtroRating = cbRating.getValue();

        if (cbGenero.getValue() != null) {
            try {
                filtroGeneroId = generoDAO.findByName(cbGenero.getValue()).getIdGenero();
            } catch (Exception e) {
                filtroGeneroId = null;
            }
        } else filtroGeneroId = null;

        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

    private void limpiarFiltros() {
        cbYear.setValue(null);
        cbRating.setValue(null);
        cbGenero.setValue(null);
        filtroYear = null;
        filtroRating = null;
        filtroGeneroId = null;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }
}