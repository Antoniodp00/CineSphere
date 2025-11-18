package org.dam2.adp.cinesphere.DAO;



import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActorDAO {


    private static final String SQL_INSERT =
            "INSERT INTO Actor(nombreActor) VALUES(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT idActor, nombreActor FROM Actor WHERE idActor=?";

    private static final String SQL_FIND_ALL =
            "SELECT idActor, nombreActor FROM Actor";


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
        if (rs.next())
            return new Actor(
                    rs.getInt("idActor"),
                    rs.getString("nombreActor")
            );

        return null;
    }

    public List<Actor> findAll() throws SQLException {
        List<Actor> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);

        while (rs.next())
            list.add(new Actor(
                    rs.getInt("idActor"),
                    rs.getString("nombreActor")
            ));

        return list;
    }
}

