package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaActorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO PeliculaActor(idPelicula, idActor) VALUES(?, ?)";

    private static final String SQL_FIND_BY_PELICULA =
            "SELECT a.idActor, a.nombreActor " +
                    "FROM Actor a JOIN PeliculaActor pa ON a.idActor = pa.idActor " +
                    "WHERE pa.idPelicula = ?";

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

        while (rs.next())
            list.add(new Actor(rs.getInt(1), rs.getString(2)));

        return list;
    }
}

