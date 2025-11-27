package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Actor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la entidad Actor.
 */
public class ActorDAO {

    private static final String SQL_INSERT = "INSERT INTO actor(nombreactor) VALUES(?)";
    private static final String SQL_FIND_BY_ID = "SELECT idactor, nombreactor FROM actor WHERE idactor=?";
    private static final String SQL_FIND_ALL = "SELECT idactor, nombreactor FROM actor";
    private static final String SQL_FIND_BY_NAME = "SELECT idactor, nombreactor FROM actor WHERE nombreactor=?";

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(ActorDAO.class.getName());

    /**
     * Inserta un nuevo actor en la base de datos.
     * @param a el actor a insertar.
     * @return el actor insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Actor insert(Actor a) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, a.getNombreActor());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setIdActor(keys.getInt(1));
                }
            }
            logger.log(Level.INFO, "Actor insertado: " + a.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al insertar actor: " + e.getMessage(), e);
            throw e;
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
        Actor actor = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    actor = new Actor(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Actor encontrado por ID " + id + ": " + (actor != null ? actor.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar actor por ID " + id + ": " + e.getMessage(), e);
            throw e;
        }
        return actor;
    }

    /**
     * Obtiene todos los actores de la base de datos.
     * @return una lista de todos los actores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Actor> findAll() throws SQLException {
        List<Actor> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                list.add(new Actor(rs.getInt(1), rs.getString(2)));
            }
            logger.log(Level.INFO, "Encontrados " + list.size() + " actores.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar todos los actores: " + e.getMessage(), e);
            throw e;
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
        Actor actor = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    actor = new Actor(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Actor encontrado por nombre '" + name + "': " + (actor != null ? actor.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar actor por nombre '" + name + "': " + e.getMessage(), e);
            throw e;
        }
        return null;
    }
}
