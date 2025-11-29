package org.dam2.adp.cinesphere.model;

/**
 * Representa un director de cine en el sistema.
 * Hereda de BaseSincronizable para permitir la sincronización de cambios (Soft Delete y Timestamps).
 */
public class Director extends BaseSincronizable {

    private int idDirector;
    private String nombreDirector;

    /**
     * Constructor por defecto.
     * Inicializa explícitamente los campos de sincronización mediante super().
     */
    public Director() {
        super();
    }

    /**
     * Constructor con todos los campos.
     * @param idDirector el ID del director.
     * @param nombreDirector el nombre del director.
     */
    public Director(int idDirector, String nombreDirector) {
        super(); // Inicializa timestamps y estado eliminado
        this.idDirector = idDirector;
        this.nombreDirector = nombreDirector;
    }

    /**
     * Constructor sin el campo ID (para nuevos registros antes de insertar en BD).
     * @param nombreDirector el nombre del director.
     */
    public Director(String nombreDirector) {
        super(); // Inicializa timestamps y estado eliminado
        this.nombreDirector = nombreDirector;
    }

    /**
     * Obtiene el ID del director.
     * @return el ID del director.
     */
    public int getIdDirector() {
        return idDirector;
    }

    /**
     * Establece el ID del director.
     * @param idDirector el nuevo ID del director.
     */
    public void setIdDirector(int idDirector) {
        this.idDirector = idDirector;
    }

    /**
     * Obtiene el nombre del director.
     * @return el nombre del director.
     */
    public String getNombreDirector() {
        return nombreDirector;
    }

    /**
     * Establece el nombre del director.
     * @param nombreDirector el nuevo nombre del director.
     */
    public void setNombreDirector(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    /**
     * Devuelve una representación en cadena del director (su nombre).
     * @return el nombre del director.
     */
    @Override
    public String toString() {
        return nombreDirector;
    }
}