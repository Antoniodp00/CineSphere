package org.dam2.adp.cinesphere.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.DAO.PeliculaGeneroDAO;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.util.List;

public class MiListaController {

    @FXML
    private FlowPane flowPeliculas;

    private final MiListaDAO miListaDAO = new MiListaDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private Usuario usuario;

    @FXML
    private void initialize() {
        usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) {
            Navigation.switchScene("login.fxml");
            return;
        }
        cargarMiLista();
    }

    private void cargarMiLista() {
        flowPeliculas.getChildren().clear();
        try {
            List<Pelicula> misPeliculas = miListaDAO.findPeliculasByUsuario(usuario.getIdUsuario());
            for (Pelicula p : misPeliculas) {
                flowPeliculas.getChildren().add(crearCardPelicula(p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox crearCardPelicula(Pelicula p) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        // estilos y tamaños definidos en style.css (.movie-card)

        card.setOnMouseClicked(event -> {
            SessionManager.getInstance().set("selectedPeliculaId", p.getIdPelicula());
            Navigation.navigate("peliculas_detalle.fxml");
        });

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
}
