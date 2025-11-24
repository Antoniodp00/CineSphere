package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Usuario;

import java.sql.*;

/**
 * DAO para la entidad Usuario.
 */
public class UsuarioDAO {

    private static final String SQL_INSERT =
            "INSERT INTO usuario(nombreusuario, email, passw, borndate) VALUES(?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE usuario SET nombreusuario=?, email=?, passw=?, borndate=? WHERE idusuario=?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM usuario WHERE idusuario=?";

    private static final String SQL_FIND_BY_NAME =
            "SELECT * FROM usuario WHERE nombreusuario=?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT * FROM usuario WHERE email=?";

    private final Connection conn = Conexion.getInstance().getConnection();

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param u el usuario a insertar.
     * @return el usuario insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario insert(Usuario u) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, u.getNombreUsuario());
            st.setString(2, u.getEmail());
            st.setString(3, u.getPassw());
            st.setDate(4, u.getBornDate() != null ? Date.valueOf(u.getBornDate()) : null);
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setIdUsuario(rs.getInt(1));
                }
            }
        }
        return u;
    }

    public Usuario update(Usuario u) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE)) {
            st.setString(1, u.getNombreUsuario());
            st.setString(2, u.getEmail());
            st.setString(3, u.getPassw());
            st.setDate(4, u.getBornDate() != null ? Date.valueOf(u.getBornDate()) : null);
            st.setInt(5, u.getIdUsuario());
            st.executeUpdate();

            try(ResultSet rs = st.getGeneratedKeys()){
                if(rs.next()){
                    u.setIdUsuario(rs.getInt(1));
                }
            }
        }
        return u;
    }

    /**
     * Busca un usuario por su ID.
     * @param id el ID del usuario a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findById(int id) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null
                    );
                }
            }
        }
        return null;
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param name el nombre de usuario a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findByName(String name) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null
                    );
                }
            }
        }
        return null;
    }

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email el correo electrónico a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findByEmail(String email) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null
                    );
                }
            }
        }
        return null;
    }
}
