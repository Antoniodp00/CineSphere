package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String SQL_FIND_GENEROS_LOTE = """
            SELECT pg.idpelicula, g.idgenero, g.nombregenero
            FROM peliculagenero pg
            JOIN genero g ON pg.idgenero = g.idgenero
            WHERE pg.idpelicula IN (%s)
            """;
    private static final String SQL_COUNT_BASE = "SELECT COUNT(DISTINCT p.idpelicula) FROM pelicula p ";
    private static final String SQL_FILTER_SELECT = "SELECT DISTINCT p.* FROM pelicula p ";
    private static final String SQL_FILTER_PAGINATION = "ORDER BY p.idpelicula LIMIT ? OFFSET ?";


    private final PeliculaGeneroDAO peliculaGeneroDAO = new PeliculaGeneroDAO();
    private final PeliculaActorDAO peliculaActorDAO = new PeliculaActorDAO();
    private final PeliculaDirectorDAO peliculaDirectorDAO = new PeliculaDirectorDAO();


    /**
     * Inserta una nueva película en la base de datos.
     *
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
            st.setString(5, p.getClasificacion().getNombreClasificacion());
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
     *
     * @return una lista de todas las películas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findAllLazy() throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                Pelicula p = mapeoPelicula(rs);
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Busca una película por su ID (carga perezosa).
     *
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
                    Pelicula p = mapeoPelicula(rs);
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Busca una película por su título y año.
     *
     * @param titulo el título de la película.
     * @param year   el año de la película.
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
                    Pelicula p = mapeoPelicula(rs);
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Busca una película por su ID (carga ansiosa).
     *
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
     *
     * @param page     el número de página a obtener.
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
                    Pelicula p = mapeoPelicula(rs);
                    lista.add(p);
                }
            }
        }
        cargarGenerosEnLote(lista);

        return lista;
    }

    /**
     * Cuenta el número total de películas que coinciden con los filtros especificados.
     *
     * @param year         el año de la película.
     * @param ratingMin    el rating mínimo de la película.
     * @param idGenero     el ID del género de la película.
     * @param filtroTitulo el título a buscar.
     * @return el número total de películas que coinciden con los filtros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countPeliculas(Integer year, Double ratingMin, Integer idGenero, String filtroTitulo) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();

        FiltroContexto contexto = construirCondicionesFiltro(year, ratingMin, idGenero, filtroTitulo);
        String sql = SQL_COUNT_BASE + contexto.sqlPart;

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            asignarParametros(st, contexto.params);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Busca y devuelve una lista paginada de películas aplicando filtros dinámicos.
     *
     * @param year         El año para filtrar (opcional).
     * @param ratingMin    El rating mínimo para filtrar (opcional).
     * @param idGenero     El ID del género para filtrar (opcional).
     * @param filtroTitulo El término de búsqueda para el título (opcional).
     * @param page         El número de página para la paginación.
     * @param pageSize     El tamaño de cada página.
     * @return Una lista de objetos {@link Pelicula} que coinciden con los criterios.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findFiltered(Integer year, Double ratingMin, Integer idGenero, String filtroTitulo, int page, int pageSize) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> lista = new ArrayList<>();

        FiltroContexto contexto = construirCondicionesFiltro(year, ratingMin, idGenero, filtroTitulo);
        String sql = SQL_FILTER_SELECT + contexto.sqlPart + SQL_FILTER_PAGINATION;

        contexto.params.add(pageSize);
        contexto.params.add((page - 1) * pageSize);

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            asignarParametros(st, contexto.params);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapeoPelicula(rs));
                }
            }
        }

        cargarGenerosEnLote(lista);
        return lista;
    }

    /**
     * Centraliza la lógica de construcción de las cláusulas SQL dinámicas (JOIN y WHERE) para los filtros.
     *
     * @param year         El año para filtrar.
     * @param ratingMin    El rating mínimo para filtrar.
     * @param idGenero     El ID del género para filtrar.
     * @param filtroTitulo El término de búsqueda para el título.
     * @return Un objeto {@link FiltroContexto} que contiene el fragmento de SQL y la lista de parámetros.
     */
    private FiltroContexto construirCondicionesFiltro(Integer year, Double ratingMin, Integer idGenero, String filtroTitulo) {
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (idGenero != null) {
            sqlBuilder.append("JOIN peliculagenero pg ON p.idpelicula = pg.idpelicula ");
        }

        List<String> conditions = new ArrayList<>();

        if (year != null) {
            conditions.add("p.yearpelicula = ?");
            params.add(year);
        }
        if (ratingMin != null) {
            conditions.add("p.ratingpelicula >= ?");
            params.add(ratingMin);
        }
        if (idGenero != null) {
            conditions.add("pg.idgenero = ?");
            params.add(idGenero);
        }
        if (filtroTitulo != null && !filtroTitulo.isBlank()) {
            conditions.add("p.titulopelicula ILIKE ?");
            params.add("%" + filtroTitulo + "%");
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return new FiltroContexto(sqlBuilder.toString(), params);
    }

    /**
     * Asigna una lista de parámetros a un PreparedStatement.
     *
     * @param st     El PreparedStatement al que se le asignarán los parámetros.
     * @param params La lista de parámetros a asignar.
     * @throws SQLException si ocurre un error al asignar un parámetro.
     */
    private void asignarParametros(PreparedStatement st, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Clase interna para encapsular el resultado de la construcción de filtros:
     * el fragmento de SQL y la lista de parámetros correspondiente.
     */
    private static class FiltroContexto {
        String sqlPart;
        List<Object> params;

        public FiltroContexto(String sqlPart, List<Object> params) {
            this.sqlPart = sqlPart;
            this.params = params;
        }
    }

    /**
     * Carga los géneros de una lista de películas en un solo lote para optimizar las consultas a la base de datos.
     *
     * @param peliculas la lista de películas a la que se le cargarán los géneros.
     */
    public void cargarGenerosEnLote(List<Pelicula> peliculas) {
        if (peliculas.isEmpty()) return;
        Connection conn = Conexion.getInstance().getConnection();

        Map<Integer, Pelicula> peliculasPorId = peliculas.stream()
                .collect(Collectors.toMap(Pelicula::getIdPelicula, p -> p));

        List<Integer> ids = new ArrayList<>(peliculasPorId.keySet());

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sqlFinal = String.format(SQL_FIND_GENEROS_LOTE, placeholders);

        try {

            try (PreparedStatement ps = conn.prepareStatement(sqlFinal)) {

                for (int i = 0; i < ids.size(); i++) {
                    ps.setInt(i + 1, ids.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idPelicula = rs.getInt("idpelicula");
                        Pelicula pelicula = peliculasPorId.get(idPelicula);

                        if (pelicula != null) {
                            if (pelicula.getGeneros() == null) {
                                pelicula.setGeneros(new ArrayList<>());
                            }
                            Genero genero = new Genero();
                            genero.setIdGenero(rs.getInt("idgenero"));
                            genero.setNombreGenero(rs.getString("nombregenero"));
                            pelicula.getGeneros().add(genero);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mapea una fila de un ResultSet a un objeto Pelicula.
     *
     * @param rs el ResultSet del que obtener los datos.
     * @return un objeto Pelicula con los datos de la fila.
     * @throws SQLException si ocurre un error al acceder a los datos del ResultSet.
     */
    public Pelicula mapeoPelicula(ResultSet rs) throws SQLException {
        Pelicula p = new Pelicula();
        p.setIdPelicula(rs.getInt("idpelicula"));
        p.setTituloPelicula(rs.getString("titulopelicula"));
        p.setYearPelicula(rs.getObject("yearpelicula", Integer.class));
        p.setRatingPelicula(rs.getDouble("ratingpelicula"));
        p.setDuracionPelicula(rs.getObject("duracionpelicula", Integer.class));

        Clasificacion c = new Clasificacion();
        c.setNombreClasificacion(rs.getString("nombreclasificacion"));
        p.setClasificacion(c);

        return p;
    }
}
