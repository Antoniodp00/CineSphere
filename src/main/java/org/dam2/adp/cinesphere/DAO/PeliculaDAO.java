package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Pelicula.
 */
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

    private static final String SQL_FIND_BY_TITULO_AND_YEAR =
            "SELECT idpelicula, titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion " +
                    "FROM pelicula WHERE titulopelicula=? AND yearpelicula=?";

    private static final String SQL_FIND_PAGE = """
            SELECT idpelicula, titulopelicula, yearpelicula, ratingpelicula, duracionpelicula, nombreclasificacion
            FROM pelicula
            ORDER BY idpelicula
            LIMIT ? OFFSET ?
            """;
    private static final String SQL_DELETE =
            "DELETE FROM pelicula WHERE idpelicula=?";
    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM pelicula";

    private PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();




    /**
     * Inserta una nueva película en la base de datos.
     * @param p la película a insertar.
     * @return la película insertada con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Pelicula insert(Pelicula p) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, p.getTituloPelicula());
            st.setObject(2, p.getYearPelicula());
            st.setObject(3, p.getRatingPelicula());
            st.setObject(4, p.getDuracionPelicula());
            st.setString(5, p.getNombreClasificacion());
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setIdPelicula(keys.getInt(1));
                }
            }
        }
        return p;
    }

    /**
     * Elimina una película de la base de datos.
     *
     * @param idPelicula el ID de la película a eliminar.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void delete(int idPelicula) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_DELETE)) {
            st.setInt(1, idPelicula);
            st.executeUpdate();
        }
    }

    /**
     * Obtiene todas las películas de la base de datos (carga perezosa).
     * @return una lista de todas las películas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findAllLazy() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                Pelicula p = new Pelicula();
                p.setIdPelicula(rs.getInt("idpelicula"));
                p.setTituloPelicula(rs.getString("titulopelicula"));
                p.setYearPelicula(rs.getInt("yearpelicula"));
                p.setRatingPelicula(rs.getDouble("ratingpelicula"));
                p.setDuracionPelicula(rs.getInt("duracionpelicula"));
                p.setNombreClasificacion(rs.getString("nombreclasificacion"));
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Busca una película por su ID (carga perezosa).
     * @param idPelicula el ID de la película a buscar.
     * @return la película encontrada, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Pelicula findByIdLazy(int idPelicula) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_ID)) {
            st.setInt(1, idPelicula);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Pelicula p = new Pelicula();
                    p.setIdPelicula(rs.getInt("idpelicula"));
                    p.setTituloPelicula(rs.getString("titulopelicula"));
                    p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
                    p.setRatingPelicula(rs.getDouble("ratingpelicula"));
                    p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
                    p.setNombreClasificacion(rs.getString("nombreclasificacion"));
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Busca una película por su título y año.
     * @param titulo el título de la película.
     * @param year el año de la película.
     * @return la película encontrada, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Pelicula findByTituloAndYear(String titulo, int year) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_TITULO_AND_YEAR)) {
            st.setString(1, titulo);
            st.setInt(2, year);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Pelicula p = new Pelicula();
                    p.setIdPelicula(rs.getInt("idpelicula"));
                    p.setTituloPelicula(rs.getString("titulopelicula"));
                    p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
                    p.setRatingPelicula(rs.getDouble("ratingpelicula"));
                    p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));
                    p.setNombreClasificacion(rs.getString("nombreclasificacion"));
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Busca una película por su ID (carga ansiosa).
     * @param idPelicula el ID de la película a buscar.
     * @return la película encontrada, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Pelicula findByIdEager(int idPelicula) throws SQLException {
        Pelicula p = findByIdLazy(idPelicula);
        if (p == null) return null;

        p.setGeneros(peliculaGeneroDAO.findByPelicula(idPelicula));
        p.setActores(peliculaActorDAO.findByPelicula(idPelicula));
        p.setDirectores(peliculaDirectorDAO.findByPelicula(idPelicula));

        return p;
    }

    /**
     * Obtiene una página de películas de la base de datos.
     * @param page el número de página a obtener.
     * @param pageSize el tamaño de la página.
     * @return una lista de películas de la página especificada.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findPage(int page, int pageSize) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> lista = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_PAGE)) {
            st.setInt(1, pageSize);
            st.setInt(2, offset);
            try (ResultSet rs = st.executeQuery()) {
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
            }
        }
        return lista;
    }

    /**
     * Cuenta el número total de películas que coinciden con los filtros especificados.
     * @param year el año de la película.
     * @param ratingMin el rating mínimo de la película.
     * @param idGenero el ID del género de la película.
     * @return el número total de películas que coinciden con los filtros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countPeliculas(Integer year, Double ratingMin, Integer idGenero) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
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

        try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Obtiene una lista paginada de películas que coinciden con los filtros especificados.
     * @param year el año de la película.
     * @param ratingMin el rating mínimo de la película.
     * @param idGenero el ID del género de la película.
     * @param page el número de página a obtener.
     * @param pageSize el tamaño de la página.
     * @return una lista de películas que coinciden con los filtros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findFiltered(Integer year, Double ratingMin, Integer idGenero, int page, int pageSize) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> lista = new ArrayList<>();
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

        try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
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
            }
        }
        return lista;
    }
}
