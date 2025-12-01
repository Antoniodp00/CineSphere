package org.dam2.adp.cinesphere.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Clase de utilidades generales para la aplicación.
 */
public class Utils {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Obtiene la ruta de la imagen asociada a un género.
     * @param nombreGenero el nombre del género.
     * @return la ruta de la imagen.
     */
    public static String obtenerRutaImagenPorGenero(String nombreGenero) {
        if (nombreGenero == null) return "/img/noImage.png";


        switch (nombreGenero) {
            case "Action":
                return "/img/Action.png";
            case "Drama":
                return "/img/Drama.png";
            case "Adventure":
                return "/img/Aventure.png";
            case "Comedy":
                return "/img/Comedy.png";
            case "Crime":
                return "/img/Crime.png";
            case "Sci-Fi":
                return "/img/Scifi.png";
            case "Fantasy":
                return "/img/Fantasy.png";
            case "Biography":
                return "/img/Biography.png";
            case "Romance":
                return "/img/Romance.png";
            case "Family":
                return "/img/Family.png";
            case "Horror":
                return "/img/Horror.png";
            case "Music":
                return "/img/Music.png";
            case "Thriller":
                return "/img/Thriller.png";
            case "War":
                return "/img/War.png";
            case "Mystery":
                return "/img/Mistey.png";
            case "History":
                return "/img/History.png";
            case "Western":
                return "/img/Western.png";
            case "Sport":
                return "/img/Sport.png";
            case "Animation":
                return "/img/Animation.png";
            case "Film-Noir":
                return "/img/Film-noir.png";
            case "Musical":
                return "/img/musical.png";
            default:
                return "/img/noImage.png";
        }
    }

    /**
     * Valida si una fecha de nacimiento cumple con la edad mínima requerida
     * y no es una fecha futura.
     *
     * @param fechaNacimiento La fecha a validar.
     * @param edadMinima      La edad mínima requerida (ej. 14, 18).
     * @return true si la fecha es válida y cumple la edad; false en caso contrario o si es nula.
     */
    public static boolean esEdadValida(LocalDate fechaNacimiento, int edadMinima) {
        if (fechaNacimiento == null) {
            return false;
        }

        LocalDate fechaActual = LocalDate.now();

        if (fechaNacimiento.isAfter(fechaActual)) {
            return false;
        }

        int edad = Period.between(fechaNacimiento, fechaActual).getYears();
        return edad >= edadMinima;
    }

    /**
     * Valida el formato de un correo electrónico usando expresiones regulares.
     * @param email El texto del correo a validar.
     * @return true si el formato es correcto, false si es nulo o incorrecto.
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
