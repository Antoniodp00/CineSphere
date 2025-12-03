package org.dam2.adp.cinesphere.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Define los estados posibles de una Película en la lista personal del Usuario.
 * Mapea a VARCHAR(50) en la tabla MiLista.
 */
public enum PeliculaEstado {
    PENDIENTE("Pendiente"),
    VIENDO("Viendo"),
    TERMINADA("Terminada"),
    ABANDONADA("Abandonada");

    private final String estado;
    private static final Logger logger = Logger.getLogger(PeliculaEstado.class.getName());

    /**
     * Constructor del enum.
     * @param estado el valor a mostrar en la interfaz.
     */
    PeliculaEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el valor a mostrar en la interfaz.
     * @return el valor a mostrar.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Convierte una cadena a su correspondiente valor de PeliculaEstado.
     * @param text la cadena a convertir.
     * @return el valor de PeliculaEstado correspondiente.
     * @throws IllegalArgumentException si la cadena no corresponde a ningún estado.
     */
    public static PeliculaEstado fromString(String text) {
        if (text == null) {
            logger.log(Level.FINER, "Valor de estado nulo, se devuelve null.");
            return null;
        }
        for (PeliculaEstado estado : PeliculaEstado.values()) {
            if (estado.estado.equalsIgnoreCase(text)) {
                return estado;
            }
        }
        logger.log(Level.SEVERE, "No hay constante de PeliculaEstado con el valor: " + text);
        throw new IllegalArgumentException("No hay constante con el valor: " + text);
    }
}
