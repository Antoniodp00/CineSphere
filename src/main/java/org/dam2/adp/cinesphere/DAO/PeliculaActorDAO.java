package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaActorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO peliculaactor(idpelicula, idactor) VALUES(?, ?)";

    private static final String SQL_FIND_BY_PELICULA =
            "SELECT a.idactor, a.nombreactor " +
                    "FROM actor a JOIN peliculaactor pa ON a.idactor = pa.idactor " +
                    "WHERE pa.idpelicula = ?";

    private final Connection conn = Conexion.getConnection();

    public void add(int idPelicula, int idActor) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setInt(1, idPelicula);
        st.setInt(2, idActor);
        st.executeUpdate();
    }

    public List<Actor> findByPelicula(int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA);
        st.setInt(1, idPelicula);
        ResultSet rs = st.executeQuery();
        List<Actor> list = new ArrayList<>();
        while (rs.next()) list.add(new Actor(rs.getInt(1), rs.getString(2)));
        return list;
    }
}