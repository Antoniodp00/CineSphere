package org.dam2.adp.cinesphere.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.dam2.adp.cinesphere.DAO.MiListaDAO;
import org.dam2.adp.cinesphere.model.PeliculaEstado;
import org.dam2.adp.cinesphere.model.Usuario;
import org.dam2.adp.cinesphere.util.SessionManager;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la vista de estadísticas, que muestra datos sobre la actividad del usuario.
 */
public class EstadisticasController {

    @FXML private Label lblTotalGuardadas;
    @FXML private Label lblPeliculasVistas;
    @FXML private Label lblTiempoTotal;

    @FXML private PieChart pieEstados;
    @FXML private BarChart<String, Number> barGeneros;

    private final MiListaDAO miListaDAO = new MiListaDAO();
    private static final Logger logger = Logger.getLogger(EstadisticasController.class.getName());

    /**
     * Inicializa el controlador, cargando las estadísticas del usuario actual.
     */
    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Inicializando EstadisticasController...");
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) {
            logger.log(Level.WARNING, "No hay usuario en sesión. Mostrando estadísticas vacías.");
            setKPIs(0, 0, 0);
            pieEstados.setData(FXCollections.observableArrayList());
            barGeneros.setData(FXCollections.observableArrayList());
            return;
        }

        int idUsuario = usuario.getIdUsuario();
        logger.log(Level.INFO, "Cargando estadísticas para el usuario ID: " + idUsuario);

        try {
            cargarKPIs(idUsuario);
            cargarPieEstados(idUsuario);
            cargarBarGeneros(idUsuario);
            logger.log(Level.INFO, "Estadísticas cargadas correctamente.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al cargar las estadísticas", e);
        }
    }

    /**
     * Carga los indicadores clave de rendimiento (KPIs) del usuario.
     * @param idUsuario el ID del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void cargarKPIs(int idUsuario) throws SQLException {
        logger.log(Level.INFO, "Cargando KPIs...");
        int total = miListaDAO.countGuardadas(idUsuario);
        int vistas = miListaDAO.countByEstado(idUsuario, PeliculaEstado.TERMINADA);
        int minutosVistos = miListaDAO.sumDuracionTerminadas(idUsuario);
        setKPIs(total, vistas, minutosVistos);
        logger.log(Level.INFO, "KPIs cargados: Total=" + total + ", Vistas=" + vistas + ", Minutos=" + minutosVistos);
    }

    /**
     * Establece los valores de los KPIs en las etiquetas de la interfaz.
     * @param total el número total de películas guardadas.
     * @param vistas el número de películas vistas.
     * @param minutosVistos el total de minutos de películas vistas.
     */
    private void setKPIs(int total, int vistas, int minutosVistos) {
        lblTotalGuardadas.setText(String.valueOf(total));
        lblPeliculasVistas.setText(String.valueOf(vistas));
        lblTiempoTotal.setText(formatearMinutos(minutosVistos));
    }

    /**
     * Formatea un número de minutos a un formato de horas y minutos.
     * @param minutos el número de minutos a formatear.
     * @return una cadena con el formato "X h Y min".
     */
    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return horas + " h " + mins + " min";
    }

    /**
     * Carga el gráfico de tarta con las estadísticas de estados de las películas del usuario.
     * @param idUsuario el ID del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void cargarPieEstados(int idUsuario) throws SQLException {
        logger.log(Level.INFO, "Cargando gráfico de estados...");
        Map<PeliculaEstado, Integer> mapa = miListaDAO.getEstadisticasEstados(idUsuario);
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        int suma = 0;
        for (Map.Entry<PeliculaEstado, Integer> e : mapa.entrySet()) {
            datos.add(new PieChart.Data(e.getKey().getEstado(), e.getValue()));//añade los datos a la tarta
            suma += e.getValue();
        }

        pieEstados.setData(datos);
        pieEstados.setLabelsVisible(true);
        pieEstados.setLegendVisible(true);
        pieEstados.setTitle("Estados (" + suma + ")");
        logger.log(Level.INFO, "Gráfico de estados cargado con " + datos.size() + " secciones.");
    }

    /**
     * Carga el gráfico de barras con las estadísticas de géneros de las películas del usuario.
     * @param idUsuario el ID del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void cargarBarGeneros(int idUsuario) throws SQLException {
        logger.log(Level.INFO, "Cargando gráfico de géneros...");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad por género");

        for (Map.Entry<String, Integer> e : miListaDAO.getConteoGenerosByUsuario(idUsuario).entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())); //añade texto eje x cantidad eje y
        }

        barGeneros.getData().clear();
        barGeneros.getData().add(series);
        barGeneros.setLegendVisible(false);
        barGeneros.setAnimated(false);
        logger.log(Level.INFO, "Gráfico de géneros cargado con " + series.getData().size() + " barras.");
    }
}
