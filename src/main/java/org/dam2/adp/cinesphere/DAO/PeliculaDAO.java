package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO Pelicula(tituloPelicula, yearPelicula, ratingPelicula, duracionPelicula, nombreClasificacion) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM Pelicula WHERE idPelicula=?";

    private static final String SQL_FIND_ALL_LAZY =
            "SELECT idPelicula, tituloPelicula FROM Pelicula";

    private static final String SQL_FIND_BY_TITLE =
            "SELECT * FROM Pelicula WHERE LOWER(tituloPelicula) LIKE LOWER(?)";

    private static final String SQL_DELETE =
            "DELETE FROM Pelicula WHERE idPelicula=?";

    private final Connection conn = Conexion.getConnection();

    private final ClasificacionDAO clasificacionDAO = new ClasificacionDAO();
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private final MiListaDAO miListaDAO = new MiListaDAO();

    public Pelicula insert(Pelicula p) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

        st.setString(1, p.getTituloPelicula());
        st.setObject(2, p.getYearPelicula());
        st.setObject(3, p.getRatingPelicula());
        st.setObject(4, p.getDuracionPelicula());
        st.setString(5, p.getNombreClasificacion());

        st.executeUpdate();

        ResultSet keys = st.getGeneratedKeys();
        if (keys.next()) p.setIdPelicula(keys.getInt(1));

        return p;
    }

    public Pelicula findByIdLazy(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, id);

        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Pelicula p = new Pelicula();
            p.setIdPelicula(rs.getInt("idPelicula"));
            p.setTituloPelicula(rs.getString("tituloPelicula"));
            p.setYearPelicula(rs.getObject("yearPelicula") != null ? rs.getInt("yearPelicula") : null);
            p.setRatingPelicula(rs.getObject("ratingPelicula") != null ? rs.getDouble("ratingPelicula") : null);
            p.setDuracionPelicula(rs.getObject("duracionPelicula") != null ? rs.getInt("duracionPelicula") : null);
            p.setNombreClasificacion(rs.getString("nombreClasificacion"));
            return p;
        }
        return null;
    }

    public Pelicula findByIdEager(int idPelicula, int idUsuarioActual) throws SQLException {
        Pelicula p = findByIdLazy(idPelicula);
        if (p == null) return null;

        p.setClasificacion(clasificacionDAO.findById(p.getNombreClasificacion()));
        p.setDirectores(peliculaDirectorDAO.findByPelicula(idPelicula));
        p.setActores(peliculaActorDAO.findByPelicula(idPelicula));
        p.setGeneros(peliculaGeneroDAO.findByPelicula(idPelicula));

        MiLista ml = miListaDAO.find(idUsuarioActual, idPelicula);
        if (ml != null) p.setUsuariosQueLaTienen(List.of(ml));
        else p.setUsuariosQueLaTienen(new ArrayList<>());

        return p;
    }

    public List<Pelicula> findAllLazy() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL_LAZY);
        List<Pelicula> list = new ArrayList<>();

        while (rs.next()) {
            list.add(new Pelicula(
                    rs.getInt("idPelicula"),
                    rs.getString("tituloPelicula")
            ));
        }
        return list;
    }

    public List<Pelicula> searchByTitle(String title) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_TITLE);
        st.setString(1, "%" + title + "%");

        ResultSet rs = st.executeQuery();
        List<Pelicula> list = new ArrayList<>();

        while (rs.next()) {
            Pelicula p = new Pelicula();
            p.setIdPelicula(rs.getInt("idPelicula"));
            p.setTituloPelicula(rs.getString("tituloPelicula"));
            list.add(p);
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_DELETE);
        st.setInt(1, id);
        st.executeUpdate();
    }
}
