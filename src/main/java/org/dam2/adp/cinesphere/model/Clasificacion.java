package org.dam2.adp.cinesphere.model;

/**
 * Representa la clasificación por edades de una película (ej. PG-13, R).
 * Hereda de BaseSincronizable para permitir la sincronización de cambios.
 */
public class Clasificacion extends BaseSincronizable {

    // En tu esquema de BBDD, el nombre es la Primary Key
    private String nombreClasificacion;

    /**
     * Constructor por defecto.
     * Inicializa explícitamente los campos de sincronización mediante super().
     */
    public Clasificacion() {
        super();
    }

    /**
     * Constructor con el nombre de la clasificación.
     * @param nombreClasificacion el nombre de la clasificación (PK).
     */
    public Clasificacion(String nombreClasificacion) {
        super(); // Inicializa timestamps y estado eliminado
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
     * Devuelve una representación en cadena de la clasificación.
     * @return el nombre de la clasificación.
     */
    @Override
    public String toString() {
        return nombreClasificacion;
    }
}