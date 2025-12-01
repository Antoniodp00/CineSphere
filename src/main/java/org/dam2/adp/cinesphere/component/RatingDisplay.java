package org.dam2.adp.cinesphere.component;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Componente visual para mostrar una calificación con estrellas y un valor numérico.
 */
public class RatingDisplay extends HBox {

    /**
     * Constructor del componente RatingDisplay.
     * @param rating El rating numérico (de 0 a 10) a mostrar.
     */
    public RatingDisplay(Double rating) {
        super(2);
        initialize(rating);
    }

    /**
     * Inicializa y construye el componente visual de rating.
     * @param rating El rating numérico a visualizar.
     */
    private void initialize(Double rating) {
        getStyleClass().add("rating");
        setAlignment(Pos.CENTER_LEFT);

        int numStars = 5;
        int filledStars = (rating != null) ? (int) (rating / 2) : 0;

        for (int i = 0; i < numStars; i++) {
            Button star = new Button("★");
            star.getStyleClass().addAll("button", Styles.FLAT);
            star.setDisable(true);
            if (i < filledStars) {
                star.getStyleClass().add("strong");
            }
            getChildren().add(star);
        }

        if (rating != null) {
            Label ratingValue = new Label(String.format("%.1f", rating));
            ratingValue.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
            getChildren().add(ratingValue);
        }
    }
}
