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

    private static final String SQL_FIND_PAGE = """
            SELECT idpelicula, titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion
            FROM pelicula
            ORDER BY idpelicula
            LIMIT ? OFFSET ?
            """;

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM pelicula";

    private PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();


    private final Connection conn = Conexion.getConnection();

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
            p.setRatingPelicula(rs.getDouble("ratingpelicula"));
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
        p.setRatingPelicula(rs.getDouble("ratingpelicula"));
        p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
        p.setNombreClasificacion(rs.getString("nombreclasificacion"));

        return p;
    }

    public Pelicula findByIdEager(int idPelicula) throws SQLException {
        Pelicula p = findByIdLazy(idPelicula);
        if (p == null) return null;

        p.setGeneros(peliculaGeneroDAO.findByPelicula(idPelicula));
        p.setActores(peliculaActorDAO.findByPelicula(idPelicula));
        p.setDirectores(peliculaDirectorDAO.findByPelicula(idPelicula));

        return p;
    }

    public List<Pelicula> findPage(int page, int pageSize) throws SQLException {
        int offset = (page - 1) * pageSize;

        PreparedStatement st = conn.prepareStatement(SQL_FIND_PAGE);
        st.setInt(1, pageSize);
        st.setInt(2, offset);

        ResultSet rs = st.executeQuery();
        List<Pelicula> lista = new ArrayList<>();

        while (rs.next()) {
            Pelicula p = new Pelicula();
            p.setIdPelicula(rs.getInt(1));
            p.setTituloPelicula(rs.getString(2));
            p.setYearPelicula(rs.getObject(3, Integer.class));
            p.setRatingPelicula(rs.getDouble(4));
            p.setDuracionPelicula(rs.getObject(5, Integer.class));
            p.setNombreClasificacion(rs.getString(6));
            lista.add(p);
        }

        return lista;
    }

    public int countPeliculas(Integer year, Double ratingMin, Integer idGenero) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT p.idpelicula) FROM pelicula p ");
        if (idGenero != null) {
            sql.append("LEFT JOIN peliculaGenero pg ON p.idpelicula = pg.idpelicula ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append("AND p.yearpelicula = ? ");
            params.add(year);
        }
        if (ratingMin != null) {
            sql.append("AND p.ratingpelicula >= ? ");
            params.add(ratingMin);
        }
        if (idGenero != null) {
            sql.append("AND pg.idgenero = ? ");
            params.add(idGenero);
        }

        PreparedStatement st = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }

        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public List<Pelicula> findFiltered(Integer year, Double ratingMin, Integer idGenero, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.* FROM pelicula p ");
        if (idGenero != null) {
            sql.append("LEFT JOIN peliculaGenero pg ON p.idpelicula = pg.idpelicula ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append("AND p.yearpelicula = ? ");
            params.add(year);
        }
        if (ratingMin != null) {
            sql.append("AND p.ratingpelicula >= ? ");
            params.add(ratingMin);
        }
        if (idGenero != null) {
            sql.append("AND pg.idgenero = ? ");
            params.add(idGenero);
        }

        sql.append("ORDER BY p.idpelicula LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        PreparedStatement st = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }

        ResultSet rs = st.executeQuery();
        List<Pelicula> lista = new ArrayList<>();
        while (rs.next()) {
            Pelicula p = new Pelicula();
            p.setIdPelicula(rs.getInt("idpelicula"));
            p.setTituloPelicula(rs.getString("titulopelicula"));
            p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
            p.setRatingPelicula(rs.getDouble("ratingpelicula"));
            p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
            p.setNombreClasificacion(rs.getString("nombreclasificacion"));
            lista.add(p);
        }
        return lista;
    }
}
