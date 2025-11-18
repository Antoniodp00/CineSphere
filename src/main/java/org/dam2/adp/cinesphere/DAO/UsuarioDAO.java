package org.dam2.adp.cinesphere.DAO;



import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.Usuario;

import java.sql.*;
import java.time.LocalDate;

public class UsuarioDAO {


    private static final String SQL_INSERT =
            "INSERT INTO Usuario(nombreUsuario, email, passw, bornDate) VALUES(?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM Usuario WHERE idUsuario = ?";

    private static final String SQL_FIND_BY_NAME =
            "SELECT * FROM Usuario WHERE nombreUsuario = ?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT * FROM Usuario WHERE email = ?";


    private final Connection conn = Conexion.getConnection();

    public Usuario insert(Usuario u) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

        st.setString(1, u.getNombreUsuario());
        st.setString(2, u.getEmail());
        st.setString(3, u.getPassw());
        st.setDate(4, u.getBornDate() != null ? Date.valueOf(u.getBornDate()) : null);

        st.executeUpdate();

        ResultSet keys = st.getGeneratedKeys();
        if (keys.next()) u.setIdUsuario(keys.getInt(1));

        return u;
    }

    public Usuario findById(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, id);

        ResultSet rs = st.executeQuery();
        if (rs.next())
            return new Usuario(
                    rs.getInt("idUsuario"),
                    rs.getString("nombreUsuario"),
                    rs.getString("email"),
                    rs.getString("passw"),
                    rs.getDate("bornDate") != null ? rs.getDate("bornDate").toLocalDate() : null,
                    null // Lista de MiLista (EAGER) opcional
            );

        return null;
    }

    public Usuario findByName(String name) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_NAME);
        st.setString(1, name);

        ResultSet rs = st.executeQuery();
        if (rs.next())
            return new Usuario(
                    rs.getInt("idUsuario"),
                    rs.getString("nombreUsuario"),
                    rs.getString("email"),
                    rs.getString("passw"),
                    rs.getDate("bornDate") != null ? rs.getDate("bornDate").toLocalDate() : null,
                    null
            );

        return null;
    }

    public Usuario findByEmail(String email) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_EMAIL);
        st.setString(1, email);

        ResultSet rs = st.executeQuery();
        if (rs.next())
            return new Usuario(
                    rs.getInt("idUsuario"),
                    rs.getString("nombreUsuario"),
                    rs.getString("email"),
                    rs.getString("passw"),
                    rs.getDate("bornDate") != null ? rs.getDate("bornDate").toLocalDate() : null,
                    null
            );

        return null;
    }
}

