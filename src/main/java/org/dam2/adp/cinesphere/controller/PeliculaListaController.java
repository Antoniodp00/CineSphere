package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.dam2.adp.cinesphere.DAO.GeneroDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaGeneroDAO;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

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
    private FlowPane flowPeliculas;
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

    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();

    private int page = 1;
    private final int pageSize = 18;
    private int totalPages = 1;

    @FXML
    private void initialize() {
        // Rellenar ComboBoxes
        for (int y = 2024; y >= 1950; y--) cbYear.getItems().add(y);
        cbRating.getItems().addAll(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        try {
            generoDAO.findAll().forEach(g -> cbGenero.getItems().add(g.getNombreGenero()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Asignar eventos
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



        // Carga inicial
        actualizarTotalPaginas();
        cargarPagina(1);
    }

    private void actualizarTotalPaginas() {
        try {
            int total = peliculaDAO.countPeliculas(filtroYear, filtroRating, filtroGeneroId);
            totalPages = (int) Math.ceil((double) total / pageSize);
            if (totalPages == 0) totalPages = 1;
        } catch (SQLException e) {
            e.printStackTrace();
            totalPages = 1;
        }
    }

    private void buscar(String filtro) {
        try {
            List<Pelicula> todas = peliculaDAO.findAllLazy();

            List<Pelicula> filtradas = todas.stream()
                    .filter(p -> p.getTituloPelicula().toLowerCase().contains(filtro.toLowerCase()))
                    .toList();

            flowPeliculas.getChildren().clear();
            filtradas.forEach(p -> flowPeliculas.getChildren().add(crearCardPelicula(p)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarPagina(int pagina) {
        try {
            flowPeliculas.getChildren().clear();

            List<Pelicula> lista;

            if (filtroYear != null || filtroRating != null || filtroGeneroId != null) {
                lista = peliculaDAO.findFiltered(
                        filtroYear,
                        filtroRating,
                        filtroGeneroId,
                        pagina,
                        pageSize
                );
            } else {
                lista = peliculaDAO.findPage(pagina, pageSize);
            }

            for (Pelicula p : lista) {
                flowPeliculas.getChildren().add(crearCardPelicula(p));
            }

            lblPage.setText(pagina + " / " + totalPages);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox crearCardPelicula(Pelicula p) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        card.setOnMouseClicked(event -> verDetalle(p.getIdPelicula()));

        ImageView img = new ImageView();
        img.setFitWidth(150);
        img.setFitHeight(220);
        img.setPreserveRatio(false);
        img.setImage(new Image(getClass().getResource("/img/noImage.png").toExternalForm()));

        Label lblTitulo = new Label(p.getTituloPelicula());
        lblTitulo.getStyleClass().add("movie-title");

        String generos = "";
        try {
            generos = peliculaGeneroDAO.findByPelicula(p.getIdPelicula())
                    .stream()
                    .map(g -> g.getNombreGenero())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Sin género");
        } catch (Exception e) {
            generos = "Sin género";
        }

        Label lblGeneros = new Label(generos);
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
