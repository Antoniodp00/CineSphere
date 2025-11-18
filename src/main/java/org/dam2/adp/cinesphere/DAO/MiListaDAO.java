package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.time.LocalDateTime;

public class MiListaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO MiLista(idUsuario, idPelicula, estado, puntuacion, urlImg, fecha_anadido) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND =
            "SELECT * FROM MiLista WHERE idUsuario=? AND idPelicula=?";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE MiLista SET estado=? WHERE idUsuario=? AND idPelicula=?";

    private static final String SQL_DELETE =
            "DELETE FROM MiLista WHERE idUsuario=? AND idPelicula=?";

    private static final String SQL_FIND_BY_USUARIO =
            "SELECT idPelicula FROM MiLista WHERE idUsuario=?";

    private final Connection conn = Conexion.getConnection();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();

    public void insert(MiLista ml) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT);
        st.setInt(1, ml.getUsuario().getIdUsuario());
        st.setInt(2, ml.getPelicula().getIdPelicula());
        st.setString(3, ml.getEstado() != null ? ml.getEstado().getDisplayValue() : null);
        st.setObject(4, ml.getPuntuacion());
        st.setString(5, ml.getUrlImg());
        st.setString(6, ml.getFechaAnadido().toString());
        st.executeUpdate();
    }

    public MiLista find(int idUsuario, int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND);
        st.setInt(1, idUsuario);
        st.setInt(2, idPelicula);

        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            Usuario usuario = usuarioDAO.findById(idUsuario);
            Pelicula pelicula = peliculaDAO.findByIdEager(idPelicula, idUsuario);

            return new MiLista(
                    pelicula,
                    usuario,
                    PeliculaEstado.fromString(rs.getString("estado")),
                    rs.getObject("puntuacion") != null ? rs.getInt("puntuacion") : null,
                    rs.getString("urlImg"),
                    LocalDateTime.parse(rs.getString("fecha_anadido"))
            );
        }
        return null;
    }

    public void updateEstado(int idUsuario, int idPelicula, PeliculaEstado estado) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ESTADO);
        st.setString(1, estado.getDisplayValue());
        st.setInt(2, idUsuario);
        st.setInt(3, idPelicula);
        st.executeUpdate();
    }

    public void delete(int idUsuario, int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_DELETE);
        st.setInt(1, idUsuario);
        st.setInt(2, idPelicula);
        st.executeUpdate();
    }
}
