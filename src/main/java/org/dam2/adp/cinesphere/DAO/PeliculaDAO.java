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
     * @param pelicula la película a insertar.
     * @return la película insertada con su ID generado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Pelicula insert(Pelicula pelicula) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, pelicula.getTituloPelicula());
            st.setObject(2, pelicula.getYearPelicula());
            st.setObject(3, pelicula.getRatingPelicula());
            st.setObject(4, pelicula.getDuracionPelicula());
            st.setString(5, pelicula.getClasificacion().getNombreClasificacion());
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    pelicula.setIdPelicula(keys.getInt(1));
                }
            }
        }
        return pelicula;
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
        List<Pelicula> listaPelicula = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                Pelicula p = mapeoPelicula(rs);
                listaPelicula.add(p);
            }
        }
        return listaPelicula;
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
        Pelicula Pelicula = findByIdLazy(idPelicula);
        if (Pelicula == null) return null;

        Pelicula.setGeneros(peliculaGeneroDAO.findByPelicula(idPelicula));
        Pelicula.setActores(peliculaActorDAO.findByPelicula(idPelicula));
        Pelicula.setDirectores(peliculaDirectorDAO.findByPelicula(idPelicula));

        return Pelicula;
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
        List<Pelicula> listaPelicula = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_PAGE)) {
            st.setInt(1, pageSize);
            st.setInt(2, offset);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = mapeoPelicula(rs);
                    listaPelicula.add(pelicula);
                }
            }
        }
        cargarGenerosEnLote(listaPelicula);

        return listaPelicula;
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
        // 1. Se crea una lista para almacenar los parámetros que se usarán en la consulta.
        List<Object> parametros = new ArrayList<>();
        // 2. Se construye el fragmento de SQL dinámico (JOINs y WHERE) y se llena la lista de parámetros.
        String sqlPart = construirCondicionesFiltro(year, ratingMin, idGenero, filtroTitulo, parametros);
        // 3. Se combina el SQL base con el fragmento dinámico.
        String sql = SQL_COUNT_BASE + sqlPart;

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            // 4. Se asignan los parámetros a la consulta preparada.
            for (int i = 0; i < parametros.size(); i++) {
                st.setObject(i + 1, parametros.get(i));
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
        List<Pelicula> listaPelicula = new ArrayList<>();
        // 1. Se crea una lista para almacenar los parámetros que se usarán en la consulta.
        List<Object> parametros = new ArrayList<>();

        // 2. Se construye el fragmento de SQL dinámico (JOINs y WHERE) y se llena la lista de parámetros.
        String sqlPart = construirCondicionesFiltro(year, ratingMin, idGenero, filtroTitulo, parametros);
        // 3. Se combina el SQL base, el fragmento dinámico y la paginación.
        String sql = SQL_FILTER_SELECT + sqlPart + " " + SQL_FILTER_PAGINATION;

        // 4. Se añaden los parámetros de paginación a la lista.
        parametros.add(pageSize);
        parametros.add((page - 1) * pageSize);

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            // 5. Se asignan todos los parámetros (filtros + paginación) a la consulta preparada.
            for (int i = 0; i < parametros.size(); i++) {
                st.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    listaPelicula.add(mapeoPelicula(rs));
                }
            }
        }

        cargarGenerosEnLote(listaPelicula);
        return listaPelicula;
    }

    /**
     * Centraliza la lógica de construcción de las cláusulas SQL dinámicas (JOIN y WHERE) para los filtros.
     *
     * @param year         El año para filtrar.
     * @param ratingMin    El rating mínimo para filtrar.
     * @param idGenero     El ID del género para filtrar.
     * @param filtroTitulo El término de búsqueda para el título.
     * @param parametros   La lista de parámetros que se llenará.
     * @return Un String que contiene el fragmento de SQL generado.
     */
    private String construirCondicionesFiltro(Integer year, Double ratingMin, Integer idGenero, String filtroTitulo, List<Object> parametros) {
        StringBuilder consultaConstruida = new StringBuilder();

        // Si se filtra por género, se necesita hacer un JOIN con la tabla de relación.
        if (idGenero != null) {
            consultaConstruida.append("JOIN peliculagenero pg ON p.idpelicula = pg.idpelicula ");
        }

        List<String> condiciones = new ArrayList<>();

        // Para cada filtro que no sea nulo, se añade la condición SQL a una lista
        // y el valor del filtro a la lista de parámetros.
        if (year != null) {
            condiciones.add("p.yearpelicula = ?");
            parametros.add(year);
        }
        if (ratingMin != null) {
            condiciones.add("p.ratingpelicula >= ?");
            parametros.add(ratingMin);
        }
        if (idGenero != null) {
            condiciones.add("pg.idgenero = ?");
            parametros.add(idGenero);
        }
        if (filtroTitulo != null && !filtroTitulo.isBlank()) {
            condiciones.add("LOWER(p.titulopelicula) LIKE LOWER(?)");
            parametros.add("%" + filtroTitulo + "%");
        }

        // Si hay condiciones, se unen con "AND" y se añaden a la cláusula WHERE.
        if (!condiciones.isEmpty()) {
            consultaConstruida.append("WHERE ").append(String.join(" AND ", condiciones));
        }

        return consultaConstruida.toString();
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
