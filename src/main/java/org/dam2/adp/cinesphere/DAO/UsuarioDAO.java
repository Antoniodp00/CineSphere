package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Rol;
import org.dam2.adp.cinesphere.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final Connection conn = Conexion.getInstance().getConnection();
    private static final Logger logger = Logger.getLogger(UsuarioDAO.class.getName());

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
            if (u.getRol() == null) {
                u.setRol(Rol.USER);
            }
            st.setString(5, u.getRol().name());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setIdUsuario(keys.getInt(1));
                }
            }
            logger.log(Level.INFO, "Usuario insertado: " + u.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al insertar usuario: " + e.getMessage(), e);
            throw e;
        }
        return u;
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * @param u el usuario a actualizar.
     * @return el usuario actualizado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario update(Usuario u) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE)) {
            st.setString(1, u.getNombreUsuario());
            st.setString(2, u.getEmail());
            st.setString(3, u.getPassw());
            st.setDate(4, u.getBornDate() != null ? Date.valueOf(u.getBornDate()) : null);
            st.setString(5, u.getRol().name());
            st.setInt(6, u.getIdUsuario());
            st.executeUpdate();
            logger.log(Level.INFO, "Usuario actualizado: " + u.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar usuario: " + e.getMessage(), e);
            throw e;
        }
        return u;
    }

    /**
     * Elimina un usuario de la base de datos.
     * @param u El objeto Usuario a eliminar.
     * @return true si se eliminó correctamente, false si no se encontró.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Boolean delete(Usuario u) throws SQLException {
        boolean eliminado = false;
        try (PreparedStatement st = conn.prepareStatement(SQL_DELETE)) {
            st.setInt(1, u.getIdUsuario());
            // Si devuelve > 0, significa que borró al menos una fila
            eliminado = st.executeUpdate() > 0;
            if (eliminado) {
                logger.log(Level.INFO, "Usuario eliminado: " + u.toString());
            } else {
                logger.log(Level.WARNING, "No se encontró el usuario para eliminar: " + u.toString());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al eliminar usuario: " + e.getMessage(), e);
            throw e;
        }
        return eliminado;
    }

    /**
     * Busca un usuario por su ID.
     * @param id el ID del usuario a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findById(int id) throws SQLException {
        Usuario usuario = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null,
                            Rol.fromString(rs.getString("rol"))
                    );
                }
            }
            logger.log(Level.INFO, "Usuario encontrado por ID " + id + ": " + (usuario != null ? usuario.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar usuario por ID " + id + ": " + e.getMessage(), e);
            throw e;
        }
        return usuario;
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param name el nombre de usuario a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findByName(String name) throws SQLException {
        Usuario usuario = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null,
                            Rol.fromString(rs.getString("rol"))
                    );
                }
            }
            logger.log(Level.INFO, "Usuario encontrado por nombre '" + name + "': " + (usuario != null ? usuario.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar usuario por nombre '" + name + "': " + e.getMessage(), e);
            throw e;
        }
        return usuario;
    }

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email el correo electrónico a buscar.
     * @return el usuario encontrado, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Usuario findByEmail(String email) throws SQLException {
        Usuario usuario = null;
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("idusuario"),
                            rs.getString("nombreusuario"),
                            rs.getString("email"),
                            rs.getString("passw"),
                            rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null,
                            null,
                            Rol.fromString(rs.getString("rol"))
                    );
                }
            }
            logger.log(Level.INFO, "Usuario encontrado por email '" + email + "': " + (usuario != null ? usuario.toString() : "null"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar usuario por email '" + email + "': " + e.getMessage(), e);
            throw e;
        }
        return usuario;
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     * @return una lista de todos los usuarios.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idusuario"));
                u.setNombreUsuario(rs.getString("nombreusuario"));
                u.setEmail(rs.getString("email"));
                // No necesitamos la contraseña para el listado, por seguridad la dejamos null o vacía
                u.setBornDate(rs.getDate("borndate") != null ? rs.getDate("borndate").toLocalDate() : null);
                u.setRol(Rol.fromString(rs.getString("rol"))); // Convierte String DB -> Enum
                usuarios.add(u);
            }
            logger.log(Level.INFO, "Encontrados " + usuarios.size() + " usuarios.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar todos los usuarios: " + e.getMessage(), e);
            throw e;
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
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ROL)) {
            st.setString(1, nuevoRol.name());
            st.setInt(2, idUsuario);
            st.executeUpdate();
            logger.log(Level.INFO, "Rol actualizado a " + nuevoRol + " para el usuario con ID " + idUsuario);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar el rol para el usuario con ID " + idUsuario + ": " + e.getMessage(), e);
            throw e;
        }
    }

}
