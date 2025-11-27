package org.dam2.adp.cinesphere;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.util.CsvImporter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Pruebas {

    private static final Logger logger = Logger.getLogger(Pruebas.class.getName());

    public static void main(String[] args) {
        pruebaImportarCSV();
    }

    /**
     * Prueba la importación de películas desde un archivo CSV.
     */
    public static void pruebaImportarCSV() {
        try {
            logger.log(Level.INFO, "Iniciando conexión...");
            Conexion.getInstance().connect("config-postgres.properties");
            logger.log(Level.INFO, "BD conectada.");

            CsvImporter importer = new CsvImporter();

            logger.log(Level.INFO, "Importando CSV...");
            importer.importar("src/main/resources/csv/IMDb_Data_final.csv");

            logger.log(Level.INFO, "Importación completada con éxito.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "ERROR durante la importación:", e);
        }
    }
}
