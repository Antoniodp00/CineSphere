package org.dam2.adp.cinesphere.model;


public class Clasificacion {
    private String nombreClasificacion;

    public Clasificacion() {}

    public Clasificacion(String nombreClasificacion) {
        this.nombreClasificacion = nombreClasificacion;
    }

    public String getNombreClasificacion() {
        return nombreClasificacion;
    }

    public void setNombreClasificacion(String nombreClasificacion) {
        this.nombreClasificacion = nombreClasificacion;
    }

    @Override
    public String toString() {
        return nombreClasificacion;
    }
}
