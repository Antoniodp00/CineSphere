package org.dam2.adp.cinesphere.component;

import atlantafx.base.theme.Styles;
import javafx.scene.control.Label;

/**
 * Componente visual para mostrar etiquetas (chips) con un estilo consistente.
 * Utilizado para géneros, actores, directores, etc.
 */
public class Chip extends Label {

    /**
     * Constructor del componente Chip.
     * @param text El texto que se mostrará en el chip.
     */
    public Chip(String text) {
        super(text);
        initialize();
    }

    /**
     * Inicializa el estilo del componente.
     */
    private void initialize() {
        getStyleClass().addAll("chip", Styles.TEXT_SMALL);
    }
}
