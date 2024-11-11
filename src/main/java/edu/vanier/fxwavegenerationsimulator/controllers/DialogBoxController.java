package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *  JavaFX controller class for the dialog box.
 *  @author CihaoZhang
 */

public class DialogBoxController extends Stage {
    static Wave wave;

    /**
     * The flag indicating if the user selected "Cancel" to close the dialog box.
     */
    private boolean cancelled;

    /**
     * The display method to show the dialog box.
     * This isn't done in fxml because it is handled differently to be imported into the AddedWaves Tableview.
     */
    public DialogBoxController () {
        // Create a grid pane
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        initModality(Modality.APPLICATION_MODAL);

        // Create labels and text fields
        Label typeLabel = new Label("Type:");
        Label frequencyLabel = new Label("Frequency:");
        Label amplitudeLabel = new Label("Amplitude:");
        TextField frequencyField = new TextField();
        TextField amplitudeField = new TextField();

        // Create checkboxes for the type
        RadioButton sinSelected = new RadioButton("Sin");
        RadioButton cosSelected = new RadioButton("Cos");
        ToggleGroup group = new ToggleGroup();
        sinSelected.setToggleGroup(group);
        cosSelected.setToggleGroup(group);

        // Set default selection
        sinSelected.setSelected(true);

        // Create buttons
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");

        // Add elements to the grid pane
        gridPane.add(typeLabel, 0, 0);
        gridPane.add(sinSelected, 0, 1);
        gridPane.add(cosSelected, 0, 2);
        gridPane.add(frequencyLabel, 0, 3);
        gridPane.add(frequencyField, 1, 3);
        gridPane.add(amplitudeLabel, 0, 4);
        gridPane.add(amplitudeField, 1, 4);

        // Create an HBox for the buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addButton, cancelButton);
        gridPane.add(buttonBox, 0, 5, 2, 1);

        // Create a scene and add the grid pane to it
        Scene scene = new Scene(gridPane, 300, 250);
        this.setScene(scene);

        // Add event handlers
        addButton.setOnAction(e -> {
            if (frequencyField.getText().isEmpty() || amplitudeField.getText().isEmpty()) {
                MainAppFXMLController.showAlert("Error", "Please enter a frequency and amplitude.");
                return;
            }

            int frequency = -1;
            double amplitude = 0;

            try {
                frequency = Integer.parseInt(frequencyField.getText());
                amplitude = Double.parseDouble(amplitudeField.getText());
            } catch (NumberFormatException exception) {
                MainAppFXMLController.showAlert("Error", "Frequency and amplitude must be numbers.");
                return;
            }

            WaveTypes waveType = null;

            if (sinSelected.isSelected()) {
                waveType = WaveTypes.SIN;
            } else if (cosSelected.isSelected()) {
                waveType = WaveTypes.COS;
            }

            try {
                wave = new Wave(waveType, frequency, amplitude);
            } catch (IllegalArgumentException exception) {
                MainAppFXMLController.showAlert("Error", exception.getMessage());
                return;
            }

            cancelled = false;
            this.hide();
        });

        cancelButton.setOnAction(e -> {
            cancelled = true;
            this.hide();
        });

        wave = null;
    }

    /**
     * Getter for the wave object.
     * @return A wave object
     */
    public Wave getWave() {
        return wave;
    }

    /**
     * Getter for the isCancelled flag, so the main app can know if there is result to be read.
     * @return if the dialog box was cancelled
     * @author Qian Qian
     */
    public boolean isCancelled() {
        return cancelled;
    }
}