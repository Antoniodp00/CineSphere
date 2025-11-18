package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Director;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO director(nombredirector) VALUES(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT iddirector, nombredirector FROM director WHERE iddirector=?";

    private static final String SQL_FIND_ALL =
            "SELECT iddirector, nombredirector FROM director";

    private final Connection conn = Conexion.getConnection();

    public Director insert(Director d) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, d.getNombreDirector());
        st.executeUpdate();
        ResultSet keys = st.getGeneratedKeys();
        if (keys.next()) d.setIdDirector(keys.getInt(1));
        return d;
    }

    public Director findById(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        if (rs.next()) return new Director(rs.getInt(1), rs.getString(2));
        return null;
    }

    public List<Director> findAll() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Director> list = new ArrayList<>();
        while (rs.next()) list.add(new Director(rs.getInt(1), rs.getString(2)));
        return list;
    }
}
