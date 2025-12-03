package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Clasificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Clasificacion.
 */
public class ClasificacionDAO {

    private static final String SQL_INSERT = "INSERT INTO clasificacion(nombreclasificacion) VALUES(?)";
    private static final String SQL_FIND_ALL = "SELECT nombreclasificacion FROM clasificacion";
    private static final String SQL_FIND_BY_ID = "SELECT nombreclasificacion FROM clasificacion WHERE nombreclasificacion=?";

    /**
     * Inserta una nueva clasificación en la base de datos.
     * @param c la clasificación a insertar.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void insert(Clasificacion c) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setString(1, c.getNombreClasificacion());
            st.executeUpdate();
        }
    }

    /**
     * Busca una clasificación por su ID.
     * @param id el ID de la clasificación a buscar.
     * @return la clasificación encontrada, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Clasificacion findById(String id) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setString(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Clasificacion(rs.getString(1));
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todas las clasificaciones de la base de datos.
     * @return una lista de todas las clasificaciones.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Clasificacion> findAll() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Clasificacion> listaClasificacion = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                listaClasificacion.add(new Clasificacion(rs.getString(1)));
            }
        }
        return listaClasificacion;
    }
}
