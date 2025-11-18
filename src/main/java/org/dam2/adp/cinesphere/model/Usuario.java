package org.dam2.adp.cinesphere.model;

import java.time.LocalDate;
import java.util.List;

public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String email;
    private String passw;
    private LocalDate bornDate;

    // RELACIÓN EAGER opcional
    private List<MiLista> misPeliculas;

    public Usuario() {}

    // Constructor LAZY
    public Usuario(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    // Constructor EAGER
    public Usuario(int idUsuario, String nombreUsuario, String email, String passw,
                   LocalDate bornDate, List<MiLista> misPeliculas) {

        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.passw = passw;
        this.bornDate = bornDate;
        this.misPeliculas = misPeliculas;
    }

    // getters + setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassw() { return passw; }
    public void setPassw(String passw) { this.passw = passw; }

    public LocalDate getBornDate() { return bornDate; }
    public void setBornDate(LocalDate bornDate) { this.bornDate = bornDate; }

    public List<MiLista> getMisPeliculas() { return misPeliculas; }
    public void setMisPeliculas(List<MiLista> misPeliculas) { this.misPeliculas = misPeliculas; }

    @Override
    public String toString() {
        return nombreUsuario;
    }
}
