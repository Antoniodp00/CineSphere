package org.dam2.adp.cinesphere.model;

import java.time.LocalDateTime;

/**
 * Clase base para todas las entidades que requieren sincronización y borrado lógico.
 * Evita la duplicación de código en los modelos.
 */
public abstract class BaseSincronizable {

    protected LocalDateTime ultimaModificacion;
    protected boolean eliminado;

    public BaseSincronizable() {
        // Por defecto, al crear un objeto en memoria, está activo y la fecha es ahora.
        this.ultimaModificacion = LocalDateTime.now();
        this.eliminado = false;
    }

    // --- Getters y Setters ---

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}