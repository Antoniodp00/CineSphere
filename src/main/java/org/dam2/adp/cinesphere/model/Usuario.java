package org.dam2.adp.cinesphere.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Usuario {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDate fechaNacimiento;

    private List<MiLista> listaPerosnal;

    // Constructor completo
    public Usuario(int id, String username, String email, String passwordHash, LocalDate fechaNacimiento) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Constructor para CREATE (sin ID)
    public Usuario(String username, String email, String passwordHash, LocalDate fechaNacimiento) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public List<MiLista> getListaPerosnal() {
        return listaPerosnal;
    }

    public void setListaPerosnal(List<MiLista> listaPerosnal) {
        this.listaPerosnal = listaPerosnal;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", listaPerosnal=" + listaPerosnal +
                '}';
    }
}
