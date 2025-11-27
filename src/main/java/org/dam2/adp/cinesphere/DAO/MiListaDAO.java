package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO para la entidad MiLista.
 */
public class MiListaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO milista(idusuario, idpelicula, estado, puntuacion, urlimg, fecha_anadido) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM milista WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_FIND_BY_USER = "SELECT idpelicula FROM milista WHERE idusuario=?";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE milista SET estado=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_UPDATE_PUNTUACION = "UPDATE milista SET puntuacion=? WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_DELETE =
            "DELETE FROM milista WHERE idusuario=? AND idpelicula=?";

    private static final String SQL_COUNT_DURACION_TERMINADAS = "SELECT COALESCE(SUM(p.duracionpelicula), 0) FROM milista m JOIN pelicula p ON m.idpelicula = p.idpelicula  WHERE m.idusuario = ? AND m.estado = ?";

    private static final String SQL_COUNT_GENEROS_BY_USER = "SELECT g.nombregenero, COUNT(*) AS total FROM milista m JOIN peliculagenero pg ON m.idpelicula = pg.idpelicula JOIN genero g ON g.idgenero = pg.idgenero WHERE m.idusuario = ? GROUP BY g.nombregenero ORDER BY total DESC";

    private static final String SQL_COUNT_GUARDADAS ="SELECT COUNT(*) FROM milista WHERE idusuario = ?";
    private static final String SQL_COUNT_BY_ESTADOS = "SELECT COUNT(*) FROM milista WHERE idusuario = ? AND estado = ?";

    private static final String SQL_COUNT_ALL_ESTADOS =  "SELECT estado, COUNT(*) AS total FROM milista WHERE idusuario = ? GROUP BY estado";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();


    /**
     * Inserta una nueva entrada en la lista de un usuario.
     * @param ml la entrada a insertar.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void insert(MiLista ml) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_INSERT)) {
            st.setInt(1, ml.getUsuario().getIdUsuario());
            st.setInt(2, ml.getPelicula().getIdPelicula());
            st.setString(3, ml.getEstado() != null ? ml.getEstado().getDisplayValue() : null);
            st.setObject(4, ml.getPuntuacion());
            st.setString(5, ml.getUrlImg());
            st.setObject(6, ml.getFechaAnadido());
            st.executeUpdate();
        }
    }

    /**
     * Busca una entrada en la lista de un usuario por ID de usuario y de película.
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
     * Obtiene todas las películas de la lista de un usuario.
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
     * Actualiza el estado de una película en la lista de un usuario.
     * @param idUsuario el ID del usuario.
     * @param idPelicula el ID de la película.
     * @param estado el nuevo estado de la película.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public void updateEstado(int idUsuario, int idPelicula, PeliculaEstado estado) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_UPDATE_ESTADO)) {
            st.setString(1, estado.getDisplayValue());
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
     *
     * @param idUsuario  el ID del usuario.
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
     * Devuelve un mapa con el número de películas por estado para un usuario dado.
     * @param idUsuario identificador del usuario.
     * @return un mapa con el estado de la película como clave y el recuento como valor.
     * @throws SQLException en caso de error de acceso a datos.
     */
    public Map<PeliculaEstado, Integer> getEstadisticasEstados(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        Map<PeliculaEstado, Integer> mapa = new EnumMap<>(PeliculaEstado.class);
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_ALL_ESTADOS)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String estadoStr = rs.getString("estado");
                    int total = rs.getInt("total");
                    if (estadoStr != null) {
                        try {
                            PeliculaEstado estado = PeliculaEstado.fromString(estadoStr);
                            mapa.put(estado, total);
                        } catch (IllegalArgumentException ex) {
                            // Si hay valores no mapeados, los ignoramos silenciosamente
                        }
                    }
                }
            }
        }
        return mapa;
    }

    /**
     * Cuenta cuántas películas tiene guardadas un usuario en total.
     * @param idUsuario el ID del usuario.
     * @return el número total de películas guardadas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countGuardadas(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GUARDADAS)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Cuenta cuántas películas tiene el usuario en un estado concreto.
     * @param idUsuario el ID del usuario.
     * @param estado el estado de la película a contar.
     * @return el número de películas en el estado especificado.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int countByEstado(int idUsuario, PeliculaEstado estado) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_BY_ESTADOS)) {
            st.setInt(1, idUsuario);
            st.setString(2, estado.getDisplayValue());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Suma los minutos de duración de todas las películas TERMINADAS por el usuario.
     * @param idUsuario el ID del usuario.
     * @return el total de minutos de duración de las películas terminadas.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public int sumDuracionTerminadas(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_DURACION_TERMINADAS)) {
            st.setInt(1, idUsuario);
            st.setString(2, PeliculaEstado.TERMINADA.getDisplayValue());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    long total = rs.getLong(1);
                    if (total > Integer.MAX_VALUE) return Integer.MAX_VALUE;
                    return (int) total;
                }
            }
        }
        return 0;
    }

    /**
     * Devuelve un mapa ordenado (por total DESC) con el conteo de películas por género para un usuario.
     * @param idUsuario el ID del usuario.
     * @return un mapa con el nombre del género como clave y el recuento como valor.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    public Map<String, Integer> getConteoGenerosByUsuario(int idUsuario) throws SQLException {
        Connection conn = Conexion.getInstance().getConnection();
        Map<String, Integer> mapa = new LinkedHashMap<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GENEROS_BY_USER)) {
            st.setInt(1, idUsuario);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getString(1), rs.getInt(2));
                }
            }
        }
        return mapa;
    }
}
