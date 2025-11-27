package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la entidad Genero.
 */
public class GeneroDAO {

    private static final String SQL_INSERT = "INSERT INTO genero(nombregenero) VALUES(?)";
    private static final String SQL_FIND_BY_ID = "SELECT idgenero, nombregenero FROM genero WHERE idgenero=?";
    private static final String SQL_FIND_ALL = "SELECT idgenero, nombregenero FROM genero";
    private static final String SQL_FIND_BY_NAME = "SELECT idgenero, nombregenero FROM genero WHERE nombregenero=?";

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(GeneroDAO.class.getName());

    /**
     * Inserta un nuevo género en la base de datos.
     * @param g el género a insertar.
     * @return el género insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero insert(Genero g) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, g.getNombreGenero());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    g.setIdGenero(keys.getInt(1));
                }
            }
            logger.log(Level.INFO, "Genero insertado: " + g.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al insertar genero: " + e.getMessage(), e);
            throw e;
        }
        return g;
    }

    /**
     * Busca un género por su ID.
     * @param id el ID del género a buscar.
     * @return el género encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero findById(int id) throws SQLException {
        Genero genero = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    genero = new Genero(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Genero encontrado por ID " + id + ": " + (genero != null ? genero.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar genero por ID " + id + ": " + e.getMessage(), e);
            throw e;
        }
        return genero;
    }

    /**
     * Obtiene todos los géneros de la base de datos.
     * @return una lista de todos los géneros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Genero> findAll() throws SQLException {
        List<Genero> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                list.add(new Genero(rs.getInt(1), rs.getString(2)));
            }
            logger.log(Level.INFO, "Encontrados " + list.size() + " generos.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar todos los generos: " + e.getMessage(), e);
            throw e;
        }
        return list;
    }

    /**
     * Busca un género por su nombre.
     * @param name el nombre del género a buscar.
     * @return el género encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero findByName(String name) throws SQLException {
        Genero genero = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    genero = new Genero(rs.getInt(1), rs.getString(2));
                }
            }
            logger.log(Level.INFO, "Genero encontrado por nombre '" + name + "': " + (genero != null ? genero.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar genero por nombre '" + name + "': " + e.getMessage(), e);
            throw e;
        }
        return genero;
    }
}
