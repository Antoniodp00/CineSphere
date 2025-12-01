package org.dam2.adp.cinesphere.component;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.dam2.adp.cinesphere.model.Pelicula;
import org.dam2.adp.cinesphere.util.Navigation;
import org.dam2.adp.cinesphere.util.SessionManager;
import org.dam2.adp.cinesphere.util.Utils;

import java.util.stream.Collectors;

/**
 * Componente de UI estandarizado para mostrar una película en una tarjeta.
 * Extiende de VBox y encapsula todo el estilo y layout.
 */
public class MovieCard extends VBox {

    private static final double CARD_WIDTH = 180;
    private static final double IMAGE_HEIGHT = 250;

    /**
     * Constructor del componente MovieCard.
     * @param pelicula La película a mostrar en la tarjeta.
     */
    public MovieCard(Pelicula pelicula) {
        super();

        getStyleClass().addAll("movie-card", Styles.ELEVATED_1, Styles.ROUNDED);
        setPadding(new Insets(10));
        setSpacing(8);
        setAlignment(Pos.TOP_LEFT);
        setPrefWidth(CARD_WIDTH);

        ImageView imageView = createImageView(pelicula);
        
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(CARD_WIDTH, IMAGE_HEIGHT);
        
        Rectangle clip = new Rectangle(CARD_WIDTH, IMAGE_HEIGHT);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageContainer.setClip(clip);
        
        Label titleLabel = new Label(pelicula.getTituloPelicula());
        titleLabel.getStyleClass().add(Styles.TITLE_4);
        titleLabel.setWrapText(true);
        titleLabel.setTooltip(new Tooltip(pelicula.getTituloPelicula()));
        titleLabel.setMaxWidth(CARD_WIDTH);
        titleLabel.setPrefHeight(40);

        String generos = pelicula.getGeneros().stream()
                .map(g -> g.getNombreGenero())
                .limit(1)
                .collect(Collectors.joining());

        Label yearLabel = new Label(String.valueOf(pelicula.getYearPelicula()));
        yearLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        Label genreLabel = new Label(generos);
        genreLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox metadataBox = new HBox(yearLabel, spacer, genreLabel);
        metadataBox.setAlignment(Pos.CENTER_LEFT);

        RatingDisplay ratingDisplay = new RatingDisplay(pelicula.getRatingPelicula());

        getChildren().addAll(imageContainer, titleLabel, metadataBox, ratingDisplay);

        setOnMouseClicked(event -> {
            SessionManager.getInstance().set("selectedPeliculaId", pelicula.getIdPelicula());
            Navigation.navigate("peliculas_detalle.fxml");
        });
    }

    /**
     * Crea y configura el ImageView para la portada de la película.
     * @param pelicula La película de la que se obtendrá la imagen.
     * @return Un ImageView configurado.
     */
    private ImageView createImageView(Pelicula pelicula) {
        String rutaImagen = Utils.obtenerRutaImagenPorGenero(
            pelicula.getGeneros().isEmpty() ? "" : pelicula.getGeneros().get(0).getNombreGenero()
        );

        Image image = new Image(
            getClass().getResource(rutaImagen).toExternalForm(), 
            CARD_WIDTH, 0,
            true, true, true
        );

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        
        return imageView;
    }
}
