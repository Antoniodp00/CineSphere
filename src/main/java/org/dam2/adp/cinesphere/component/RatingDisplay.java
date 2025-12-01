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

    public RatingDisplay(Double rating) {
        super(2); // Espaciado de 2px entre elementos
        initialize(rating);
    }

    private void initialize(Double rating) {
        getStyleClass().add("rating");
        setAlignment(Pos.CENTER_LEFT);

        int numStars = 5;
        int filledStars = (rating != null) ? (int) (rating / 2) : 0; // Convertir rating 0-10 a 0-5 estrellas

        for (int i = 0; i < numStars; i++) {
            Button star = new Button("★");
            star.getStyleClass().addAll("button", Styles.FLAT);
            star.setDisable(true); // Las estrellas no son interactivas aquí
            if (i < filledStars) {
                star.getStyleClass().add("strong");
            }
            getChildren().add(star);
        }

        // Mostrar el rating numérico al lado
        if (rating != null) {
            Label ratingValue = new Label(String.format("%.1f", rating));
            ratingValue.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
            getChildren().add(ratingValue);
        }
    }
}
