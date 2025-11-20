package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MiListaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO milista(idusuario, idpelicula, estado, puntuacion, urlimg, fecha_anadido) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND =
            "SELECT * FROM milista WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_FIND_BY_USER = "SELECT idpelicula FROM milista WHERE idusuario=?";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE milista SET estado=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_UPDATE_PUNTUACION = "UPDATE milista SET puntuacion=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_DELETE =
            "DELETE FROM milista WHERE idusuario=? AND idpelicula=?";

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
        st.setObject(6, ml.getFechaAnadido());
        st.executeUpdate();
    }

    public MiLista find(int idUsuario, int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND);
        st.setInt(1, idUsuario);
        st.setInt(2, idPelicula);
        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            Usuario u = usuarioDAO.findById(idUsuario);
            Pelicula p = peliculaDAO.findByIdLazy(idPelicula);

            return new MiLista(
                    p,
                    u,
                    PeliculaEstado.fromString(rs.getString("estado")),
                    rs.getObject("puntuacion") != null ? rs.getInt("puntuacion") : null,
                    rs.getString("urlimg"),
                    rs.getObject("fecha_anadido", LocalDateTime.class)
            );
        }
        return null;
    }

    public List<Pelicula> findPeliculasByUsuario(int idUsuario) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_USER);
        st.setInt(1, idUsuario);
        ResultSet rs = st.executeQuery();

        List<Pelicula> misPeliculas = new ArrayList<>();
        while (rs.next()) {
            int idPelicula = rs.getInt("idpelicula");
            Pelicula p = peliculaDAO.findByIdLazy(idPelicula);
            if (p != null) {
                misPeliculas.add(p);
            }
        }
        return misPeliculas;
    }

    public void updateEstado(int idUsuario, int idPelicula, PeliculaEstado estado) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ESTADO);
        st.setString(1, estado.getDisplayValue());
        st.setInt(2, idUsuario);
        st.setInt(3, idPelicula);
        st.executeUpdate();
    }

    public void updatePuntuacion(int idUsuario, int idPelicula, int puntuacion) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_UPDATE_PUNTUACION);
        st.setInt(1, puntuacion);
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
