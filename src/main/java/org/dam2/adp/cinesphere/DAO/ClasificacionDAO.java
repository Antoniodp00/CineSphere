package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Clasificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasificacionDAO {

    private static final String SQL_INSERT =
            "INSERT INTO clasificacion(nombreclasificacion) VALUES(?)";

    private static final String SQL_FIND_ALL =
            "SELECT nombreclasificacion FROM clasificacion";

    private static final String SQL_FIND_BY_ID =
            "SELECT nombreclasificacion FROM clasificacion WHERE nombreclasificacion=?";

    private final Connection conn = Conexion.getInstance().getConnection();

    public void insert(Clasificacion c) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setString(1, c.getNombreClasificacion());
        st.executeUpdate();
    }

    public Clasificacion findById(String id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setString(1, id);
        ResultSet rs = st.executeQuery();
        if (rs.next()) return new Clasificacion(rs.getString(1));
        return null;
    }

    public List<Clasificacion> findAll() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Clasificacion> list = new ArrayList<>();
        while (rs.next()) list.add(new Clasificacion(rs.getString(1)));
        return list;
    }
}
