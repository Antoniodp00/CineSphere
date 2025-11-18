package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO pelicula(titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT idpelicula, titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion " +
                    "FROM pelicula";

    private static final String SQL_FIND_BY_ID =
            "SELECT idpelicula, titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion " +
                    "FROM pelicula WHERE idpelicula=?";

    private final Connection conn = Conexion.getConnection();

    private final DirectorDAO directorDAO = new DirectorDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
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

    public List<Pelicula> findAllLazy() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(SQL_FIND_ALL);
        List<Pelicula> list = new ArrayList<>();

        while (rs.next()) {
            Pelicula p = new Pelicula();
            p.setIdPelicula(rs.getInt("idpelicula"));
            p.setTituloPelicula(rs.getString("titulopelicula"));
            p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
            p.setRatingPelicula(rs.getObject("ratingpelicula", Double.class));
            p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
            p.setNombreClasificacion(rs.getString("nombreclasificacion"));
            list.add(p);
        }
        return list;
    }

    public Pelicula findByIdLazy(int idPelicula) throws SQLException {
        PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID);
        st.setInt(1, idPelicula);
        ResultSet rs = st.executeQuery();

        if (!rs.next()) return null;

        Pelicula p = new Pelicula();
        p.setIdPelicula(rs.getInt("idpelicula"));
        p.setTituloPelicula(rs.getString("titulopelicula"));
        p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
        p.setRatingPelicula(rs.getObject("ratingpelicula", Double.class));
        p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
        p.setNombreClasificacion(rs.getString("nombreclasificacion"));

        return p;
    }

    public Pelicula findByIdEager(int idPelicula, int idUsuarioActual) throws SQLException {
        Pelicula p = findByIdLazy(idPelicula);
        if (p == null) return null;

        if (p.getNombreClasificacion() != null)
            p.setClasificacion(clasificacionDAO.findById(p.getNombreClasificacion()));

        p.setDirectores(peliculaDirectorDAO.findByPelicula(idPelicula));
        p.setActores(peliculaActorDAO.findByPelicula(idPelicula));
        p.setGeneros(peliculaGeneroDAO.findByPelicula(idPelicula));

        MiLista ml = miListaDAO.find(idUsuarioActual, idPelicula);
        p.setUsuariosQueLaTienen((List<MiLista>) ml);


        return p;
    }
}
