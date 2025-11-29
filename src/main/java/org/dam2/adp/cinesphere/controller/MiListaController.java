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

    @FXML private TilePane tilePeliculas;
    @FXML private ScrollPane scroll;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private ComboBox<Integer> cbYear;
    @FXML private ComboBox<Double> cbRating;
    @FXML private ComboBox<String> cbGenero;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiar;

    @FXML private Button btnFirst, btnPrev, btnNext, btnLast;
    @FXML private Label lblPage;

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
            ajustarPageSize(newVal.doubleValue());
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
        btnBuscar.setOnAction(e -> buscar(txtBuscar.getText()));

        btnFirst.setOnAction(e -> cambiarPagina(1));
        btnPrev.setOnAction(e -> cambiarPagina(page - 1));
        btnNext.setOnAction(e -> cambiarPagina(page + 1));
        btnLast.setOnAction(e -> cambiarPagina(totalPages));

        ajustarPageSize(scroll.getWidth());
        actualizarTotalPaginas();
        cargarPagina(1);
        logger.log(Level.INFO, "MiListaController inicializado correctamente.");
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
        int cardWidth = 200; // Ancho de la tarjeta + hgap
        int numColumns = (int) Math.floor(scrollWidth / cardWidth);
        if (numColumns == 0) numColumns = 1;
        pageSize = numColumns * 3;
    }

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

    private void buscar(String filtro) {
        this.filtroBusqueda = filtro;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

    private void cargarPagina(int pagina) {
        try {
            tilePeliculas.getChildren().clear();
            List<Pelicula> lista = miListaDAO.findFiltered(usuario.getIdUsuario(), filtroYear, filtroRating, filtroGeneroId, filtroBusqueda, pagina, pageSize);

            for (Pelicula p : lista) {
                tilePeliculas.getChildren().add(new MovieCard(p));
            }
            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar la página " + pagina, e);
        }
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
        } else {
            filtroGeneroId = null;
        }
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

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
