package org.dam2.adp.cinesphere.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enumeración que representa los roles de usuario en la aplicación.
 */
public enum Rol {
    /**
     * Rol de usuario estándar.
     */
    USER,
    /**
     * Rol de administrador con permisos elevados.
     */
    ADMIN;

    private static final Logger logger = Logger.getLogger(Rol.class.getName());

    /**
     * Convierte un String de la base de datos al Enum correspondiente.
     * Es seguro: si es null o no coincide, devuelve USER por defecto.
     * @param value El valor de la cadena a convertir.
     * @return El enum Rol correspondiente.
     */
    public static Rol fromString(String value) {
        if (value == null || value.isBlank()) {
            logger.log(Level.FINER, "Valor de rol nulo o vacío, se devuelve USER por defecto.");
            return USER;
        }
        try {
            return Rol.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Rol desconocido en BD: '" + value + "'. Se asigna USER por defecto.", e);
            return USER;
        }
    }
}
