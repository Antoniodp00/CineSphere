package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        }
        return list;
    }
}
