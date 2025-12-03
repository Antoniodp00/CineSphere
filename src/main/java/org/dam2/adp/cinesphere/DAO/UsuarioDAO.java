package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Usuario.
 */
public class UsuarioDAO {

    private static final String SQL_INSERT =
            "INSERT INTO usuario(nombreusuario, email, passw, borndate, rol) VALUES(?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE usuario SET nombreusuario=?, email=?, passw=?, borndate=?, rol=? WHERE idusuario=?";

    private static final String SQL_DELETE =
            "DELETE FROM usuario WHERE idusuario=?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM usuario WHERE idusuario=?";

    private static final String SQL_FIND_BY_NAME =
            "SELECT * FROM usuario WHERE nombreusuario=?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT * FROM usuario WHERE email=?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM usuario ORDER BY idusuario";
    private static final String SQL_UPDATE_ROL =
            "UPDATE usuario SET rol=? WHERE idusuario=?";


    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario el usuario a insertar.
     * @return el usuario insertado con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario insert(Usuario usuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, usuario.getNombreUsuario());
            st.setString(2, usuario.getEmail());
            st.setString(3, usuario.getPassw());
            st.setDate(4, usuario.getBornDate() != null ? Date.valueOf(usuario.getBornDate()) : null);
            if (usuario.getRol() == null) {
                usuario.setRol(Rol.USER);
            }
            st.setString(5, usuario.getRol().name());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setIdUsuario(keys.getInt(1));
                }
            }
        }
        return usuario;
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * @param usuario el usuario a actualizar.
     * @return el usuario actualizado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario update(Usuario usuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE)) {
            st.setString(1, usuario.getNombreUsuario());
            st.setString(2, usuario.getEmail());
            st.setString(3, usuario.getPassw());
            st.setDate(4, usuario.getBornDate() != null ? Date.valueOf(usuario.getBornDate()) : null);
            st.setString(5, usuario.getRol().name());
            st.setInt(6, usuario.getIdUsuario());
            st.executeUpdate();
        }
        return usuario;
    }

    /**
     * Elimina un usuario de la base de datos.
     * @param u El objeto Usuario a eliminar.
     * @return true si se eliminó correctamente, false si no se encontró.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public boolean delete(Usuario u) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_DELETE)) {
            st.setInt(1, u.getIdUsuario());
            return st.executeUpdate() > 0;
        }
    }

    /**
     * Busca un usuario por su ID.
     * @param id el ID del usuario a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findById(int id) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoUsuario(rs);
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
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoUsuario(rs);
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
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapeoUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     * @return una lista de todos los usuarios.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Usuario> findAll() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Usuario> usuarios = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                usuarios.add(mapeoUsuario(rs));
            }
        }
        return usuarios;
    }

    /**
     * Actualiza el rol de un usuario por su ID.
     * @param idUsuario el ID del usuario a actualizar.
     * @param nuevoRol el nuevo rol del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void updateRol(int idUsuario, Rol nuevoRol) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ROL)) {
            st.setString(1, nuevoRol.name());
            st.setInt(2, idUsuario);
            st.executeUpdate();
        }
    }

    /**
     * Mapea una fila de un ResultSet a un objeto Usuario.
     * @param rs el ResultSet del que obtener los datos.
     * @return un objeto Usuario con los datos de la fila.
     * @throws SQLException si ocurre un error al acceder a los datos del ResultSet.
     */
    private Usuario mapeoUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idusuario"));
        usuario.setNombreUsuario(rs.getString("nombreusuario"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassw(rs.getString("passw"));
        Date bornDate = rs.getDate("borndate");
        if (bornDate != null) {
            usuario.setBornDate(bornDate.toLocalDate());
        }
        usuario.setRol(Rol.fromString(rs.getString("rol")));
        return usuario;
    }
}
