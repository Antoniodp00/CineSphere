package org.dam2.adp.cinesphere.model;

import java.util.List;

/**
 * Representa una película en el sistema.
 */
public class Pelicula {

    private int idPelicula;
    private String tituloPelicula;
    private Integer yearPelicula;
    private Double ratingPelicula;
    private Integer duracionPelicula;

    // EAGER opcional
    private Clasificacion clasificacion;
    private List<Director> directores;
    private List<Actor> actores;
    private List<Genero> generos;
    private List<MiLista> usuariosQueLaTienen;

    /**
     * Constructor por defecto.
     */
    public Pelicula() {}

    /**
     * Constructor para carga lazy (datos principales de la película).
     * @param idPelicula el ID de la película.
     * @param tituloPelicula el título de la película.
     * @param yearPelicula el año de la película.
     * @param ratingPelicula el rating de la película.
     * @param duracionPelicula la duración de la película.
     */
    public Pelicula(int idPelicula,
                    String tituloPelicula,
                    Integer yearPelicula,
                    Double ratingPelicula,
                    Integer duracionPelicula) {
        this.idPelicula = idPelicula;
        this.tituloPelicula = tituloPelicula;
        this.yearPelicula = yearPelicula;
        this.ratingPelicula = ratingPelicula;
        this.duracionPelicula = duracionPelicula;
    }

    /**
     * Constructor para carga eager.
     * @param idPelicula el ID de la película.
     * @param tituloPelicula el título de la película.
     * @param yearPelicula el año de la película.
     * @param ratingPelicula el rating de la película.
     * @param duracionPelicula la duración de la película.
     * @param clasificacion la clasificación de la película.
     * @param directores los directores de la película.
     * @param actores los actores de la película.
     * @param generos los géneros de la película.
     * @param usuariosQueLaTienen los usuarios que tienen la película en su lista.
     */
    public Pelicula(int idPelicula,
                    String tituloPelicula,
                    Integer yearPelicula,
                    Double ratingPelicula,
                    Integer duracionPelicula,
                    Clasificacion clasificacion,
                    List<Director> directores,
                    List<Actor> actores,
                    List<Genero> generos,
                    List<MiLista> usuariosQueLaTienen) {

        this(idPelicula, tituloPelicula, yearPelicula, ratingPelicula, duracionPelicula);
        this.clasificacion = clasificacion;
        this.directores = directores;
        this.actores = actores;
        this.generos = generos;
        this.usuariosQueLaTienen = usuariosQueLaTienen;
    }

    /**
     * Obtiene el ID de la película.
     * @return el ID de la película.
     */
    public int getIdPelicula() {
        return idPelicula;
    }

    /**
     * Establece el ID de la película.
     * @param idPelicula el nuevo ID de la película.
     */
    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    /**
     * Obtiene el título de la película.
     * @return el título de la película.
     */
    public String getTituloPelicula() {
        return tituloPelicula;
    }

    /**
     * Establece el título de la película.
     * @param tituloPelicula el nuevo título de la película.
     */
    public void setTituloPelicula(String tituloPelicula) {
        this.tituloPelicula = tituloPelicula;
    }

    /**
     * Obtiene el año de la película.
     * @return el año de la película.
     */
    public Integer getYearPelicula() {
        return yearPelicula;
    }

    /**
     * Establece el año de la película.
     * @param yearPelicula el nuevo año de la película.
     */
    public void setYearPelicula(Integer yearPelicula) {
        this.yearPelicula = yearPelicula;
    }

    /**
     * Obtiene el rating de la película.
     * @return el rating de la película.
     */
    public Double getRatingPelicula() {
        return ratingPelicula;
    }

    /**
     * Establece el rating de la película.
     * @param ratingPelicula el nuevo rating de la película.
     */
    public void setRatingPelicula(Double ratingPelicula) {
        this.ratingPelicula = ratingPelicula;
    }

    /**
     * Obtiene la duración de la película.
     * @return la duración de la película.
     */
    public Integer getDuracionPelicula() {
        return duracionPelicula;
    }

    /**
     * Establece la duración de la película.
     * @param duracionPelicula la nueva duración de la película.
     */
    public void setDuracionPelicula(Integer duracionPelicula) {
        this.duracionPelicula = duracionPelicula;
    }

    /**
     * Obtiene la clasificación de la película.
     * @return la clasificación de la película.
     */
    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    /**
     * Establece la clasificación de la película.
     * @param clasificacion la nueva clasificación de la película.
     */
    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    /**
     * Obtiene los directores de la película.
     * @return una lista de los directores de la película.
     */
    public List<Director> getDirectores() {
        return directores;
    }

    /**
     * Establece los directores de la película.
     * @param directores la nueva lista de directores de la película.
     */
    public void setDirectores(List<Director> directores) {
        this.directores = directores;
    }

    /**
     * Obtiene los actores de la película.
     * @return una lista de los actores de la película.
     */
    public List<Actor> getActores() {
        return actores;
    }

    /**
     * Establece los actores de la película.
     * @param actores la nueva lista de actores de la película.
     */
    public void setActores(List<Actor> actores) {
        this.actores = actores;
    }

    /**
     * Obtiene los géneros de la película.
     * @return una lista de los géneros de la película.
     */
    public List<Genero> getGeneros() {
        return generos;
    }

    /**
     * Establece los géneros de la película.
     * @param generos la nueva lista de géneros de la película.
     */
    public void setGeneros(List<Genero> generos) {
        this.generos = generos;
    }

    /**
     * Obtiene los usuarios que tienen la película en su lista.
     * @return una lista de los usuarios que tienen la película en su lista.
     */
    public List<MiLista> getUsuariosQueLaTienen() {
        return usuariosQueLaTienen;
    }

    /**
     * Establece los usuarios que tienen la película en su lista.
     * @param usuariosQueLaTienen la nueva lista de usuarios que tienen la película en su lista.
     */
    public void setUsuariosQueLaTienen(List<MiLista> usuariosQueLaTienen) {
        this.usuariosQueLaTienen = usuariosQueLaTienen;
    }

    /**
     * Devuelve una representación en cadena de la película (su título).
     * @return el título de la película.
     */
    @Override
    public String toString() {
        return tituloPelicula;
    }
}
