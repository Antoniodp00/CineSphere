package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla de relación PeliculaGenero.
 */
public class PeliculaGeneroDAO {

    private static final String SQL_INSERT = "INSERT INTO peliculagenero(idpelicula, idgenero) VALUES(?, ?)";
    private static final String SQL_FIND_BY_PELICULA =
            "SELECT g.idgenero, g.nombregenero " +
            "FROM genero g JOIN peliculagenero pg ON g.idgenero = pg.idgenero " +
            "WHERE pg.idpelicula = ?";

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(PeliculaGeneroDAO.class.getName());

    /**
     * Asocia un género a una película.
     * @param idPelicula el ID de la película.
     * @param idGenero el ID del género.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void add(int idPelicula, int idGenero) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setInt(1, idPelicula);
            st.setInt(2, idGenero);
            st.executeUpdate();
            logger.log(Level.INFO, "Género " + idGenero + " asociado a la película " + idPelicula);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al asociar género " + idGenero + " a la película " + idPelicula + ": " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene todos los géneros de una película.
     * @param idPelicula el ID de la película.
     * @return una lista de los géneros de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Genero> findByPelicula(int idPelicula) throws SQLException {
        List<Genero> list = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA)) {
            st.setInt(1, idPelicula);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Genero(rs.getInt(1), rs.getString(2)));
                }
            }
            logger.log(Level.INFO, "Encontrados " + list.size() + " géneros para la película " + idPelicula);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar géneros para la película " + idPelicula + ": " + e.getMessage(), e);
            throw e;
        }
        return list;
    }
}
