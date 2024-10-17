package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.exceptions.ChosenFileIsDirectoryException;
import edu.vanier.fxwavegenerationsimulator.exceptions.DataFileNotFoundException;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import edu.vanier.fxwavegenerationsimulator.models.WaveSimulationDisplay;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.dataset.spi.DefaultDataSet;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static edu.vanier.fxwavegenerationsimulator.controllers.JsonDataController.importWaveSimulation;
import static edu.vanier.fxwavegenerationsimulator.enums.WaveTypes.SIN;

/**
 *  FXML controller class for the main application.
 *  This class handles all UI elements in the main application.
 *  Requires the WaveSimulationController class to be initialized. (TO-DO)
 *  @author CihaoZhang
 */

public class MainAppFXMLController implements WaveSimulationDisplay {

    private final static Logger logger = LoggerFactory.getLogger(MainAppFXMLController.class);

    private WaveSimulationController waveSimulationController;

    @FXML
    private Button importButton;

    @FXML
    private Button exportButton;

    @FXML
    private ComboBox<String> presetComboBox;

    @FXML
    private Button playButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button stepButton;

    @FXML
    private TableView<Wave> addedWavesTableView;

    @FXML
    private TableColumn<Wave, String> typeColumn;

    @FXML
    private TableColumn<Wave, Double> frequencyColumn;

    @FXML
    private TableColumn<Wave, Double> amplitudeColumn;

    @FXML
    private TableColumn<Wave, Double> currentAmplitudeColumn;

    @FXML
    private Button AddWave;

    @FXML
    private AnchorPane chartPane;

    private XYChart chart;

    @FXML
    public void initialize() {
        logger.info("Initializing MainAppController...");

        // Initialize ComboBox with presets
        // TO-DO: Add & Load Presets : Build #3
        presetComboBox.getItems().addAll("Pure Sin", "Square Wave", "Triangle Wave", "Sawtooth Wave");

        // Initialize volume slider
        volumeSlider.setValue(50);
        // Initialize TableView columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("waveType"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        amplitudeColumn.setCellValueFactory(new PropertyValueFactory<>("amplitude"));

        // Initialize TableView
        addedWavesTableView.getItems().add(new Wave(SIN, 1, 1.0));
        addedWavesTableView.getItems().add(new Wave(SIN, 1, -1.0));

        importButton.setOnAction(this::handleImportButton);
        exportButton.setOnAction(this::handleExportButton);

        try {
            playButton.setOnAction(event -> {
                waveSimulationController.start();
            });

            pauseButton.setOnAction(event -> {
                waveSimulationController.pause();
            });

            stepButton.setOnAction(event -> {
                waveSimulationController.step(100);
            });
        } catch (Exception e) {
            logger.error("Error initializing wave simulation: " + e.getMessage());
            showAlert("Error", "Error initializing wave simulation: " + e.getMessage());
        }

        // TO-DO: Change simulation to work on waves
        waveSimulationController = new WaveSimulationController(10, this);

        // TO-DO : Add functionality to add waves & Configure waveSimulationController
        AddWave.setOnAction(event -> {
            DialogBoxController dialogBoxController = new DialogBoxController();
            dialogBoxController.showDialog(new Stage());
        });

        // TO-DO : Add audio after displaying Waves -> currently Impossible


        // Set up the chart for wave visualization
        DefaultNumericAxis xAxis = new DefaultNumericAxis("X-Axis");
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Y-Axis");
        chart = new XYChart(xAxis, yAxis);

        chart.setTitle("Wave Simulation");

        // Enable zoom and pan
        chart.getPlugins().add(new Zoomer());

        // Add the chart to the chartPane (the placeholder in FXML)
        chartPane.getChildren().add(chart);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
    }

    @FXML
    private void handleImportButton(ActionEvent event) {
        // Implement Import functionality
        logger.info("Importing wave data...");
        try {
            // File chooser for selecting the JSON file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Wave Simulation Data File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());

            if (file != null) {
                // Read the JSON content from the file
                String jsonContent = new String(Files.readAllBytes(file.toPath()));
                // Use the JsonDataController to import the wave data
                JsonDataController.importWaveSimulation(waveSimulationController, jsonContent);
                logger.info("Wave data imported successfully from " + file.getAbsolutePath());
            } else {
                logger.warn("Import canceled or no file selected.");
            }
        } catch (ChosenFileIsDirectoryException e) {
            logger.error("Chosen file is a directory: " + e.getMessage());
            showAlert("Error", "Chosen file is a directory: " + e.getMessage());
        } catch (IOException e) {
            logger.error("An error occurred while importing wave data: " + e.getMessage());
            showAlert("Error", "An error occurred while importing wave data: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExportButton(ActionEvent event) {
        logger.info("Exporting wave data...");
        try {
            // File chooser for selecting the export destination
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Wave Simulation Data File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

            if (file != null) {
                // Use the JsonDataController to export the wave data
                String jsonContent = JsonDataController.exportWaveSimulation(waveSimulationController);
                // Write the JSON content to the file
                Files.write(file.toPath(), jsonContent.getBytes());
                logger.info("Wave data exported successfully to " + file.getAbsolutePath());
            } else {
                logger.warn("Export canceled or no file selected.");
            }
        } catch (IOException e) {
            logger.error("An error occurred while exporting wave data: " + e.getMessage());
            showAlert("Error", "An error occurred while exporting wave data: " + e.getMessage());
        }
    }

    @Override
    public void update(Map<Wave, double[]> dataPoints, double milliseconds) {
        // Clear previous data series
        Platform.runLater(() -> chart.getDatasets().clear());

        // Iterate over each wave and its corresponding data points
        for (Map.Entry<Wave, double[]> entry : dataPoints.entrySet()) {
            Wave wave = entry.getKey();
            double[] points = entry.getValue();

            // Create a dataset for each wave
            DefaultDataSet dataSet = new DefaultDataSet(wave.getWaveType().toString());

            for (int i = 0; i < points.length; i++) {
                dataSet.add(i, points[i]); // X is the index (time or position), Y is the amplitude
            }

            // Add the dataset to the chart
            Platform.runLater(() -> chart.getDatasets().add(dataSet));
        }
    }
}