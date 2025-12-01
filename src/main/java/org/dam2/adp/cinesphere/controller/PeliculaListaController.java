package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import org.dam2.adp.cinesphere.DAO.GeneroDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaDAO;
import org.dam2.adp.cinesphere.component.MovieCard;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista de lista de películas.
 */
public class PeliculaListaController {

    @FXML
    private ComboBox<Integer> cbYear;
    @FXML
    private ComboBox<Double> cbRating;
    @FXML
    private ComboBox<String> cbGenero;

    @FXML
    private Button btnFiltrar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TilePane tilePeliculas;
    @FXML
    private ScrollPane scroll;

    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnFirst, btnPrev, btnNext, btnLast;
    @FXML
    private Label lblPage;

    private Integer filtroYear = null;
    private Double filtroRating = null;
    private Integer filtroGeneroId = null;
    private String filtroTitulo = null;

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();

    private int page = 1;
    private int pageSize = 18;
    private int totalPages = 1;

    private MovieCard selectedMovieCard = null;

    private static final Logger logger = Logger.getLogger(PeliculaListaController.class.getName());

    /**
     * Inicializa el controlador, configurando los listeners y cargando la primera página de películas.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando PeliculaListaController...");

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

        for (int y = 2024; y >= 1950; y--) {
            cbYear.getItems().add(y);
        }
        cbRating.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);

        try {
            generoDAO.findAll().forEach(g -> cbGenero.getItems().add(g.getNombreGenero()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar géneros", e);
        }

        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnBuscar.setOnAction(e -> aplicarFiltros());

        btnFirst.setOnAction(e -> cambiarPagina(1));
        btnPrev.setOnAction(e -> cambiarPagina(page - 1));
        btnNext.setOnAction(e -> cambiarPagina(page + 1));
        btnLast.setOnAction(e -> cambiarPagina(totalPages));

        ajustarPageSize(scroll.getWidth());
        actualizarTotalPaginas();
        cargarPagina(1);

        logger.log(Level.INFO, "PeliculaListaController inicializado.");
    }

    /**
     * Cambia a una página específica si está dentro de los límites.
     *
     * @param nuevaPagina El número de la página a la que se quiere navegar.
     */
    private void cambiarPagina(int nuevaPagina) {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPages) {
            page = nuevaPagina;
            cargarPagina(page);
        }
    }

    /**
     * Ajusta el número de películas por página basándose en el ancho del ScrollPane.
     *
     * @param scrollWidth El ancho actual del ScrollPane.
     */
    private void ajustarPageSize(double scrollWidth) {
        if (scrollWidth <= 0) {
            pageSize = 18;
            return;
        }
        int cardWidth = 200;
        int numColumns = (int) Math.floor(scrollWidth / cardWidth);
        if (numColumns == 0) {
            numColumns = 1;
        }
        pageSize = numColumns * 3;
    }

    /**
     * Calcula y actualiza el número total de páginas basándose en los filtros actuales.
     */
    private void actualizarTotalPaginas() {
        try {
            int total = peliculaDAO.countPeliculas(filtroYear, filtroRating, filtroGeneroId, filtroTitulo);
            totalPages = (int) Math.ceil((double) total / pageSize);
            if (totalPages == 0) {
                totalPages = 1;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error contando páginas", e);
            totalPages = 1;
        }
    }

    /**
     * Carga y muestra las películas de una página específica, aplicando los filtros actuales.
     *
     * @param pagina El número de página a cargar.
     */
    private void cargarPagina(int pagina) {
        try {
            tilePeliculas.getChildren().clear();
            selectedMovieCard = null;
            List<Pelicula> lista;

            boolean hasFilters = filtroYear != null || filtroRating != null || filtroGeneroId != null || (filtroTitulo != null && !filtroTitulo.isEmpty());

            if (hasFilters) {
                lista = peliculaDAO.findFiltered(filtroYear, filtroRating, filtroGeneroId, filtroTitulo, pagina, pageSize);
            } else {
                lista = peliculaDAO.findPage(pagina, pageSize);
            }

            for (Pelicula p : lista) {
                MovieCard card = new MovieCard(p);
                card.setOnMouseClicked(event -> {
                    if (selectedMovieCard != null) {
                        selectedMovieCard.getStyleClass().remove("neon-glow");
                    }
                    card.getStyleClass().add("neon-glow");
                    selectedMovieCard = card;
                    SessionManager.getInstance().set("selectedPeliculaId", p.getIdPelicula());
                    Navigation.navigate("peliculas_detalle.fxml");
                });
                tilePeliculas.getChildren().add(card);
            }
            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar página", e);
        }
    }

    /**
     * Aplica los filtros seleccionados en los ComboBox y recarga la vista.
     */
    private void aplicarFiltros() {
        filtroYear = cbYear.getValue();
        filtroRating = cbRating.getValue();
        filtroTitulo = txtBuscar.getText().trim();

        if (cbGenero.getValue() != null) {
            try {
                filtroGeneroId = generoDAO.findByName(cbGenero.getValue()).getIdGenero();
            } catch (Exception e) {
                filtroGeneroId = null;
            }
        } else {
            filtroGeneroId = null;
        }

        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

    /**
     * Limpia todos los filtros aplicados y recarga la vista a su estado inicial.
     */
    private void limpiarFiltros() {
        cbYear.setValue(null);
        cbRating.setValue(null);
        cbGenero.setValue(null);
        txtBuscar.clear();
        filtroYear = null;
        filtroRating = null;
        filtroGeneroId = null;
        filtroTitulo = null;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }
}
