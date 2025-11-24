package org.dam2.adp.cinesphere.model;

/**
 * Representa un actor en el sistema.
 */
public class Actor {
    private int idActor;
    private String nombreActor;

    /**
     * Constructor por defecto.
     */
    public Actor() {}

    /**
     * Constructor con todos los campos.
     * @param idActor el ID del actor.
     * @param nombreActor el nombre del actor.
     */
    public Actor(int idActor, String nombreActor) {
        this.idActor = idActor;
        this.nombreActor = nombreActor;
    }

    /**
     * Constructor sin el campo ID.
     * @param nombreActor el nombre del actor.
     */
    public Actor(String nombreActor) {
        this.nombreActor = nombreActor;
    }

    /**
     * Obtiene el ID del actor.
     * @return el ID del actor.
     */
    public int getIdActor() {
        return idActor;
    }

    /**
     * Establece el ID del actor.
     * @param idActor el nuevo ID del actor.
     */
    public void setIdActor(int idActor) {
        this.idActor = idActor;
    }

    /**
     * Obtiene el nombre del actor.
     * @return el nombre del actor.
     */
    public String getNombreActor() {
        return nombreActor;
    }

    /**
     * Establece el nombre del actor.
     * @param nombreActor el nuevo nombre del actor.
     */
    public void setNombreActor(String nombreActor) {
        this.nombreActor = nombreActor;
    }

    /**
     * Devuelve una representación en cadena del actor (su nombre).
     * @return el nombre del actor.
     */
    @Override
    public String toString() {
        return nombreActor;
    }
}
