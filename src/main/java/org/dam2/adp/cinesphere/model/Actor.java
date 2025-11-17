package org.dam2.adp.cinesphere.model;

public class Actor {
    private int id;
    private String nombre;

    public Actor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Actor(String nombre) {
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
        return "Actor{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               '}';
    }

}
