package org.dam2.adp.cinesphere.model;

import java.time.LocalDateTime;

public class MiLista {

    private Pelicula pelicula;
    private Usuario usuario;

    private PeliculaEstado estado;
    private Integer puntuacion;
    private String urlImg;
    private LocalDateTime fechaAnadido;

    public MiLista() {}

    // LAZY
    public MiLista(Pelicula pelicula, Usuario usuario) {
        this.pelicula = pelicula;
        this.usuario = usuario;
        this.fechaAnadido = LocalDateTime.now();
    }

    // EAGER
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

    public Pelicula getPelicula() { return pelicula; }
    public void setPelicula(Pelicula pelicula) { this.pelicula = pelicula; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public PeliculaEstado getEstado() { return estado; }
    public void setEstado(PeliculaEstado estado) { this.estado = estado; }

    public Integer getPuntuacion() { return puntuacion; }
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

    public String getUrlImg() { return urlImg; }
    public void setUrlImg(String urlImg) { this.urlImg = urlImg; }

    public LocalDateTime getFechaAnadido() { return fechaAnadido; }
    public void setFechaAnadido(LocalDateTime fechaAnadido) { this.fechaAnadido = fechaAnadido; }

    @Override
    public String toString() {
        return pelicula.getTituloPelicula() + " (" + (estado != null ? estado.getDisplayValue() : "Sin estado") + ")";
    }
}
