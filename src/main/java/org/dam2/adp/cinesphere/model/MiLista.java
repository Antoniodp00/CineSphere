package org.dam2.adp.cinesphere.model;

import java.time.LocalDateTime;

/**
 * Representa una entrada en la lista de películas de un usuario.
 */
public class MiLista {

    private Pelicula pelicula;
    private Usuario usuario;

    private PeliculaEstado estado;
    private Integer puntuacion;
    private String urlImg;
    private LocalDateTime fechaAnadido;

    /**
     * Constructor por defecto.
     */
    public MiLista() {}

    /**
     * Constructor para carga perezosa.
     * @param pelicula la película.
     * @param usuario el usuario.
     * @param estado el estado de la película.
     * @param puntuacion la puntuación de la película.
     * @param urlImg la URL de la imagen de la película.
     */
    public MiLista(Pelicula pelicula, Usuario usuario, PeliculaEstado estado, Integer puntuacion, String urlImg) {
        this.pelicula = pelicula;
        this.usuario = usuario;
        this.estado = estado;
        this.puntuacion = puntuacion;
        this.urlImg = urlImg;
        this.fechaAnadido = LocalDateTime.now();
    }

    /**
     * Constructor para carga ansiosa.
     * @param pelicula la película.
     * @param usuario el usuario.
     * @param estado el estado de la película.
     * @param puntuacion la puntuación de la película.
     * @param urlImg la URL de la imagen de la película.
     * @param fechaAnadido la fecha en que se añadió la película.
     */
    public MiLista(Pelicula pelicula, Usuario usuario,
                   PeliculaEstado estado, Integer puntuacion,
                   String urlImg, LocalDateTime fechaAnadido) {

        this.pelicula = pelicula;
        this.usuario = usuario;
        this.estado = estado;
        this.puntuacion = puntuacion;
        this.urlImg = urlImg;
        this.fechaAnadido = fechaAnadido;
    }

    /**
     * Obtiene la película.
     * @return la película.
     */
    public Pelicula getPelicula() { return pelicula; }

    /**
     * Establece la película.
     * @param pelicula la nueva película.
     */
    public void setPelicula(Pelicula pelicula) { this.pelicula = pelicula; }

    /**
     * Obtiene el usuario.
     * @return el usuario.
     */
    public Usuario getUsuario() { return usuario; }

    /**
     * Establece el usuario.
     * @param usuario el nuevo usuario.
     */
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    /**
     * Obtiene el estado de la película.
     * @return el estado de la película.
     */
    public PeliculaEstado getEstado() { return estado; }

    /**
     * Establece el estado de la película.
     * @param estado el nuevo estado de la película.
     */
    public void setEstado(PeliculaEstado estado) { this.estado = estado; }

    /**
     * Obtiene la puntuación de la película.
     * @return la puntuación de la película.
     */
    public Integer getPuntuacion() { return puntuacion; }

    /**
     * Establece la puntuación de la película.
     * @param puntuacion la nueva puntuación de la película.
     */
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

    /**
     * Obtiene la URL de la imagen de la película.
     * @return la URL de la imagen de la película.
     */
    public String getUrlImg() { return urlImg; }

    /**
     * Establece la URL de la imagen de la película.
     * @param urlImg la nueva URL de la imagen de la película.
     */
    public void setUrlImg(String urlImg) { this.urlImg = urlImg; }

    /**
     * Obtiene la fecha en que se añadió la película.
     * @return la fecha en que se añadió la película.
     */
    public LocalDateTime getFechaAnadido() { return fechaAnadido; }

    /**
     * Establece la fecha en que se añadió la película.
     * @param fechaAnadido la nueva fecha en que se añadió la película.
     */
    public void setFechaAnadido(LocalDateTime fechaAnadido) { this.fechaAnadido = fechaAnadido; }

    /**
     * Devuelve una representación en cadena de la entrada de la lista (título de la película y estado).
     * @return una cadena con el título de la película y su estado.
     */
    @Override
    public String toString() {
        return pelicula.getTituloPelicula() + " (" + (estado != null ? estado.getEstadoValor() : "Sin estado") + ")";
    }
}
