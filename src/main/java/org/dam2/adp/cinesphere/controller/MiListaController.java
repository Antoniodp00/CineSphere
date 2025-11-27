package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaGeneroDAO;
import org.dam2.adp.cinesphere.DAO.GeneroDAO;
import org.dam2.adp.cinesphere.model.Genero;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.dam2.adp.cinesphere.util.Utils.obtenerRutaImagenPorGenero;

/**
 * Controlador para la vista "Mi Lista", que muestra las películas guardadas por el usuario.
 */
public class MiListaController {

    @FXML
    private FlowPane flowPeliculas;
    @FXML
    private ScrollPane scroll;

    // Barra búsqueda / filtros
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

    // Paginación
    @FXML
    private Button btnFirst, btnPrev, btnNext, btnLast;
    @FXML
    private Label lblPage;

    private final MiListaDAO miListaDAO = new MiListaDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private Usuario usuario;

    // Datos en memoria de la lista del usuario
    private List<Pelicula> cacheMisPeliculas; // lista completa del usuario

    // Estado de filtros
    private Integer filtroYear = null;
    private Double filtroRating = null;
    private String filtroGeneroNombre = null; // nombre legible
    private String filtroBusqueda = null; // por título

    // Paginación
    private int page = 1;
    private final int pageSize = 18;
    private int totalPages = 1;

    private static final Logger logger = Logger.getLogger(MiListaController.class.getName());

    /**
     * Inicializa el controlador, configurando los componentes de la interfaz,
     * cargando los datos iniciales y asignando los manejadores de eventos.
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

        // Combos Year y Rating
        for (int y = 2024; y >= 1950; y--) cbYear.getItems().add(y);
        cbRating.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        try {
            generoDAO.findAll().forEach(g -> cbGenero.getItems().add(g.getNombreGenero()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar géneros para el ComboBox", e);
        }

        // Eventos
        btnFiltrar.setOnAction(e -> aplicarFiltros());
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnBuscar.setOnAction(e -> buscar(txtBuscar.getText()));

        btnFirst.setOnAction(e -> {
            if (page > 1) {
                page = 1;
                cargarPagina(page);
            }
        });
        btnPrev.setOnAction(e -> {
            if (page > 1) {
                page--;
                cargarPagina(page);
            }
        });
        btnNext.setOnAction(e -> {
            if (page < totalPages) {
                page++;
                cargarPagina(page);
            }
        });
        btnLast.setOnAction(e -> {
            if (page < totalPages) {
                page = totalPages;
                cargarPagina(page);
            }
        });

        // Cargar datos iniciales desde DAO y pintar página 1
        recargarCacheDesdeDAO();
        actualizarTotalPaginas();
        cargarPagina(1);
        logger.log(Level.INFO, "MiListaController inicializado correctamente.");
    }

    /**
     * Recarga la caché de películas del usuario desde la base de datos.
     */
    private void recargarCacheDesdeDAO() {
        logger.log(Level.INFO, "Recargando caché de películas del usuario desde la base de datos...");
        try {
            cacheMisPeliculas = miListaDAO.findPeliculasByUsuario(usuario.getIdUsuario());
            logger.log(Level.INFO, "Caché recargada con " + cacheMisPeliculas.size() + " películas.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al recargar la caché de películas del usuario", e);
        }
        if (cacheMisPeliculas == null) {
            cacheMisPeliculas = List.of();
        }
    }

    /**
     * Aplica los filtros seleccionados a la lista de películas en memoria.
     * @return una lista de películas que coinciden con los criterios de filtrado.
     */
    private List<Pelicula> aplicarFiltrosEnMemoria() {
        logger.log(Level.INFO, "Aplicando filtros en memoria...");
        return cacheMisPeliculas.stream()
                .filter(p -> filtroYear == null || Objects.equals(p.getYearPelicula(), filtroYear))
                .filter(p -> filtroRating == null || (p.getRatingPelicula() != null && p.getRatingPelicula() >= filtroRating))
                .filter(p -> {
                    if (filtroGeneroNombre == null || filtroGeneroNombre.isBlank()) return true;
                    try {
                        return peliculaGeneroDAO.findByPelicula(p.getIdPelicula())
                                .stream()
                                .anyMatch(g -> filtroGeneroNombre.equalsIgnoreCase(g.getNombreGenero()));
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Error al filtrar por género para la película ID " + p.getIdPelicula(), ex);
                        return true;
                    }
                })
                .filter(p -> {
                    if (filtroBusqueda == null || filtroBusqueda.isBlank()) return true;
                    return p.getTituloPelicula() != null && p.getTituloPelicula().toLowerCase().contains(filtroBusqueda.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el número total de páginas basándose en los filtros aplicados.
     */
    private void actualizarTotalPaginas() {
        int total = aplicarFiltrosEnMemoria().size();
        totalPages = (int) Math.ceil((double) total / pageSize);
        if (totalPages == 0) totalPages = 1;
        logger.log(Level.INFO, "Total de páginas actualizado a: " + totalPages);
    }

    /**
     * Inicia una búsqueda por título de película.
     * @param filtro el texto a buscar en los títulos de las películas.
     */
    private void buscar(String filtro) {
        logger.log(Level.INFO, "Iniciando búsqueda con filtro: '" + filtro + "'");
        this.filtroBusqueda = filtro;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

    /**
     * Carga y muestra una página específica de películas.
     * @param pagina el número de página a cargar.
     */
    private void cargarPagina(int pagina) {
        logger.log(Level.INFO, "Cargando página " + pagina + " de " + totalPages);
        try {
            flowPeliculas.getChildren().clear();

            List<Pelicula> listaFiltrada = aplicarFiltrosEnMemoria();

            int desde = (pagina - 1) * pageSize;
            int hasta = Math.min(desde + pageSize, listaFiltrada.size());
            if (desde < 0) desde = 0;
            if (desde > hasta) desde = hasta;

            List<Pelicula> pageItems = listaFiltrada.subList(desde, hasta);
            for (Pelicula p : pageItems) {
                flowPeliculas.getChildren().add(crearCardPelicula(p));
            }

            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar la página " + pagina, e);
        }
    }

    /**
     * Crea una tarjeta de película (VBox) para mostrar en la lista.
     * @param p la película para la que se creará la tarjeta.
     * @return un VBox que representa la tarjeta de la película.
     */
    private VBox crearCardPelicula(Pelicula p) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        // estilos y tamaños definidos en style.css (.movie-card)

        card.setOnMouseClicked(event -> {
            logger.log(Level.INFO, "Click en la película: " + p.getTituloPelicula() + " (ID: " + p.getIdPelicula() + ")");
            SessionManager.getInstance().set("selectedPeliculaId", p.getIdPelicula());
            Navigation.navigate("peliculas_detalle.fxml");
        });

        // --- LÓGICA DE GÉNEROS E IMAGEN ---
        String textoGeneros = "Sin género";
        String rutaImagen = "/img/noImage.png"; // Imagen por defecto inicial

        try {
            // 1. Obtenemos la lista completa de géneros primero
            List<Genero> listaGeneros = peliculaGeneroDAO.findByPelicula(p.getIdPelicula());

            if (listaGeneros != null && !listaGeneros.isEmpty()) {
                // 2. Usamos el PRIMER género para determinar la imagen de fondo
                String primerGenero = listaGeneros.get(0).getNombreGenero();
                rutaImagen = obtenerRutaImagenPorGenero(primerGenero);

                // 3. Creamos el texto para la etiqueta (ej: "Action, Drama")
                textoGeneros = listaGeneros.stream()
                        .map(g -> g.getNombreGenero())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Sin género");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al obtener géneros para la película ID " + p.getIdPelicula(), e);
        }

        // --- CONFIGURACIÓN DE LA IMAGEN ---
        ImageView img = new ImageView();
        img.setFitWidth(150);
        img.setFitHeight(220);
        img.setPreserveRatio(false);

        try {
            // Intentamos cargar la imagen específica del género
            img.setImage(new Image(getClass().getResource(rutaImagen).toExternalForm()));
        } catch (Exception e) {
            // Si la imagen no existe o la ruta está mal, cargamos la imagen por defecto
            // Esto evita que la aplicación se cierre si falta un archivo .png
            img.setImage(new Image(getClass().getResource("/img/noImage.png").toExternalForm()));
        }

        // --- RESTO DE ETIQUETAS ---
        Label lblTitulo = new Label(p.getTituloPelicula());
        lblTitulo.getStyleClass().add("movie-title");

        Label lblGeneros = new Label(textoGeneros);
        lblGeneros.getStyleClass().add("movie-info");
        lblGeneros.setWrapText(true);

        Label lblYear = new Label(p.getYearPelicula() != null ? p.getYearPelicula().toString() : "—");
        lblYear.getStyleClass().add("movie-info");

        Label lblRating = new Label(
                p.getRatingPelicula() != null ? "★ " + p.getRatingPelicula() : "★ —"
        );
        lblRating.getStyleClass().add("movie-rating");

        card.getChildren().addAll(img, lblTitulo, lblGeneros, lblYear, lblRating);
        return card;
    }

    /**
     * Aplica los filtros seleccionados en los ComboBox y recarga la vista.
     */
    private void aplicarFiltros() {
        logger.log(Level.INFO, "Aplicando filtros...");
        filtroYear = cbYear.getValue();
        filtroRating = cbRating.getValue();
        filtroGeneroNombre = cbGenero.getValue();
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }

    /**
     * Limpia todos los filtros aplicados y recarga la vista.
     */
    private void limpiarFiltros() {
        logger.log(Level.INFO, "Limpiando filtros...");
        cbYear.setValue(null);
        cbRating.setValue(null);
        cbGenero.setValue(null);
        txtBuscar.setText("");
        filtroYear = null;
        filtroRating = null;
        filtroGeneroNombre = null;
        filtroBusqueda = null;
        page = 1;
        actualizarTotalPaginas();
        cargarPagina(page);
    }
}
