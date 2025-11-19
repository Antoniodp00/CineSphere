package org.dam2.adp.cinesphere;


import org.dam2.adp.cinesphere.DAO.UsuarioDAO;
import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.CsvImporter;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.helpers.Util;

import java.time.LocalDate;

public class Pruebas {

    public static void main(String[] args) {

      /*  // 1. Conectar BD
        Conexion.connect("config-postgres.properties");
        System.out.println("BD conectada.");

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            System.out.println("\n--- PRUEBA 1: Insertar usuario ---");

            Usuario u = new Usuario();
            u.setNombreUsuario("antonioTest");
            u.setEmail("antonioTest@test.com");
            u.setPassw(BCrypt.hashpw("1234", BCrypt.gensalt()));
            u.setBornDate(LocalDate.of(2000, 1, 1));

            Usuario insertado = usuarioDAO.insert(u);
            System.out.println("Insertado con ID = " + insertado.getIdUsuario());


            System.out.println("\n--- PRUEBA 2: Buscar por nombre ---");

            Usuario buscado = usuarioDAO.findByName("antonioTest");
            System.out.println("Encontrado: " + buscado.getNombreUsuario() + ", email = " + buscado.getEmail());


            System.out.println("\n--- PRUEBA 3: Login correcto ---");

            boolean loginCorrecto = BCrypt.checkpw("1234", buscado.getPassw());
            System.out.println("¿Login correcto? " + loginCorrecto);


            System.out.println("\n--- PRUEBA 4: Login incorrecto ---");

            boolean loginIncorrecto = BCrypt.checkpw("wrong", buscado.getPassw());
            System.out.println("¿Login incorrecto detectado? " + !loginIncorrecto);


            System.out.println("\n--- PRUEBA 5: Buscar por email ---");

            Usuario porEmail = usuarioDAO.findByEmail("antonioTest@test.com");
            System.out.println("Encontrado por email: " + porEmail.getNombreUsuario());


            System.out.println("\n--- TODAS LAS PRUEBAS COMPLETADAS ---");

        } catch (Exception e) {
            System.out.println("Error en pruebas:");
            e.printStackTrace();
        }
*/
        pruebaImportarCSV();
    }
    public static void pruebaImportarCSV() {
        try {
            System.out.println("Iniciando conexión...");
            Conexion.connect("config-postgres.properties");

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
