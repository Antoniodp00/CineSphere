package org.dam2.adp.cinesphere.model;


public class Director {
    private int idDirector;
    private String nombreDirector;

    public Director() {}

    public Director(int idDirector, String nombreDirector) {
        this.idDirector = idDirector;
        this.nombreDirector = nombreDirector;
    }

    public Director(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    public int getIdDirector() {
        return idDirector;
    }

    public void setIdDirector(int idDirector) {
        this.idDirector = idDirector;
    }

    public String getNombreDirector() {
        return nombreDirector;
    }

    public void setNombreDirector(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    @Override
    public String toString() {
        return nombreDirector;
    }
}
