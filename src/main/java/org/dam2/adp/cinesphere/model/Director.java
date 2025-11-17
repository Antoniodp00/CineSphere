package org.dam2.adp.cinesphere.model;

public class Director {
    private int id;
    private String nombre;

    public Director(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Director(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Director{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               '}';
    }

}
