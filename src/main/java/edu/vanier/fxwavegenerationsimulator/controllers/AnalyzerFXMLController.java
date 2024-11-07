package edu.vanier.fxwavegenerationsimulator.controllers;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.LineStyle;
import io.fair_acc.chartfx.renderer.spi.HistogramRenderer;
import io.fair_acc.dataset.spi.DefaultDataSet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The FXML controller for the individual Analyzer
 * @author Qian Qian
 */
public class AnalyzerFXMLController {
    /**
     * The timer to keep data updating on the chart.
     */
    private Timer timer;
    /**
     * The time elapsed in after playing (in milliseconds)
     * to keep track of the time for the chart.
     */
    private int milliseconds;

    /**
     * The sound controller that generates the sound for the current simulation,
     * where the analyzer can fetch data and show it on the chart.
     */
    private SoundController soundController;

    /**
     * The setter for the sound controller, so the real controllers from MainApp can be passed in,
     * and this controller can fetch data from them.
     * @param soundController the sound controller of the current simulation
     */
    public void setSoundController(SoundController soundController) {
        this.soundController = soundController;
    }

    /**
     * The task that updates the data on the chart.
     */
    private class UpateTask extends TimerTask {
        @Override
        public void run() {
            milliseconds++;
            if (soundController != null) {
                // Get the sound data buffer from the controller.
                byte[] buffer = soundController.getBuffer();

                // As each index of the buffer represents 1/44100 second,
                // we convert the current time in milliseconds to the index in the buffer
                // by multiplying it by 44100 and dividing it by 1000.
                int bufferIndex = milliseconds * 44100 / 1000;

                // Fetch the amplitude from the sound controller.
                // To prevent out of bound exception, we do the index modulo of the buffer length to get the real index.
                byte amplitude = buffer[bufferIndex % buffer.length];

                // Update the volume label and the volume chart.
                Platform.runLater(() -> {
                    volumeLabel.setText(amplitude + "/127");

                    DefaultDataSet volumeDataSet = new DefaultDataSet("Volume");
                    // We need to add the data to both x = 0 and x = 1 to make the bar visible.
                    volumeDataSet.add(0, amplitude);
                    volumeDataSet.add(1, amplitude);
                    volumeRenderer.getDatasets().set(0, volumeDataSet);
                });
            }
        }
    };

    @FXML
    private AnchorPane analyzerGraphPane;

    @FXML
    private AnchorPane volumeChartPane;

    @FXML
    private Label volumeLabel;

    /**
     * The chart showing the analysis of each frequency.
     */
    private XYChart waveAnalyzerChart;

    /**
     * The chart showing the volume of the combined waves.
     */
    private XYChart volumeChart;
    private HistogramRenderer volumeRenderer;

    @FXML
    public void initialize() {
        timer = new Timer();

        // Create the axis for the wave analyzer chart.
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Frequency");
        xAxis.setAutoRangeRounding(false);
        xAxis.setAutoRanging(true);
        xAxis.setUnit("Hz");
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Amplitude", null);
        yAxis.setAutoRangeRounding(false);
        yAxis.setAutoRanging(true);

        // Create the chart for the wave analyzer.
        waveAnalyzerChart = new XYChart(xAxis, yAxis);
        waveAnalyzerChart.setTitle("Wave Analyzer");

        analyzerGraphPane.getChildren().add(waveAnalyzerChart);
        AnchorPane.setTopAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setBottomAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setLeftAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setRightAnchor(waveAnalyzerChart, 0.0);

        // Create the axis for the volume chart.
        DefaultNumericAxis volumeXAxis = new DefaultNumericAxis("", -0.5, 0.5, 1.0);
        volumeXAxis.setAutoRangeRounding(false);
        volumeXAxis.setAutoRanging(false);
        volumeXAxis.setUnit(null);
        DefaultNumericAxis volumeAxis = new DefaultNumericAxis("Volume", -128.0, 128.0, 5.0);
        volumeAxis.setAutoRangeRounding(false);
        volumeAxis.setAutoRanging(false);
        volumeAxis.setUnit(null);
        // Create the chart for the volume.
        volumeRenderer = new HistogramRenderer();
        volumeRenderer.setDrawBars(true);
        volumeRenderer.setPolyLineStyle(LineStyle.NONE);
        volumeRenderer.setShiftBar(false);

        volumeChart = new XYChart(volumeXAxis, volumeAxis);
        volumeChart.setTitle("Volume Chart");
        volumeChart.getRenderers().set(0, volumeRenderer);
        volumeChart.getLegend().getNode().visibleProperty().set(true);
        volumeChart.setLegendVisible(false);

        DefaultDataSet volumeDataSet = new DefaultDataSet("Volume");
        // We need to add the data to both x = 0 and x = 1 to make the bar visible.
        volumeDataSet.add(0, 0);
        volumeDataSet.add(1, 0);
        volumeRenderer.getDatasets().add(volumeDataSet);

        volumeChartPane.getChildren().add(volumeChart);
        AnchorPane.setTopAnchor(volumeChart, 0.0);
        AnchorPane.setBottomAnchor(volumeChart, 0.0);
        AnchorPane.setLeftAnchor(volumeChart, 0.0);
        AnchorPane.setRightAnchor(volumeChart, 0.0);
    }

    /**
     * Start to update the data on the analyzer.
     */
    public void start() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = new Timer();
        // Schedule the update task to run every 1 millisecond, which is the minimum allowed period.
        this.timer.scheduleAtFixedRate(new UpateTask(), 0, 1);
    }

    /**
     * Pause the updating of the data on the analyzer.
     */
    public void pause() {
        this.timer.cancel();
    }

    /**
     * Stop the updating of the data on the analyzer (the time is also reset).
     */
    public void stop() {
        this.timer.cancel();
        this.milliseconds = 0;
    }

    /**
     * Step the time by a given number of time.
     * @param milliseconds the time to be skipped (in milliseconds).
     */
    public void step(int milliseconds) {
        this.milliseconds += milliseconds;
        // Update the data on the chart.
        new UpateTask().run();
    }
}
