package org.dam2.adp.cinesphere.model;

/**
 * Define los estados posibles de una Película en la lista personal del Usuario.
 * Mapea a VARCHAR(50) en la tabla MiLista.
 */
public enum PeliculaEstado {
    PENDIENTE("Pendiente"),
    VIENDO("Viendo"),
    TERMINADA("Terminada"),
    ABANDONADA("Abandonada");

    private final String displayValue;

    PeliculaEstado(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    // Método estático útil para mapeo DAO (de String a Enum)
    public static PeliculaEstado fromString(String text) {
        if (text == null) return null;
        for (PeliculaEstado estado : PeliculaEstado.values()) {
            if (estado.displayValue.equalsIgnoreCase(text)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("No hay constante con el valor: " + text);
    }
}
