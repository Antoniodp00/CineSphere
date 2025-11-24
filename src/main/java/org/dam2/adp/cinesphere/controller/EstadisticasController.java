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

    /**
     * Inicializa el controlador, cargando las estadísticas del usuario actual.
     */
    @FXML
    private void initialize() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) {
            // No hay usuario en sesión, evita NPE y muestra ceros
            setKPIs(0, 0, 0);
            pieEstados.setData(FXCollections.observableArrayList());
            barGeneros.setData(FXCollections.observableArrayList());
            return;
        }

        int idUsuario = usuario.getIdUsuario();

        try {
            cargarKPIs(idUsuario);
            cargarPieEstados(idUsuario);
            cargarBarGeneros(idUsuario);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga los indicadores clave de rendimiento (KPIs) del usuario.
     * @param idUsuario el ID del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void cargarKPIs(int idUsuario) throws SQLException {
        int total = miListaDAO.countGuardadas(idUsuario);
        int vistas = miListaDAO.countByEstado(idUsuario, PeliculaEstado.TERMINADA);
        int minutosVistos = miListaDAO.sumDuracionTerminadas(idUsuario);
        setKPIs(total, vistas, minutosVistos);
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
        Map<PeliculaEstado, Integer> mapa = miListaDAO.getEstadisticasEstados(idUsuario);
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        int suma = 0;
        for (Map.Entry<PeliculaEstado, Integer> e : mapa.entrySet()) {
            datos.add(new PieChart.Data(e.getKey().getDisplayValue(), e.getValue()));
            suma += e.getValue();
        }

        pieEstados.setData(datos);
        pieEstados.setLabelsVisible(true);
        pieEstados.setLegendVisible(true);
        pieEstados.setTitle("Estados (" + suma + ")");
    }

    /**
     * Carga el gráfico de barras con las estadísticas de géneros de las películas del usuario.
     * @param idUsuario el ID del usuario.
     * @throws SQLException si ocurre un error al acceder a la base de datos.
     */
    private void cargarBarGeneros(int idUsuario) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad por género");

        for (Map.Entry<String, Integer> e : miListaDAO.getConteoGenerosByUsuario(idUsuario).entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        barGeneros.getData().clear();
        barGeneros.getData().add(series);
        barGeneros.setLegendVisible(false);
        barGeneros.setAnimated(false);
    }
}
