package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Director;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de relación PeliculaDirector.
 */
public class PeliculaDirectorDAO {

    private static final String SQL_INSERT = "INSERT INTO peliculadirector(idpelicula, iddirector) VALUES(?, ?)";
    private static final String SQL_FIND_BY_PELICULA =
            "SELECT d.iddirector, d.nombredirector " +
            "FROM director d JOIN peliculadirector pd ON d.iddirector = pd.iddirector " +
            "WHERE pd.idpelicula = ?";

    private final Connection conn = Conexion.getInstance().getConnection();

    /**
     * Asocia un director a una película.
     * @param idPelicula el ID de la película.
     * @param idDirector el ID del director.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void add(int idPelicula, int idDirector) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setInt(1, idPelicula);
            st.setInt(2, idDirector);
            st.executeUpdate();
        }
    }

    /**
     * Obtiene todos los directores de una película.
     * @param idPelicula el ID de la película.
     * @return una lista de los directores de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Director> findByPelicula(int idPelicula) throws SQLException {
        List<Director> list = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_PELICULA)) {
            st.setInt(1, idPelicula);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Director(rs.getInt(1), rs.getString(2)));
                }
            }
        }
        return list;
    }
}
