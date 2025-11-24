package org.dam2.adp.cinesphere.model;

/**
 * Representa un género de película en el sistema.
 */
public class Genero {
    private int idGenero;
    private String nombreGenero;

    /**
     * Constructor por defecto.
     */
    public Genero() {}

    /**
     * Constructor con todos los campos.
     * @param idGenero el ID del género.
     * @param nombreGenero el nombre del género.
     */
    public Genero(int idGenero, String nombreGenero) {
        this.idGenero = idGenero;
        this.nombreGenero = nombreGenero;
    }

    /**
     * Constructor sin el campo ID.
     * @param nombreGenero el nombre del género.
     */
    public Genero(String nombreGenero) {
        this.nombreGenero = nombreGenero;
    }

    /**
     * Obtiene el ID del género.
     * @return el ID del género.
     */
    public int getIdGenero() {
        return idGenero;
    }

    /**
     * Establece el ID del género.
     * @param idGenero el nuevo ID del género.
     */
    public void setIdGenero(int idGenero) {
        this.idGenero = idGenero;
    }

    /**
     * Obtiene el nombre del género.
     * @return el nombre del género.
     */
    public String getNombreGenero() {
        return nombreGenero;
    }

    /**
     * Establece el nombre del género.
     * @param nombreGenero el nuevo nombre del género.
     */
    public void setNombreGenero(String nombreGenero) {
        this.nombreGenero = nombreGenero;
    }

    /**
     * Devuelve una representación en cadena del género (su nombre).
     * @return el nombre del género.
     */
    @Override
    public String toString() {
        return nombreGenero;
    }
}
