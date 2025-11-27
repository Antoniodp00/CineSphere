package org.dam2.adp.cinesphere.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa un usuario en el sistema.
 */
public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String email;
    private String passw;
    private LocalDate bornDate;
    private Rol rol;

    // RELACIÓN EAGER opcional
    private List<MiLista> misPeliculas;

    /**
     * Constructor por defecto.
     */
    public Usuario() {}

    /**
     * Constructor para carga perezosa.
     * @param idUsuario el ID del usuario.
     * @param nombreUsuario el nombre de usuario.
     */
    public Usuario(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    /**
     * Constructor para carga ansiosa.
     * @param idUsuario el ID del usuario.
     * @param nombreUsuario el nombre de usuario.
     * @param email el correo electrónico del usuario.
     * @param passw la contraseña del usuario.
     * @param bornDate la fecha de nacimiento del usuario.
     * @param misPeliculas la lista de películas del usuario.
     * @param rol el rol del usuario.
     */
    public Usuario(int idUsuario, String nombreUsuario, String email, String passw,
                   LocalDate bornDate, List<MiLista> misPeliculas, Rol rol) {

        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.passw = passw;
        this.bornDate = bornDate;
        this.misPeliculas = misPeliculas;
        this.rol = rol;
    }

    /**
     * Obtiene el ID del usuario.
     * @return el ID del usuario.
     */
    public int getIdUsuario() { return idUsuario; }

    /**
     * Establece el ID del usuario.
     * @param idUsuario el nuevo ID del usuario.
     */
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    /**
     * Obtiene el nombre de usuario.
     * @return el nombre de usuario.
     */
    public String getNombreUsuario() { return nombreUsuario; }

    /**
     * Establece el nombre de usuario.
     * @param nombreUsuario el nuevo nombre de usuario.
     */
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    /**
     * Obtiene el correo electrónico del usuario.
     * @return el correo electrónico del usuario.
     */
    public String getEmail() { return email; }

    /**
     * Establece el correo electrónico del usuario.
     * @param email el nuevo correo electrónico del usuario.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la contraseña del usuario.
     * @return la contraseña del usuario.
     */
    public String getPassw() { return passw; }

    /**
     * Establece la contraseña del usuario.
     * @param passw la nueva contraseña del usuario.
     */
    public void setPassw(String passw) { this.passw = passw; }

    /**
     * Obtiene la fecha de nacimiento del usuario.
     * @return la fecha de nacimiento del usuario.
     */
    public LocalDate getBornDate() { return bornDate; }

    /**
     * Establece la fecha de nacimiento del usuario.
     * @param bornDate la nueva fecha de nacimiento del usuario.
     */
    public void setBornDate(LocalDate bornDate) { this.bornDate = bornDate; }

    /**
     * Obtiene el rol del usuario.
     * @return el rol del usuario.
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario.
     * @param rol el nuevo rol del usuario.
     */
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Obtiene la lista de películas del usuario.
     * @return la lista de películas del usuario.
     */
    public List<MiLista> getMisPeliculas() { return misPeliculas; }

    /**
     * Establece la lista de películas del usuario.
     * @param misPeliculas la nueva lista de películas del usuario.
     */
    public void setMisPeliculas(List<MiLista> misPeliculas) { this.misPeliculas = misPeliculas; }

    /**
     * Devuelve una representación en cadena del usuario (su nombre de usuario).
     * @return el nombre de usuario.
     */
    @Override
    public String toString() {
        return nombreUsuario;
    }

    /**
     * Comprueba si el usuario es administrador.
     * @return true si el usuario es administrador, false en caso contrario.
     */
    public boolean isAdmin() {
        return this.rol == Rol.ADMIN;
    }
}
