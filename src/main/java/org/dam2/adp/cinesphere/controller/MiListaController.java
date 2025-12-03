package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import org.dam2.adp.cinesphere.DAO.GeneroDAO;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.component.MovieCard;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista "Mi Lista", que muestra las películas guardadas por el usuario.
 */
public class MiListaController {

    @FXML
    private TilePane tilePeliculas;
    @FXML
    private ScrollPane scroll;

    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnBuscar;
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
    private Button btnFirst, btnPrev, btnNext, btnLast;
    @FXML
    private Label lblPage;

    private final MiListaDAO miListaDAO = new MiListaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private Usuario usuario;

    private Integer filtroYear = null;
    private Double filtroRating = null;
    private Integer filtroGeneroId = null;
    private String filtroBusqueda = null;

    private int page = 1;
    private int pageSize = 18;
    private int totalPages = 1;

    private static final Logger logger = Logger.getLogger(MiListaController.class.getName());

    /**
     * Inicializa el controlador, configurando los listeners y cargando la primera página de películas.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando MiListaController...");
        usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) {
            logger.log(Level.WARNING, "No hay usuario en sesión. Redirigiendo al login.");
            Navigation.switchScene("login.fxml");
            return;
        }

        scroll.widthProperty().addListener((obs, oldVal, newVal) -> {
            ajustarTamanoPagina(newVal.doubleValue());
            actualizarTotalPaginas();
            cargarPagina(page);
        });

        for (int y = 2024; y >= 1950; y--) cbYear.getItems().add(y);
        cbRating.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        try {
            generoDAO.findAll().forEach(g -> cbGenero.getItems().add(g.getNombreGenero()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar géneros para el ComboBox", e);
        }

        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnBuscar.setOnAction(e -> aplicarFiltros());

        btnFirst.setOnAction(e -> cambiarPagina(1));
        btnPrev.setOnAction(e -> cambiarPagina(page - 1));
        btnNext.setOnAction(e -> cambiarPagina(page + 1));
        btnLast.setOnAction(e -> cambiarPagina(totalPages));

        ajustarTamanoPagina(scroll.getWidth());
        actualizarTotalPaginas();
        cargarPagina(1);
        logger.log(Level.INFO, "MiListaController inicializado correctamente.");
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
    private void ajustarTamanoPagina(double scrollWidth) {
        if (scrollWidth <= 0) {
            pageSize = 18;
            return;
        }
        int cardWidth = 200;
        int numColumns = (int) Math.floor(scrollWidth / cardWidth);
        if (numColumns == 0) numColumns = 1;
        pageSize = numColumns * 3;
    }

    /**
     * Calcula y actualiza el número total de páginas basándose en los filtros actuales.
     */
    private void actualizarTotalPaginas() {
        try {
            int total = miListaDAO.countPeliculas(usuario.getIdUsuario(), filtroYear, filtroRating, filtroGeneroId, filtroBusqueda);
            totalPages = (int) Math.ceil((double) total / pageSize);
            if (totalPages == 0) totalPages = 1;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar el total de páginas", e);
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
            List<Pelicula> lista = miListaDAO.findFiltered(usuario.getIdUsuario(), filtroYear, filtroRating, filtroGeneroId, filtroBusqueda, pagina, pageSize);

            for (Pelicula p : lista) {
                MovieCard card = new MovieCard(p);
                card.setOnMouseClicked(event -> {
                    SessionManager.getInstance().set("selectedPeliculaId", p.getIdPelicula());
                    Navigation.navigate("peliculas_detalle.fxml");
                });
                tilePeliculas.getChildren().add(card);
            }
            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar la página " + pagina, e);
        }
    }

    /**
     * Aplica los filtros seleccionados en los ComboBox y recarga la vista.
     */
    private void aplicarFiltros() {
        filtroYear = cbYear.getValue();
        filtroRating = cbRating.getValue();
        filtroBusqueda = txtBuscar.getText().trim();

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
        txtBuscar.setText("");
        filtroYear = null;
        filtroRating = null;
        filtroGeneroId = null;
        filtroBusqueda = null;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }
}
