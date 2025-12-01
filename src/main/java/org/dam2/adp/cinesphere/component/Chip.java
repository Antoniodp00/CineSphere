package org.dam2.adp.cinesphere.component;

import atlantafx.base.theme.Styles;
import javafx.scene.control.Label;

/**
 * Componente visual para mostrar etiquetas (chips) con un estilo consistente.
 * Utilizado para géneros, actores, directores, etc.
 */
public class Chip extends Label {

    public Chip(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().addAll("chip", Styles.TEXT_SMALL);
        // Si necesitas que el texto sea visible, asegúrate de que no haya un GRAPHIC_ONLY
        // Si el estilo CSS global tiene GRAPHIC_ONLY, este componente lo heredará.
        // Si quieres forzar el texto, podrías añadir:
        // setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
