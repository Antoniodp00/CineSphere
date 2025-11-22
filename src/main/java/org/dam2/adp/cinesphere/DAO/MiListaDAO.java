package org.dam2.adp.cinesphere.DAO;

import org.dam2.adp.cinesphere.database.Conexion;
import org.dam2.adp.cinesphere.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

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

    private static final String SQL_COUNT_DURACION_TERMINADAS = "SELECT COALESCE(SUM(p.duracionpelicula), 0) FROM milista m JOIN pelicula p ON m.idpelicula = p.idpelicula  WHERE m.idusuario = ? AND m.estado = ?";

    private static final String SQL_COUNT_GENEROS_BY_USER = "SELECT g.nombregenero, COUNT(*) AS total FROM milista m JOIN peliculagenero pg ON m.idpelicula = pg.idpelicula JOIN genero g ON g.idgenero = pg.idgenero WHERE m.idusuario = ? GROUP BY g.nombregenero ORDER BY total DESC";

    private static final String SQL_COUNT_GUARDADAS ="SELECT COUNT(*) FROM milista WHERE idusuario = ?";
    private static final String SQL_COUNT_BY_ESTADOS = "SELECT COUNT(*) FROM milista WHERE idusuario = ? AND estado = ?";

    private static final String SQL_COUNT_ALL_ESTADOS =  "SELECT estado, COUNT(*) AS total FROM milista WHERE idusuario = ? GROUP BY estado";

    private final Connection conn = Conexion.getInstance().getConnection();
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

    /**
     * Devuelve un mapa con el número de películas por estado para un usuario dado.
     * Ejecuta un GROUP BY estado en la tabla milista.
     *
     * @param idUsuario identificador del usuario
     * @return Mapa Estado -> Conteo
     * @throws SQLException en caso de error de acceso a datos
     */
    public java.util.Map<PeliculaEstado, Integer> getEstadisticasEstados(int idUsuario) throws SQLException {

        PreparedStatement st = conn.prepareStatement(SQL_COUNT_ALL_ESTADOS);
        st.setInt(1, idUsuario);
        ResultSet rs = st.executeQuery();

        java.util.Map<PeliculaEstado, Integer> mapa = new java.util.EnumMap<>(PeliculaEstado.class);
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
        return mapa;
    }

    /**
     * Cuenta cuántas películas tiene guardadas un usuario en total.
     */
    public int countGuardadas(int idUsuario) throws SQLException {

        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GUARDADAS)) {
            st.setInt(1, idUsuario);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Cuenta cuántas películas tiene el usuario en un estado concreto.
     */
    public int countByEstado(int idUsuario, PeliculaEstado estado) throws SQLException {

        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_BY_ESTADOS)) {
            st.setInt(1, idUsuario);
            st.setString(2, estado.getDisplayValue());
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Suma los minutos de duración de todas las películas TERMINADAS por el usuario.
     */
    public int sumDuracionTerminadas(int idUsuario) throws SQLException {

        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_DURACION_TERMINADAS)) {
            st.setInt(1, idUsuario);
            st.setString(2, PeliculaEstado.TERMINADA.getDisplayValue());
            ResultSet rs = st.executeQuery();
            long total = 0L;
            if (rs.next()) {
                // En PostgreSQL SUM(int) -> int8 (BIGINT). Usar getLong para evitar PSQLException por Integer.
                total = rs.getLong(1);
                if (rs.wasNull()) total = 0L;
            }
            // Evitar overflow al convertir a int
            if (total > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (total < Integer.MIN_VALUE) return Integer.MIN_VALUE; // no debería ocurrir
            return (int) total;
        }
    }

    /**
     * Devuelve un mapa ordenado (por total DESC) con el conteo de películas por género para un usuario.
     * La clave es el nombre del género y el valor el total.
     */
    public Map<String, Integer> getConteoGenerosByUsuario(int idUsuario) throws SQLException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        try (PreparedStatement st = conn.prepareStatement(SQL_COUNT_GENEROS_BY_USER)) {
            st.setInt(1, idUsuario);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                mapa.put(rs.getString(1), rs.getInt(2));
            }
        }
        return mapa;
    }
}
