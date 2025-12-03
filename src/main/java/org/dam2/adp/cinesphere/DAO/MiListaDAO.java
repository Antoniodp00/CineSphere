package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO para la entidad MiLista.
 * Gestiona las películas guardadas por el usuario, sus estados y estadísticas.
 */
public class MiListaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO milista(idusuario, idpelicula, estado, puntuacion, urlimg, fecha_anadido) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM milista WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_FIND_BY_USER =
            "SELECT idpelicula FROM milista WHERE idusuario=?";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE milista SET estado=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_UPDATE_PUNTUACION =
            "UPDATE milista SET puntuacion=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_DELETE =
            "DELETE FROM milista WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_COUNT_GUARDADAS =
            "SELECT COUNT(*) FROM milista WHERE idusuario = ?";

    private static final String SQL_COUNT_BY_ESTADOS =
            "SELECT COUNT(*) FROM milista WHERE idusuario = ? AND estado = ?";

    private static final String SQL_COUNT_ALL_ESTADOS =
            "SELECT estado, COUNT(*) AS total FROM milista WHERE idusuario = ? GROUP BY estado";

    private static final String SQL_COUNT_DURACION_TERMINADAS =
            "SELECT COALESCE(SUM(p.duracionpelicula), 0) FROM milista m JOIN pelicula p ON m.idpelicula = p.idpelicula WHERE m.idusuario = ? AND m.estado = ?";

    private static final String SQL_COUNT_GENEROS_BY_USER =
            "SELECT g.nombregenero, COUNT(*) AS total FROM milista m JOIN peliculagenero pg ON m.idpelicula = pg.idpelicula JOIN genero g ON g.idgenero = pg.idgenero WHERE m.idusuario = ? GROUP BY g.nombregenero ORDER BY total DESC";

    private static final String SQL_FILTER_BASE =
            "SELECT DISTINCT p.* FROM pelicula p JOIN milista ml ON p.idpelicula = ml.idpelicula ";

    private static final String SQL_COUNT_FILTER_BASE =
            "SELECT COUNT(DISTINCT p.idpelicula) FROM pelicula p JOIN milista ml ON p.idpelicula = ml.idpelicula ";


    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();


    /**
     * Inserta una nueva entrada en la lista.
     * @param miLista la entrada a insertar.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void insert(MiLista miLista) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setInt(1, miLista.getUsuario().getIdUsuario());
            st.setInt(2, miLista.getPelicula().getIdPelicula());
            st.setString(3, miLista.getEstado() != null ? miLista.getEstado().getEstado() : null);
            st.setObject(4, miLista.getPuntuacion());
            st.setString(5, miLista.getUrlImg());
            st.setObject(6, miLista.getFechaAnadido());
            st.executeUpdate();
        }
    }

    /**
     * Busca una entrada específica (Película + Datos de usuario).
     * @param idUsuario el ID del usuario.
     * @param idPelicula el ID de la película.
     * @return la entrada encontrada, o null si no se encuentra.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public MiLista findAll(int idUsuario, int idPelicula) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_ALL)) {
            st.setInt(1, idUsuario);
            st.setInt(2, idPelicula);
            try (ResultSet rs = st.executeQuery()) {
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
            }
        }
        return null;
    }

    /**
     * Obtiene todas las películas (solo objetos Pelicula) de la lista del usuario.
     * @param idUsuario el ID del usuario.
     * @return una lista de las películas del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findPeliculasByUsuario(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> misPeliculas = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_FIND_BY_USER)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int idPelicula = rs.getInt("idpelicula");
                    Pelicula p = peliculaDAO.findByIdLazy(idPelicula);
                    if (p != null) {
                        misPeliculas.add(p);
                    }
                }
            }
        }
        return misPeliculas;
    }

    /**
     * Busca películas EN LA LISTA DEL USUARIO aplicando filtros dinámicos y paginación.
     * @param idUsuario el ID del usuario.
     * @param year el año de la película.
     * @param ratingMin el rating mínimo de la película.
     * @param idGenero el ID del género de la película.
     * @param searchQuery la consulta de búsqueda por título.
     * @param page el número de página.
     * @param pageSize el tamaño de la página.
     * @return una lista de películas filtradas y paginadas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public List<Pelicula> findFiltered(int idUsuario, Integer year, Double ratingMin, Integer idGenero, String searchQuery, int page, int pageSize) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Pelicula> peliculasFiltradas = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();

        String sqlPart = construirCondicionesFiltro(idUsuario, year, ratingMin, idGenero, searchQuery, parametros);
        String sql = SQL_FILTER_BASE + sqlPart + " ORDER BY p.idpelicula LIMIT ? OFFSET ?";

        parametros.add(pageSize);
        parametros.add((page - 1) * pageSize);

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                st.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    peliculasFiltradas.add(peliculaDAO.mapeoPelicula(rs));
                }
            }
        }

        peliculaDAO.cargarGenerosEnLote(peliculasFiltradas);

        return peliculasFiltradas;
    }

    /**
     * Cuenta películas EN LA LISTA DEL USUARIO con filtros dinámicos.
     * @param idUsuario el ID del usuario.
     * @param year el año de la película.
     * @param ratingMin el rating mínimo de la película.
     * @param idGenero el ID del género de la película.
     * @param searchQuery la consulta de búsqueda por título.
     * @return el número total de películas que coinciden con los filtros.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countPeliculas(int idUsuario, Integer year, Double ratingMin, Integer idGenero, String searchQuery) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        List<Object> params = new ArrayList<>();

        String sqlPart = construirCondicionesFiltro(idUsuario, year, ratingMin, idGenero, searchQuery, params);
        String sql = SQL_COUNT_FILTER_BASE + sqlPart;

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
     * Construye las condiciones de filtro para las consultas de MiLista.
     * @param idUsuario el ID del usuario.
     * @param year el año de la película.
     * @param ratingMin el rating mínimo de la película.
     * @param idGenero el ID del género de la película.
     * @param searchQuery la consulta de búsqueda por título.
     * @param parametros la lista de parámetros a llenar.
     * @return el fragmento de SQL con las condiciones.
     */
    private String construirCondicionesFiltro(int idUsuario, Integer year, Double ratingMin, Integer idGenero, String searchQuery, List<Object> parametros) {
        StringBuilder consultaConstruida = new StringBuilder();

        if (idGenero != null) {
            consultaConstruida.append("INNER JOIN peliculagenero pg ON p.idpelicula = pg.idpelicula ");
        }

        List<String> conditions = new ArrayList<>();
        
        conditions.add("ml.idusuario = ?");
        parametros.add(idUsuario);

        if (year != null) {
            conditions.add("p.yearpelicula = ?");
            parametros.add(year);
        }
        if (ratingMin != null) {
            conditions.add("p.ratingpelicula >= ?");
            parametros.add(ratingMin);
        }
        if (idGenero != null) {
            conditions.add("pg.idgenero = ?");
            parametros.add(idGenero);
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            conditions.add("p.titulopelicula ILIKE ?");
            parametros.add("%" + searchQuery + "%");
        }

        if (!conditions.isEmpty()) {
            consultaConstruida.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return consultaConstruida.toString();
    }

    /**
     * Actualiza el estado de una película en la lista de un usuario.
     * @param idUsuario el ID del usuario.
     * @param idPelicula el ID de la película.
     * @param estado el nuevo estado de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void updateEstado(int idUsuario, int idPelicula, PeliculaEstado estado) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ESTADO)) {
            st.setString(1, estado.getEstado());
            st.setInt(2, idUsuario);
            st.setInt(3, idPelicula);
            st.executeUpdate();
        }
    }

    /**
     * Actualiza la puntuación de una película en la lista de un usuario.
     * @param idUsuario el ID del usuario.
     * @param idPelicula el ID de la película.
     * @param puntuacion la nueva puntuación de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void updatePuntuacion(int idUsuario, int idPelicula, int puntuacion) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE_PUNTUACION)) {
            st.setInt(1, puntuacion);
            st.setInt(2, idUsuario);
            st.setInt(3, idPelicula);
            st.executeUpdate();
        }
    }

    /**
     * Elimina una película de la lista de un usuario.
     * @param idUsuario el ID del usuario.
     * @param idPelicula el ID de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void delete(int idUsuario, int idPelicula) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_DELETE)) {
            st.setInt(1, idUsuario);
            st.setInt(2, idPelicula);
            st.executeUpdate();
        }
    }

    /**
     * Obtiene las estadísticas de estados de las películas de un usuario.
     * @param idUsuario el ID del usuario.
     * @return un mapa con el estado de la película como clave y el número de películas como valor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Map<PeliculaEstado, Integer> getEstadisticasEstados(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        Map<PeliculaEstado, Integer> estadisticas = new EnumMap<>(PeliculaEstado.class);
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_ALL_ESTADOS)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String estadoStr = rs.getString("estado");
                    int total = rs.getInt("total");
                    if (estadoStr != null) {
                        try {
                            PeliculaEstado estado = PeliculaEstado.fromString(estadoStr);
                            estadisticas.put(estado, total);
                        } catch (IllegalArgumentException ex) {
                        }
                    }
                }
            }
        }
        return estadisticas;
    }

    /**
     * Cuenta el número total de películas guardadas por un usuario.
     * @param idUsuario el ID del usuario.
     * @return el número total de películas guardadas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countGuardadas(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GUARDADAS)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Cuenta el número de películas de un usuario en un estado concreto.
     * @param idUsuario el ID del usuario.
     * @param estado el estado de la película.
     * @return el número de películas en el estado especificado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countByEstado(int idUsuario, PeliculaEstado estado) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_BY_ESTADOS)) {
            st.setInt(1, idUsuario);
            st.setString(2, estado.getEstado());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Suma la duración de todas las películas terminadas por un usuario.
     * @param idUsuario el ID del usuario.
     * @return la duración total en minutos.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int sumDuracionTerminadas(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_DURACION_TERMINADAS)) {
            st.setInt(1, idUsuario);
            st.setString(2, PeliculaEstado.TERMINADA.getEstado());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Obtiene el conteo de géneros de las películas de un usuario.
     * @param idUsuario el ID del usuario.
     * @return un mapa con el nombre del género como clave y el número de películas como valor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Map<String, Integer> getConteoGenerosByUsuario(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        Map<String, Integer> generos = new LinkedHashMap<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GENEROS_BY_USER)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    generos.put(rs.getString(1), rs.getInt(2));
                }
            }
        }
        return generos;
    }
}
