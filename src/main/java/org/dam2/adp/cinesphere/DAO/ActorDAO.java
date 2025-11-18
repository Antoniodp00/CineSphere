package org.dam2.adp.cinesphere.DAO;


import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActorDAO {

    private static final String SQL_INSERT =
            "INSERT INTO actor(nombreactor) VALUES(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT idactor, nombreactor FROM actor WHERE idactor=?";

    private static final String SQL_FIND_ALL =
            "SELECT idactor, nombreactor FROM actor";

    private final Connection conn = Conexion.getConnection();

    public Actor insert(Actor a) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, a.getNombreActor());
        st.executeUpdate();
        ResultSet keys = st.getGeneratedKeys();
        if (keys.next()) a.setIdActor(keys.getInt(1));
        return a;
    }

    public Actor findById(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        if (rs.next()) return new Actor(rs.getInt(1), rs.getString(2));
        return null;
    }

    public List<Actor> findAll() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Actor> list = new ArrayList<>();
        while (rs.next()) list.add(new Actor(rs.getInt(1), rs.getString(2)));
        return list;
    }
}
