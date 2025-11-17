package org.dam2.adp.cinesphere.model;

public class Clasificacion {
    private String nombre;

    public Clasificacion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
