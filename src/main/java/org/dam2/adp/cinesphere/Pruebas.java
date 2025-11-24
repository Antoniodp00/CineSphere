package org.dam2.adp.cinesphere;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.util.CsvImporter;

public class Pruebas {

    public static void main(String[] args) {
        pruebaImportarCSV();
    }

    /**
     * Prueba la importación de películas desde un archivo CSV.
     */
    public static void pruebaImportarCSV() {
        try {
            System.out.println("Iniciando conexión...");
            Conexion.getInstance().connect("config-postgres.properties");
            System.out.println("BD conectada.");

            CsvImporter importer = new CsvImporter();

            System.out.println("Importando CSV...");
            importer.importar("src/main/resources/csv/IMDb_Data_final.csv");

            System.out.println("Importación completada con éxito.");

        } catch (Exception e) {
            System.out.println("ERROR durante la importación:");
            e.printStackTrace();
        }
    }
}
