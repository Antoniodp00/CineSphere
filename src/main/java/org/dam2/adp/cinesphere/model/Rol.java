package org.dam2.adp.cinesphere.model;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Rol {
    USER,
    ADMIN;

    private static final Logger logger = Logger.getLogger(Rol.class.getName());

    /**
     * Convierte un String de la base de datos al Enum correspondiente.
     * Es seguro: si es null o no coincide, devuelve USER por defecto.
     */
    public static Rol fromString(String value) {
        if (value == null || value.isBlank()) {
            logger.log(Level.FINER, "Valor de rol nulo o vacío, se devuelve USER por defecto.");
            return USER; // Valor por defecto
        }
        try {
            return Rol.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Rol desconocido en BD: '" + value + "'. Se asigna USER por defecto.", e);
            return USER;
        }
    }
}
