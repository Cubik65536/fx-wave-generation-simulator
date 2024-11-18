package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.MainApp;
import edu.vanier.fxwavegenerationsimulator.enums.WaveSimulationStatus;
import edu.vanier.fxwavegenerationsimulator.exceptions.ChosenFileIsDirectoryException;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import edu.vanier.fxwavegenerationsimulator.models.WaveSimulationDisplay;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.dataset.spi.DefaultDataSet;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

/**
 *  FXML controller class for the main application.
 *  This class handles all UI elements in the main application.
 *  Requires the WaveSimulationController class to be initialized.
 *
 *  @author CihaoZhang
 */
public class MainAppFXMLController implements WaveSimulationDisplay {
    private final static Logger logger = LoggerFactory.getLogger(MainAppFXMLController.class);

    private WaveSimulationController waveSimulationController;
    private SoundController soundController;
    private DatabaseController databaseController;

    private Wave wave;

    /**
     * The analyzer window controller that controls the window that shows data about playing sound.
     */
    private AnalyzerFXMLController analyzerFXMLController;

    public DialogBoxController dialogBoxController;

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
    private Button stopButton;

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
    private Button addWaveButton;

    @FXML
    private Button removeWaveButton;

    @FXML
    private Button clearWavesButton;

    @FXML
    private AnchorPane chartPane;

    @FXML
    private RadioButton audioOffButton;

    @FXML
    private RadioButton audioOnButton;

    @FXML
    private CheckBox showAnalyzerCheckBox;

    private XYChart chart;

    /**
     * The setter for the analyzerFXMLController, so the MainApp can pass in the controller
     * so the current Main App Controller can control the Analyzer Window.
     * @param analyzerFXMLController the controller of the Analyzer Window
     */
    public void setAnalyzerFXMLController(AnalyzerFXMLController analyzerFXMLController) {
        this.analyzerFXMLController = analyzerFXMLController;
    }

    /**
     * Add the new wave to ALL required places (simulation controller, audio controller, table view, etc.)
     * @param newWave the new wave to be added
     * @author Qian Qian
     */
    private void addWave(Wave newWave) throws LineUnavailableException, IOException {
        // Add the new wave to the TableView
        addedWavesTableView.getItems().add(newWave);
        // Add the new wave to the WaveSimulationController and SoundController
        waveSimulationController.addWave(newWave);
        soundController.addWave(newWave);
        // Update the chart with the new wave
        waveSimulationController.simulate();
    }

    /**
     * Remove a specific wave from all places.
     * @param wave the wave to be removed
     */
    private void removeWave(Wave wave) throws LineUnavailableException, IOException {
        // Remove the wave from the TableView
        addedWavesTableView.getItems().remove(wave);
        // Remove the wave from the WaveSimulationController and SoundController
        waveSimulationController.removeWave(wave);
        soundController.removeWave(wave);
        // Update the chart with the new wave
        waveSimulationController.simulate();
    }

    /**
     * Clear all waves from the all places.
     * @author Qian Qian
     */
    private void clearWaves() throws LineUnavailableException, IOException {
        // Clear the TableView
        addedWavesTableView.getItems().clear();
        // Clear the WaveSimulationController and SoundController
        waveSimulationController.clearWaves();
        soundController.clearWaves();
        // Clear the chart
        chart.getDatasets().clear();
    }

    @FXML
    public void initialize() {
        logger.info("Initializing MainAppController...");

        waveSimulationController = new WaveSimulationController(500, this);
        databaseController = new DatabaseController();

        try {
            soundController = new SoundController();
            databaseController.loadPresets();
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize ComboBox with presets
        // TO-DO: Add & Load Presets : Build #3
        presetComboBox.getItems().addAll("Pure Sin", "Square Wave", "Triangle Wave", "Sawtooth Wave");
        presetComboBox.setOnAction(this::handlePresetComboBox);

        // Use ToggleGroup to ensure only one audio button is selected at a time
        ToggleGroup audioToggleGroup = new ToggleGroup();
        audioOffButton.setToggleGroup(audioToggleGroup);
        audioOnButton.setToggleGroup(audioToggleGroup);
        // Make sure audio is off by default
        audioOffButton.setSelected(true);

        // Initialize TableView columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("waveType"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        amplitudeColumn.setCellValueFactory(new PropertyValueFactory<>("amplitude"));
        // The use of currentAmplitudeColumn is part of another build, as it requires Wave Generator to clash multiple waves.
//        currentAmplitudeColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmplitude"));

        importButton.setOnAction(this::handleImportButton);
        exportButton.setOnAction(this::handleExportButton);

        try {
            playButton.setOnAction(event -> {
                waveSimulationController.start();
                if (analyzerFXMLController != null) {
                    // Update the sound data on the chart
                    analyzerFXMLController.start();
                }
            });
            pauseButton.setOnAction(event -> {
                waveSimulationController.pause();
                if (analyzerFXMLController != null) {
                    // Stop updating the sound data on the chart
                    analyzerFXMLController.pause();
                }

                // Stop sound when the simulation is paused
                soundController.stop();
                // Reset the audio playing toggle
                audioOffButton.setSelected(true);
            });
            stopButton.setOnAction(event -> {
                waveSimulationController.stop();
                if (analyzerFXMLController != null) {
                    // Stop updating the sound data on the chart
                    analyzerFXMLController.stop();
                }

                // Stop sound when the simulation is stopped
                soundController.stop();
                // Reset the audio playing toggle
                audioOffButton.setSelected(true);
            });
            stepButton.setOnAction(event -> {
                int step = 100;
                waveSimulationController.step(step);
                if (analyzerFXMLController != null) {
                    // Update the sound data on the chart
                    analyzerFXMLController.step(step);
                }
            });
        } catch (Exception e) {
            logger.error("Error initializing wave simulation: {}", e.getMessage());
            showAlert("Error", "Error initializing wave simulation: " + e.getMessage());
        }

        addWaveButton.setOnAction(event -> {
            dialogBoxController = new DialogBoxController();
            dialogBoxController.showAndWait();
            if (!dialogBoxController.isCancelled()) {
                // Only read new wave data if the add wave dialog is not closed by cancel
                Wave newWave = dialogBoxController.getWave();
                try {
                    addWave(newWave);
                } catch (LineUnavailableException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        removeWaveButton.setOnAction(event -> {
            Wave selectedWave = addedWavesTableView.getSelectionModel().getSelectedItem();
            if (selectedWave != null) {
                try {
                    removeWave(selectedWave);
                } catch (LineUnavailableException | IOException e) {
                    logger.error("Error removing wave: {}", e.getMessage());
                }
            }
        });

        clearWavesButton.setOnAction(event -> {
            try {
                clearWaves();
            } catch (LineUnavailableException | IOException e) {
                logger.error("Error clearing waves: {}", e.getMessage());
            }
        });

        // Play sound when the audioOnButton is selected
        audioOnButton.setOnAction(event -> {
            if (waveSimulationController.getSimulationStatus() == WaveSimulationStatus.PLAYING) {
                // Only play sound if the simulation is playing
                soundController.start();
            } else {
                // Prevent the on button from being selected it's not playing
                audioOffButton.setSelected(true);
            }
        });
        // Stop sound when the audioOffButton is selected
        audioOffButton.setOnAction(event -> {
            soundController.stop();
        });

        showAnalyzerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Show the Wave Analyzer
                MainApp.showWaveAnalyzer();
            } else {
                // Hide the Wave Analyzer
                MainApp.hideWaveAnalyzer();
            }
        });

        // Set up the chart for wave visualization
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Position", null);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Frequency", "Hz");
        chart = new XYChart(xAxis, yAxis);

        chart.setTitle("Wave Simulation");

        // Enable zoom
        chart.getPlugins().add(new Zoomer());

        // Add the chart to the chartPane (the placeholder in FXML)
        chartPane.getChildren().add(chart);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
        xAxis.setMin(0);
        xAxis.setMax(1000);
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(1);
    }

    /**
     * Handles the features from the import button. Opens a file chooser to select the JSON file to import.
     * @param event The event triggered by the user clicking the import button.
     */
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

    /**
     * Gives an alert with the given title and message
     * @param title the title of the alert
     * @param message the error message
     */
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the features from the export button. Opens a file chooser to select the JSON file to export.
     * @param event The event triggered by the user clicking the export button.
     */
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

    public void handlePresetComboBox(ActionEvent event) {
        try {
        String preset = presetComboBox.getSelectionModel().getSelectedItem();
        ArrayList<Wave> wavesToAdd;
        switch (preset) {
            case "Pure Sin":
                clearWaves();
                wavesToAdd = databaseController.getWavesDB("Pure Sine");
                for (Wave wave: wavesToAdd) {
                    addWave(wave);
                }
                break;
            case "Square Wave":
                clearWaves();
                wavesToAdd = databaseController.getWavesDB("Square Wave");
                for (Wave wave: wavesToAdd) {
                    addWave(wave);
                }
                break;
            case "Sawtooth Wave":
                clearWaves();
                wavesToAdd = databaseController.getWavesDB("Sawtooth Wave");
                for (Wave wave: wavesToAdd) {
                    addWave(wave);
                }
                break;
            case "Triangle Wave":
                clearWaves();
                wavesToAdd = databaseController.getWavesDB("Triangle Wave");
                for (Wave wave: wavesToAdd) {
                    addWave(wave);
                }
                break;
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a dataset for each wave and adds it to the chart. This method draws the graph of the wave.
     * This is the main UI component of the application.
     * @param dataPoints the map that contains the wave object and its corresponding data points to generate the wave graph.
     * @param milliseconds the time elapsed since the simulation started.
     */
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

    /**
     * A getter for the "Show Analyzer" CheckBox, so it can be
     * automatically unchecked elsewhere when the Analyzer Window is closed.
     * @return the object of the "Show Analyzer" CheckBox
     * @author Qian Qian
     */
    public CheckBox getShowAnalyzerCheckBox() {
        return showAnalyzerCheckBox;
    }

    /**
     * A getter for the Sound Controller so that the Wave Analyzer can access it
     * to get sound data to show.
     * @return the Sound Controller object that is active in this simulation
     * @author Qian Qian
     */
    public SoundController getSoundController() {
        return soundController;
    }
}
