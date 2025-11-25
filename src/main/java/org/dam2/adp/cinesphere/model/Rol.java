package org.dam2.adp.cinesphere.model;

public enum Rol {
    USER,
    ADMIN;

    /**
     * Convierte un String de la base de datos al Enum correspondiente.
     * Es seguro: si es null o no coincide, devuelve USER por defecto.
     */
    public static Rol fromString(String value) {
        if (value == null || value.isBlank()) {
            return USER; // Valor por defecto
        }
        try {
            return Rol.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Rol desconocido en BD: " + value + ". Se asigna USER.");
            return USER;
        }
    }
}