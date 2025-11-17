package org.dam2.adp.cinesphere.model;

import java.time.Duration;
import java.util.List;

public class Pelicula {
        private int id; // idPelicula
        private String titulo; // tituloPelicula
        private int anioEstreno; // yearPelicula
        private double ratingImdb; // ratingPelicula
        private Duration duracion; // duracionPelicula (mapeado a minutos)
        private Clasificacion clasificacion; // nombreClasificacion (POJO de la FK)

        // Colecciones para Carga Eager (N:M). Serán null en Carga Lazy.
        private List<Director> directores;
        private List<Actor> actores;
        private List<Genero> generos;

        // Constructor completo (incluyendo el objeto Clasificacion)
        public Pelicula(int id, String titulo, int anioEstreno, double ratingImdb, Duration duracion, Clasificacion clasificacion) {
            this.id = id;
            this.titulo = titulo;
            this.anioEstreno = anioEstreno;
            this.ratingImdb = ratingImdb;
            this.duracion = duracion;
            this.clasificacion = clasificacion;
        }

        // Constructor para CREATE (sin ID)
        // NOTA: Para la carga CSV, necesitamos un constructor que solo tome el nombre de la clasificación (String)
        public Pelicula(String titulo, int anioEstreno, double ratingImdb, Duration duracion, String nombreClasificacion) {
            this.titulo = titulo;
            this.anioEstreno = anioEstreno;
            this.ratingImdb = ratingImdb;
            this.duracion = duracion;
            this.clasificacion = new Clasificacion(nombreClasificacion); // Crear objeto Clasificacion
        }

        // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Genero> getGeneros() {
        return generos;
    }

    public void setGeneros(List<Genero> generos) {
        this.generos = generos;
    }

    public List<Actor> getActores() {
        return actores;
    }

    public void setActores(List<Actor> actores) {
        this.actores = actores;
    }

    public List<Director> getDirectores() {
        return directores;
    }

    public void setDirectores(List<Director> directores) {
        this.directores = directores;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public Duration getDuracion() {
        return duracion;
    }

    public void setDuracion(Duration duracion) {
        this.duracion = duracion;
    }

    public double getRatingImdb() {
        return ratingImdb;
    }

    public void setRatingImdb(double ratingImdb) {
        this.ratingImdb = ratingImdb;
    }

    public int getAnioEstreno() {
        return anioEstreno;
    }

    public void setAnioEstreno(int anioEstreno) {
        this.anioEstreno = anioEstreno;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String toString() {
        return "Pelicula{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", anioEstreno=" + anioEstreno +
                ", ratingImdb=" + ratingImdb +
                ", duracion=" + duracion +
                ", clasificacion=" + clasificacion +
                ", directores=" + directores +
                ", actores=" + actores +
                ", generos=" + generos +
                '}';
    }
}

