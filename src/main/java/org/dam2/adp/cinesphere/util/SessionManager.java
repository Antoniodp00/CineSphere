package org.dam2.adp.cinesphere.util;

import org.dam2.adp.cinesphere.model.Usuario;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;
    private final Map<String, Object> sessionData = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void set(String key, Object value) {
        sessionData.put(key, value);
    }

    public Object get(String key) {
        return sessionData.get(key);
    }

    public void clear(String key) {
        sessionData.remove(key);
    }

    public void cerrarSesion() {
        usuarioActual = null;
        sessionData.clear();
    }
}
