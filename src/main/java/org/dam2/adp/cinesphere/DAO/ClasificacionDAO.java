package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Clasificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasificacionDAO {
    private static final String SQL_INSERT =
            "INSERT INTO Clasificacion(nombreClasificacion) VALUES(?)";

    private static final String SQL_FIND_ALL =
            "SELECT nombreClasificacion FROM Clasificacion";

    private static final String SQL_FIND_BY_ID =
            "SELECT nombreClasificacion FROM Clasificacion WHERE nombreClasificacion=?";


    private final Connection conn = Conexion.getConnection();

    public void insert(Clasificacion c) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setString(1, c.getNombreClasificacion());
        st.executeUpdate();
    }

    public Clasificacion findById(String id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setString(1, id);
        ResultSet rs = st.executeQuery();

        if (rs.next())
            return new Clasificacion(rs.getString("nombreClasificacion"));

        return null;
    }

    public List<Clasificacion> findAll() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Clasificacion> list = new ArrayList<>();

        while (rs.next())
            list.add(new Clasificacion(rs.getString("nombreClasificacion")));

        return list;
    }
}

