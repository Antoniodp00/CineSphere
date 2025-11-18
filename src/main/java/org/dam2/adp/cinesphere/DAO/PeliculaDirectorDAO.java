package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Director;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDirectorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO PeliculaDirector(idPelicula, idDirector) VALUES(?, ?)";

    private static final String SQL_FIND_BY_PELICULA =
            "SELECT d.idDirector, d.nombreDirector " +
                    "FROM Director d JOIN PeliculaDirector pd ON d.idDirector = pd.idDirector " +
                    "WHERE pd.idPelicula = ?";

    private final Connection conn = Conexion.getConnection();

    public void add(int idPelicula, int idDirector) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setInt(1, idPelicula);
        st.setInt(2, idDirector);
        st.executeUpdate();
    }

    public List<Director> findByPelicula(int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA);
        st.setInt(1, idPelicula);

        ResultSet rs = st.executeQuery();
        List<Director> list = new ArrayList<>();

        while (rs.next())
            list.add(new Director(rs.getInt(1), rs.getString(2)));

        return list;
    }
}
