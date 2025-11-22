package org.dam2.adp.cinesphere.model;

/**
 * Representa una clasificación de película en el sistema.
 */
public class Clasificacion {
    private String nombreClasificacion;

    /**
     * Constructor por defecto.
     */
    public Clasificacion() {}

    /**
     * Constructor con el nombre de la clasificación.
     * @param nombreClasificacion el nombre de la clasificación.
     */
    public Clasificacion(String nombreClasificacion) {
        this.nombreClasificacion = nombreClasificacion;
    }

    /**
     * Obtiene el nombre de la clasificación.
     * @return el nombre de la clasificación.
     */
    public String getNombreClasificacion() {
        return nombreClasificacion;
    }

    /**
     * Establece el nombre de la clasificación.
     * @param nombreClasificacion el nuevo nombre de la clasificación.
     */
    public void setNombreClasificacion(String nombreClasificacion) {
        this.nombreClasificacion = nombreClasificacion;
    }

    /**
     * Devuelve una representación en cadena de la clasificación (su nombre).
     * @return el nombre de la clasificación.
     */
    @Override
    public String toString() {
        return nombreClasificacion;
    }
}
