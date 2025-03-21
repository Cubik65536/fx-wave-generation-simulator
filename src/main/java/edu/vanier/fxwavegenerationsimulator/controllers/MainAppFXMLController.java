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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FXML controller class for the main application.
 * This class handles all UI elements in the main application.
 * Requires the WaveSimulationController class to be initialized.
 *
 * @author CihaoZhang
 */
public class MainAppFXMLController implements WaveSimulationDisplay {
    private final static Logger logger = LoggerFactory.getLogger(MainAppFXMLController.class);

    private WaveSimulationController waveSimulationController;
    private SoundController soundController;
    public DatabaseController databaseController;

    private Wave wave;

    /**
     * The analyzer window controller that controls the window that shows data about playing sound.
     */
    private AnalyzerFXMLController analyzerFXMLController;

    /**
     * The addWave dialog box controller that controls the pop-up window for adding waves.
     */
    public DialogBoxController dialogBoxController;

    private Map<String, Wave> predefinedSimulations = new HashMap<>();

    @FXML
    private Button importButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button refreshButton;

    @FXML
    private ComboBox<String> presetComboBox;

    @FXML
    private ImageView playButton;

    @FXML
    private ImageView pauseButton;

    @FXML
    private ImageView stopButton;

    @FXML
    private ImageView stepButton;

    @FXML
    private TableView<Wave> addedWavesTableView;

    @FXML
    private TableColumn<Wave, String> typeColumn;

    @FXML
    private TableColumn<Wave, Double> frequencyColumn;

    @FXML
    private TableColumn<Wave, Double> amplitudeColumn;

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

    @FXML
    private ListView<String> simulationListView = new ListView<>();

    private XYChart chart;

    /**
     * The setter for the analyzerFXMLController, so the MainApp can pass in the controller
     * so the current Main App Controller can control the Analyzer Window.
     *
     * @param analyzerFXMLController the controller of the Analyzer Window
     */
    public void setAnalyzerFXMLController(AnalyzerFXMLController analyzerFXMLController) {
        this.analyzerFXMLController = analyzerFXMLController;
    }

    /**
     * Add the new wave to ALL required places (simulation controller, audio controller, table view, etc.)
     *
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
     *
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
     *
     * @author Qian Qian
     */
    private void clearWaves() throws LineUnavailableException, IOException {
        // Clear the TableView
        addedWavesTableView.getItems().clear();
        // Clear the SoundController
        soundController.clearWaves();
        // Clear the chart
        chart.getDatasets().clear();

        // Reinstantiate the WaveSimulationController
        waveSimulationController = new WaveSimulationController(500, this);
    }

    @FXML
    public void initialize() {
        logger.info("Initializing MainAppController...");

        // Initialize WaveSimulationController and databaseController
        waveSimulationController = new WaveSimulationController(500, this);
        databaseController = new DatabaseController();
        databaseController.initializeDatabase();

        // Initialize SoundController and load presets
        try {
            soundController = new SoundController();
            databaseController.loadPresets();
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize ComboBox with presets
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

        // Initialize import/export buttons
        importButton.setOnAction(this::handleImportButton);
        exportButton.setOnAction(this::handleExportButton);

        // Initialize saveButton to save previous simulations to database
        saveButton.setOnAction(this::handleSaveButton);
        displaySimulationNames();
        refreshButton.setOnAction(this::handleRefreshButton);

        //Attribute images to buttons
        Image play = new Image("/images/circle-play.png");
        playButton.setImage(play);

        Image stop = new Image("/images/circle-stop.png");
        stopButton.setImage(stop);

        Image pause = new Image("/images/circle-pause.png");
        pauseButton.setImage(pause);

        Image step = new Image("/images/circle-step.png");
        stepButton.setImage(step);

        //Instantiating the ColorAdjust class
        ColorAdjust clickedButton = new ColorAdjust();

        //setting the color of the button
        clickedButton.setContrast(0.4);
        clickedButton.setHue(-0.05);
        clickedButton.setBrightness(1.0);
        clickedButton.setSaturation(0.5);

        //Instantiating the ColorAdjust class
        ColorAdjust unClickedButton = new ColorAdjust();

        //resizing the images & buttons
        playButton.setFitHeight(40);
        playButton.setFitWidth(40);

        stopButton.setFitHeight(40);
        stopButton.setFitWidth(40);

        pauseButton.setFitHeight(40);
        pauseButton.setFitWidth(40);

        stepButton.setFitHeight(40);
        stepButton.setFitWidth(40);

        //Setting up the functionalities of buttons & light up effect.
        try {
            playButton.setOnMouseClicked(event -> {
                waveSimulationController.start();
                if (analyzerFXMLController != null) {
                    // Update the sound data on the chart
                    analyzerFXMLController.start();
                }
                playButton.setEffect(clickedButton);
                pauseButton.setEffect(unClickedButton);
                stopButton.setEffect(unClickedButton);
            });
            pauseButton.setOnMouseClicked(event -> {
                waveSimulationController.pause();
                if (analyzerFXMLController != null) {
                    // Stop updating the sound data on the chart
                    analyzerFXMLController.pause();
                }

                // Stop sound when the simulation is paused
                soundController.stop();
                // Reset the audio playing toggle
                audioOffButton.setSelected(true);
                playButton.setEffect(unClickedButton);
                pauseButton.setEffect(clickedButton);
                stopButton.setEffect(unClickedButton);
            });
            stopButton.setOnMouseClicked(event -> {
                waveSimulationController.stop();
                if (analyzerFXMLController != null) {
                    // Stop updating the sound data on the chart
                    analyzerFXMLController.stop();
                }

                // Stop sound when the simulation is stopped
                soundController.stop();
                // Reset the audio playing toggle
                audioOffButton.setSelected(true);
                playButton.setEffect(unClickedButton);
                pauseButton.setEffect(unClickedButton);
                stopButton.setEffect(clickedButton);
            });
            stepButton.setOnMouseClicked(event -> {
                int stepping = 1;
                waveSimulationController.step(stepping);
                if (analyzerFXMLController != null) {
                    // Update the sound data on the chart
                    analyzerFXMLController.step(stepping);
                }
            });

            stepButton.setOnMousePressed(event -> {
                stepButton.setEffect(clickedButton);
            });

            stepButton.setOnMouseReleased(event -> {
                stepButton.setEffect(unClickedButton);
            });

        } catch (Exception e) {
            logger.error("Error initializing wave simulation: {}", e.getMessage());
            showAlert("Error", "Error initializing wave simulation: " + e.getMessage());
        }

        //Set up the buttons for adding waves
        addWaveButton.setOnAction(event -> {
            dialogBoxController = new DialogBoxController();
            dialogBoxController.showAndWait();
            if (!dialogBoxController.isCancelled()) {
                // Only read new wave data if the add wave dialog is not closed by cancel
                Wave newWave = dialogBoxController.getWave();
                if (!(newWave == null)) {
                    try {
                        addWave(newWave);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Set up the buttons for removing waves
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

        // Set up the button for clearing waves
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

        // Set up the checkbox for the wave analyzer
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
     *
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
                for (Wave wave :JsonDataController.importWaveSimulation(jsonContent)) {
                    addWave(wave);
                }
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
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives an alert with the given title and message
     *
     * @param title   the title of the alert
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
     * Handles the logic behind the refresh button. Refreshes the list of simulation names.
     * @param event The event triggered by the user clicking the refresh button.
     */
    private void handleRefreshButton(ActionEvent event) {
        displaySimulationNames();
    }

    /**
     * Handles the features from the export button. Opens a file chooser to select the JSON file to export.
     *
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

    /**
     * The logic behind the save button. Saves the current wave simulation to the database. This can be later retrieved
     * in read previous simulations.
     * @param event The event triggered by the user clicking the save button.
     */
    private void handleSaveButton(ActionEvent event) {
        // Checks if there are any waves to save
        if (!waveSimulationController.getWaves().isEmpty()) {
            // Get the next available simulation number
            int nextSimulationNumber = databaseController.getNextSimulationNumber();
            String newSimulationName = "Simulation " + nextSimulationNumber;

            // Save all waves under the new simulation name
            for (Wave wave : waveSimulationController.getWaves()) {
                databaseController.addWaveDB(newSimulationName, wave);
            }
            logger.info("Simulation saved as: " + newSimulationName);
        } else {
            logger.warn("No waves to save!");
        }
    }

    /**
     * Handles the selection of each preset in the ComboBox and allows them to be retrieved from the database.
     *
     * @param event The clicked event
     */
    public void handlePresetComboBox(ActionEvent event) {
        try {
            // Get the selected preset
            String preset = presetComboBox.getSelectionModel().getSelectedItem();
            ArrayList<Wave> wavesToAdd;
            // Get the corresponding waves from the database
            switch (preset) {
                case "Pure Sin":
                    clearWaves();
                    wavesToAdd = databaseController.getWavesDB("Pure Sine");
                    for (Wave wave : wavesToAdd) {
                        addWave(wave);
                    }
                    break;
                case "Square Wave":
                    clearWaves();
                    wavesToAdd = databaseController.getWavesDB("Square Wave");
                    for (Wave wave : wavesToAdd) {
                        addWave(wave);
                    }
                    break;
                case "Sawtooth Wave":
                    clearWaves();
                    wavesToAdd = databaseController.getWavesDB("Sawtooth Wave");
                    for (Wave wave : wavesToAdd) {
                        addWave(wave);
                    }
                    break;
                case "Triangle Wave":
                    clearWaves();
                    wavesToAdd = databaseController.getWavesDB("Triangle Wave");
                    for (Wave wave : wavesToAdd) {
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
     * Displays the names of all simulations in the database in the simulation list view.
     * This also locks the selection in the list view.
     */
    private void displaySimulationNames() {
        // Reads the last selected simulation
        AtomicReference<String> lastSelectedSimulation = new AtomicReference<>(null);
        // Clear any existing items
        simulationListView.getItems().clear();

        // Fetch simulation names from the database
        List<String> simulationNames = databaseController.getAllSimulationNames();
        simulationListView.getItems().addAll(simulationNames);

        // Add a selection listener for loading simulations (placeholder)
        simulationListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (!newValue.equals(lastSelectedSimulation.get())) {
                    System.out.println("Selected simulation: " + newValue);

                    // Check if the simulation exists in predefined ones
                    if (!predefinedSimulations.containsKey(newValue)) {
                        // Check if the simulation exists in the database
                        List<Wave> waves = databaseController.getWavesDB(newValue);
                        if (waves.isEmpty()) {
                            // Simulation not found in the database either
                            System.out.println("Simulation not found in database.");
                        } else {
                            // Simulation found in the database
                            System.out.println("Simulation found in database.");
                            for (Wave wave : waves) {
                                try {
                                    addWave(wave);
                                } catch (LineUnavailableException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else {
                        System.out.println("Simulation not created yet: " + newValue);
                    }
                } else {
                    System.out.println("You cannot re-select the same simulation: " + newValue);
                    simulationListView.getSelectionModel().clearSelection();
                }
            }
        });
    }

    /**
     * Creates a dataset for each wave and adds it to the chart. This method draws the graph of the wave.
     * This is the main UI component of the application.
     *
     * @param dataPoints   the map that contains the wave object and its corresponding data points to generate the wave graph.
     * @param milliseconds the time elapsed since the simulation started.
     */
    @Override
    public void update(Map<Wave, double[]> dataPoints, double milliseconds) {
        Platform.runLater(() -> {
            // Clear previous data series
            chart.getDatasets().clear();

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
                chart.getDatasets().add(dataSet);
            }
        });
    }

    /**
     * A getter for the "Show Analyzer" CheckBox, so it can be
     * automatically unchecked elsewhere when the Analyzer Window is closed.
     *
     * @return the object of the "Show Analyzer" CheckBox
     * @author Qian Qian
     */
    public CheckBox getShowAnalyzerCheckBox() {
        return showAnalyzerCheckBox;
    }

    /**
     * A getter for the Sound Controller so that the Wave Analyzer can access it
     * to get sound data to show.
     *
     * @return the Sound Controller object that is active in this simulation
     * @author Qian Qian
     */
    public SoundController getSoundController() {
        return soundController;
    }
}
