package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaGeneroDAO {

    private static final String SQL_INSERT =
            "INSERT INTO peliculagenero(idpelicula, idgenero) VALUES(?, ?)";

    private static final String SQL_FIND_BY_PELICULA =
            "SELECT g.idgenero, g.nombregenero " +
                    "FROM genero g JOIN peliculagenero pg ON g.idgenero = pg.idgenero " +
                    "WHERE pg.idpelicula = ?";

    private final Connection conn = Conexion.getInstance().getConnection();

    public void add(int idPelicula, int idGenero) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setInt(1, idPelicula);
        st.setInt(2, idGenero);
        st.executeUpdate();
    }

    public List<Genero> findByPelicula(int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA);
        st.setInt(1, idPelicula);
        ResultSet rs = st.executeQuery();
        List<Genero> list = new ArrayList<>();
        while (rs.next()) list.add(new Genero(rs.getInt(1), rs.getString(2)));
        return list;
    }
}
