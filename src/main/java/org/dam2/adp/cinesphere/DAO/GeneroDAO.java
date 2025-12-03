package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Genero.
 */
public class GeneroDAO {

    private static final String SQL_INSERT = "INSERT INTO genero(nombregenero) VALUES(?)";
    private static final String SQL_FIND_BY_ID = "SELECT idgenero, nombregenero FROM genero WHERE idgenero=?";
    private static final String SQL_FIND_ALL = "SELECT idgenero, nombregenero FROM genero";
    private static final String SQL_FIND_BY_NAME = "SELECT idgenero, nombregenero FROM genero WHERE nombregenero=?";


    /**
     * Inserta un nuevo género en la base de datos.
     * @param genero el género a insertar.
     * @return el género insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero insert(Genero genero) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, genero.getNombreGenero());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    genero.setIdGenero(keys.getInt(1));
                }
            }
        }
        return genero;
    }

    /**
     * Busca un género por su ID.
     * @param id el ID del género a buscar.
     * @return el género encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero findById(int id) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoGenero(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todos los géneros de la base de datos.
     * @return una lista de todos los géneros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Genero> findAll() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Genero> listaGenero = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                listaGenero.add(mapeoGenero(rs));
            }
        }
        return listaGenero;
    }

    /**
     * Busca un género por su nombre.
     * @param name el nombre del género a buscar.
     * @return el género encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Genero findByName(String name) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoGenero(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mapea una fila de un ResultSet a un objeto Genero.
     * @param rs el ResultSet del que obtener los datos.
     * @return un objeto Genero con los datos de la fila.
     * @throws SQLException si ocurre un error al acceder a los datos del ResultSet.
     */
    private Genero mapeoGenero(ResultSet rs) throws SQLException {
        return new Genero(
                rs.getInt("idgenero"),
                rs.getString("nombregenero")
        );
    }
}
