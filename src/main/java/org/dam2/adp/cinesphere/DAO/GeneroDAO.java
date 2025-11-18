package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GeneroDAO {

    private static final String SQL_INSERT =
            "INSERT INTO genero(nombregenero) VALUES(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT idgenero, nombregenero FROM genero WHERE idgenero=?";

    private static final String SQL_FIND_ALL =
            "SELECT idgenero, nombregenero FROM genero";

    private final Connection conn = Conexion.getConnection();

    public Genero insert(Genero g) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, g.getNombreGenero());
        st.executeUpdate();
        ResultSet keys = st.getGeneratedKeys();
        if (keys.next()) g.setIdGenero(keys.getInt(1));
        return g;
    }

    public Genero findById(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        if (rs.next()) return new Genero(rs.getInt(1), rs.getString(2));
        return null;
    }

    public List<Genero> findAll() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Genero> list = new ArrayList<>();
        while (rs.next()) list.add(new Genero(rs.getInt(1), rs.getString(2)));
        return list;
    }
}
