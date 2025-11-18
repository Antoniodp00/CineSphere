package org.dam2.adp.cinesphere.model;


public class Actor {
    private int idActor;
    private String nombreActor;

    public Actor() {}

    public Actor(int idActor, String nombreActor) {
        this.idActor = idActor;
        this.nombreActor = nombreActor;
    }

    public Actor(String nombreActor) {
        this.nombreActor = nombreActor;
    }

    public int getIdActor() {
        return idActor;
    }

    public void setIdActor(int idActor) {
        this.idActor = idActor;
    }

    public String getNombreActor() {
        return nombreActor;
    }

    public void setNombreActor(String nombreActor) {
        this.nombreActor = nombreActor;
    }

    @Override
    public String toString() {
        return nombreActor;
    }
}
