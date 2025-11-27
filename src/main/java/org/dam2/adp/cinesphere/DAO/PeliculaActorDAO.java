package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla de relación PeliculaActor.
 */
public class PeliculaActorDAO {

    private static final String SQL_INSERT = "INSERT INTO peliculaactor(idpelicula, idactor) VALUES(?, ?)";
    private static final String SQL_FIND_BY_PELICULA =
            "SELECT a.idactor, a.nombreactor " +
            "FROM actor a JOIN peliculaactor pa ON a.idactor = pa.idactor " +
            "WHERE pa.idpelicula = ?";

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(PeliculaActorDAO.class.getName());

    /**
     * Asocia un actor a una película.
     * @param idPelicula el ID de la película.
     * @param idActor el ID del actor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void add(int idPelicula, int idActor) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setInt(1, idPelicula);
            st.setInt(2, idActor);
            st.executeUpdate();
            logger.log(Level.INFO, "Actor " + idActor + " asociado a la película " + idPelicula);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al asociar actor " + idActor + " a la película " + idPelicula + ": " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene todos los actores de una película.
     * @param idPelicula el ID de la película.
     * @return una lista de los actores de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Actor> findByPelicula(int idPelicula) throws SQLException {
        List<Actor> list = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA)) {
            st.setInt(1, idPelicula);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Actor(rs.getInt(1), rs.getString(2)));
                }
            }
            logger.log(Level.INFO, "Encontrados " + list.size() + " actores para la película " + idPelicula);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar actores para la película " + idPelicula + ": " + e.getMessage(), e);
            throw e;
        }
        return list;
    }
}
