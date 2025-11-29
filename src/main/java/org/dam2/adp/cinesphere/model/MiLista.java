package org.dam2.adp.cinesphere.model;

import java.time.LocalDateTime;

/**
 * Representa una entrada en la lista de películas de un usuario.
 * Hereda de BaseSincronizable para permitir la sincronización de cambios (Soft Delete y Timestamps).
 */
public class MiLista extends BaseSincronizable {

    private Pelicula pelicula;
    private Usuario usuario;

    private PeliculaEstado estado;
    private Integer puntuacion;
    private String urlImg;
    private LocalDateTime fechaAnadido;

    /**
     * Constructor por defecto.
     * Inicializa explícitamente los campos de sincronización mediante super().
     */
    public MiLista() {
        super();
    }

    /**
     * Constructor para carga perezosa (Lazy).
     * @param pelicula la película asociada.
     * @param usuario el usuario propietario de la lista.
     */
    public MiLista(Pelicula pelicula, Usuario usuario) {
        super(); // Inicializa timestamps y estado eliminado
        this.pelicula = pelicula;
        this.usuario = usuario;
        this.fechaAnadido = LocalDateTime.now();
    }

    /**
     * Constructor completo (Eager).
     * @param pelicula la película.
     * @param usuario el usuario.
     * @param estado el estado de visualización.
     * @param puntuacion la puntuación personal (0-10).
     * @param urlImg la URL de la imagen (opcional).
     * @param fechaAnadido la fecha de inclusión en la lista.
     */
    public MiLista(Pelicula pelicula, Usuario usuario,
                   PeliculaEstado estado, Integer puntuacion,
                   String urlImg, LocalDateTime fechaAnadido) {
        super(); // Inicializa timestamps y estado eliminado
        this.pelicula = pelicula;
        this.usuario = usuario;
        this.estado = estado;
        this.puntuacion = puntuacion;
        this.urlImg = urlImg;
        this.fechaAnadido = fechaAnadido;
    }

    // --- Getters y Setters ---

    public Pelicula getPelicula() {
        return pelicula;
    }

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PeliculaEstado getEstado() {
        return estado;
    }

    public void setEstado(PeliculaEstado estado) {
        this.estado = estado;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }

    public LocalDateTime getFechaAnadido() {
        return fechaAnadido;
    }

    public void setFechaAnadido(LocalDateTime fechaAnadido) {
        this.fechaAnadido = fechaAnadido;
    }

    @Override
    public String toString() {
        return pelicula.getTituloPelicula() + " (" + (estado != null ? estado.getDisplayValue() : "Sin estado") + ")";
    }
}