package org.dam2.adp.cinesphere.model;

import java.util.List;

public class Pelicula {

    private int idPelicula;
    private String tituloPelicula;
    private Integer yearPelicula;
    private Double ratingPelicula;
    private Integer duracionPelicula;
    private String nombreClasificacion;

    // EAGER opcional
    private Clasificacion clasificacion;
    private List<Director> directores;
    private List<Actor> actores;
    private List<Genero> generos;
    private List<MiLista> usuariosQueLaTienen;

    public Pelicula() {}

    // LAZY
    public Pelicula(int idPelicula, String tituloPelicula) {
        this.idPelicula = idPelicula;
        this.tituloPelicula = tituloPelicula;
    }

    // EAGER
    public Pelicula(int idPelicula,
                    String tituloPelicula,
                    Integer yearPelicula,
                    Double ratingPelicula,
                    Integer duracionPelicula,
                    String nombreClasificacion,
                    Clasificacion clasificacion,
                    List<Director> directores,
                    List<Actor> actores,
                    List<Genero> generos,
                    List<MiLista> usuariosQueLaTienen) {

        this.idPelicula = idPelicula;
        this.tituloPelicula = tituloPelicula;
        this.yearPelicula = yearPelicula;
        this.ratingPelicula = ratingPelicula;
        this.duracionPelicula = duracionPelicula;
        this.nombreClasificacion = nombreClasificacion;

        this.clasificacion = clasificacion;
        this.directores = directores;
        this.actores = actores;
        this.generos = generos;
        this.usuariosQueLaTienen = usuariosQueLaTienen;
    }

    // Getters y setters


    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public void setTituloPelicula(String tituloPelicula) {
        this.tituloPelicula = tituloPelicula;
    }

    public Integer getYearPelicula() {
        return yearPelicula;
    }

    public void setYearPelicula(Integer yearPelicula) {
        this.yearPelicula = yearPelicula;
    }

    public Double getRatingPelicula() {
        return ratingPelicula;
    }

    public void setRatingPelicula(Double ratingPelicula) {
        this.ratingPelicula = ratingPelicula;
    }

    public Integer getDuracionPelicula() {
        return duracionPelicula;
    }

    public void setDuracionPelicula(Integer duracionPelicula) {
        this.duracionPelicula = duracionPelicula;
    }

    public String getNombreClasificacion() {
        return nombreClasificacion;
    }

    public void setNombreClasificacion(String nombreClasificacion) {
        this.nombreClasificacion = nombreClasificacion;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public List<Director> getDirectores() {
        return directores;
    }

    public void setDirectores(List<Director> directores) {
        this.directores = directores;
    }

    public List<Actor> getActores() {
        return actores;
    }

    public void setActores(List<Actor> actores) {
        this.actores = actores;
    }

    public List<Genero> getGeneros() {
        return generos;
    }

    public void setGeneros(List<Genero> generos) {
        this.generos = generos;
    }

    public List<MiLista> getUsuariosQueLaTienen() {
        return usuariosQueLaTienen;
    }

    public void setUsuariosQueLaTienen(List<MiLista> usuariosQueLaTienen) {
        this.usuariosQueLaTienen = usuariosQueLaTienen;
    }

    @Override
    public String toString() {
        return tituloPelicula;
    }
}
