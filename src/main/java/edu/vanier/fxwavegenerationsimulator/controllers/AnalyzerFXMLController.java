package edu.vanier.fxwavegenerationsimulator.controllers;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.math3.dfp.Dfp;

/**
 * The FXML controller for the individual Analyzer
 * @author Qian Qian
 */
public class AnalyzerFXMLController {
    @FXML
    private AnchorPane analyzerGraphPane;

    @FXML
    private AnchorPane volumeChartPane;

    @FXML
    private Label volumeLabel;

    @FXML
    public void initialize() {
        // Create the axis for the wave analyzer chart.
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Frequency");
        xAxis.setAutoRangeRounding(false);
        xAxis.setAutoRanging(true);
        xAxis.setUnit("Hz");
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Amplitude");
        yAxis.setAutoRangeRounding(false);
        yAxis.setAutoRanging(true);
        yAxis.setUnit("Amplitude Unit");

        // Create the chart for the wave analyzer.
        XYChart waveAnalyzerChart = new XYChart(xAxis, yAxis);
        waveAnalyzerChart.setTitle("Wave Analyzer");

        analyzerGraphPane.getChildren().add(waveAnalyzerChart);
        AnchorPane.setTopAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setBottomAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setLeftAnchor(waveAnalyzerChart, 0.0);
        AnchorPane.setRightAnchor(waveAnalyzerChart, 0.0);

        // Create the axis for the volume chart.
        DefaultNumericAxis volumeXAxis = new DefaultNumericAxis("", 0.0, 0.0, 5.0);
        volumeXAxis.setAutoRangeRounding(false);
        volumeXAxis.setAutoRanging(false);
        volumeXAxis.setUnit("Total");
        DefaultNumericAxis volumeAxis = new DefaultNumericAxis("Volume", 0.0, 128.0, 5.0);
        volumeAxis.setAutoRangeRounding(false);
        volumeAxis.setAutoRanging(false);
        volumeAxis.setUnit("Amplitude Unit");
        // Create the chart for the volume.
        XYChart volumeChart = new XYChart(volumeXAxis, volumeAxis);
        volumeChart.setTitle("Volume Chart");

        volumeChartPane.getChildren().add(volumeChart);
        AnchorPane.setTopAnchor(volumeChart, 0.0);
        AnchorPane.setBottomAnchor(volumeChart, 0.0);
        AnchorPane.setLeftAnchor(volumeChart, 0.0);
        AnchorPane.setRightAnchor(volumeChart, 0.0);
    }
}
