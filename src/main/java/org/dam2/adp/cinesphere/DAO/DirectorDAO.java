package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Director;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la entidad Director.
 */
public class DirectorDAO {

    private static final String SQL_INSERT = "INSERT INTO director(nombredirector) VALUES(?)";
    private static final String SQL_FIND_BY_ID = "SELECT iddirector, nombredirector FROM director WHERE iddirector=?";
    private static final String SQL_FIND_ALL = "SELECT iddirector, nombredirector FROM director";
    private static final String SQL_FIND_BY_NAME = "SELECT iddirector, nombredirector FROM director WHERE nombredirector=?";

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(DirectorDAO.class.getName());

    /**
     * Inserta un nuevo director en la base de datos.
     * @param d el director a insertar.
     * @return el director insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Director insert(Director d) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, d.getNombreDirector());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    d.setIdDirector(keys.getInt(1));
                }
            }
            logger.log(Level.INFO, "Director insertado: " + d.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al insertar director: " + e.getMessage(), e);
            throw e;
        }
        return d;
    }

    /**
     * Busca un director por su ID.
     * @param id el ID del director a buscar.
     * @return el director encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Director findById(int id) throws SQLException {
        Director director = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    director = new Director(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Director encontrado por ID " + id + ": " + (director != null ? director.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar director por ID " + id + ": " + e.getMessage(), e);
            throw e;
        }
        return director;
    }

    /**
     * Obtiene todos los directores de la base de datos.
     * @return una lista de todos los directores.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Director> findAll() throws SQLException {
        List<Director> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                list.add(new Director(rs.getInt(1), rs.getString(2)));
            }
            logger.log(Level.INFO, "Encontrados " + list.size() + " directores.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar todos los directores: " + e.getMessage(), e);
            throw e;
        }
        return list;
    }

    /**
     * Busca un director por su nombre.
     * @param name el nombre del director a buscar.
     * @return el director encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Director findByName(String name) throws SQLException {
        Director director = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    director = new Director(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Director encontrado por nombre '" + name + "': " + (director != null ? director.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar director por nombre '" + name + "': " + e.getMessage(), e);
            throw e;
        }
        return director;
    }
}
