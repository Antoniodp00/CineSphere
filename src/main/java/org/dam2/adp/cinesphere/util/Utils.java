package org.dam2.adp.cinesphere.util;

public class Utils {

    public static String obtenerRutaImagenPorGenero(String nombreGenero) {
        if (nombreGenero == null) return "/img/noImage.png";

        // Convertimos a String normalizado por si acaso, pero compararemos con tu lista exacta
        switch (nombreGenero) {
            case "Action":
                return "/img/Accion.png"; // Mapea "Action" a tu archivo "Accion.png"
            case "Drama":
                return "/img/Drama.png";
            case "Adventure":
                return "/img/Aventure.png"; // Ojo: tu archivo se llama "Aventure.png"
            case "Comedy":
                return "/img/Comedy.png";
            case "Crime":
                return "/img/Crime.png";
            case "Sci-Fi":
                return "/img/Scifipng.png"; // Ojo: tu archivo se llama "Scifipng.png"
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
                return "/img/Mistey.png"; // Ojo: tu archivo se llama "Mistey.png"
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
                return "/img/musical.png"; // Ojo: tu archivo empieza con minúscula
            default:
                return "/img/noImage.png";
        }
    }
}
