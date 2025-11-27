package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Actor.
 */
public class ActorDAO {

    private static final String SQL_INSERT = "INSERT INTO actor(nombreactor) VALUES(?)";
    private static final String SQL_FIND_BY_ID = "SELECT idactor, nombreactor FROM actor WHERE idactor=?";
    private static final String SQL_FIND_ALL = "SELECT idactor, nombreactor FROM actor";
    private static final String SQL_FIND_BY_NAME = "SELECT idactor, nombreactor FROM actor WHERE nombreactor=?";



    /**
     * Inserta un nuevo actor en la base de datos.
     * @param a el actor a insertar.
     * @return el actor insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Actor insert(Actor a) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, a.getNombreActor());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setIdActor(keys.getInt(1));
                }
            }
        }
        return a;
    }

    /**
     * Busca un actor por su ID.
     * @param id el ID del actor a buscar.
     * @return el actor encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Actor findById(int id) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoActor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todos los actores de la base de datos.
     * @return una lista de todos los actores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Actor> findAll() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Actor> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                list.add(mapeoActor(rs));
            }
        }
        return list;
    }

    /**
     * Busca un actor por su nombre.
     * @param name el nombre del actor a buscar.
     * @return el actor encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Actor findByName(String name) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoActor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mapea una fila de un ResultSet a un objeto Actor.
     * @param rs el ResultSet del que obtener los datos.
     * @return un objeto Actor con los datos de la fila.
     * @throws SQLException si ocurre un error al acceder a los datos del ResultSet.
     */
    private Actor mapeoActor(ResultSet rs) throws SQLException {
        return new Actor(
                rs.getInt("idactor"),
                rs.getString("nombreactor")
        );
    }
}
