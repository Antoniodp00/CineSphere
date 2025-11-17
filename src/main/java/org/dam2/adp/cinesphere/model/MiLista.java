package org.dam2.adp.cinesphere.model;

import java.time.LocalDate;

public class MiLista {
    // Clave compuesta y FKs. Contienen las entidades completas.
    private Usuario usuario;
    private Pelicula pelicula;

    // Atributos de la relación N:M
    private PeliculaEstado estado; // Usando el Enum
    private int puntuacion;
    private String urlImagen; // urlImg
    private LocalDate fechaAnadido; // fecha_anadido

    // Constructor completo
    public MiLista(Usuario usuario, Pelicula pelicula, PeliculaEstado estado, int puntuacion, String urlImagen, LocalDate fechaAnadido) {
        this.usuario = usuario;
        this.pelicula = pelicula;
        this.estado = estado;
        this.puntuacion = puntuacion;
        this.urlImagen = urlImagen;
        this.fechaAnadido = fechaAnadido;
    }

    // Constructor para CREATE (sin fechaAnadido, que es TIMESTAMP DEFAULT)
    public MiLista(Usuario usuario, Pelicula pelicula, PeliculaEstado estado, int puntuacion, String urlImagen) {
        this.usuario = usuario;
        this.pelicula = pelicula;
        this.estado = estado;
        this.puntuacion = puntuacion;
        this.urlImagen = urlImagen;
    }

    // Getters y Setters

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    public PeliculaEstado getEstado() {
        return estado;
    }

    public void setEstado(PeliculaEstado estado) {
        this.estado = estado;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public LocalDate getFechaAnadido() {
        return fechaAnadido;
    }

    public void setFechaAnadido(LocalDate fechaAnadido) {
        this.fechaAnadido = fechaAnadido;
    }
}

