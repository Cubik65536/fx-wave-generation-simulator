package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *  JavaFX controller class for the dialog box.
 *  @author CihaoZhang
 */

public class DialogBoxController extends Stage {
    public void showDialog (Stage primaryStage) {
        // Create a grid pane
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        // Create labels and text fields
        Label typeLabel = new Label("Type:");
        Label frequencyLabel = new Label("Frequency:");
        Label amplitudeLabel = new Label("Amplitude:");
        TextField frequencyField = new TextField();
        TextField amplitudeField = new TextField();

        // Create checkboxes for the type
        CheckBox sinCheckBox = new CheckBox("Sin");
        CheckBox cosCheckBox = new CheckBox("Cos");

        // Create buttons
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");

        // Add elements to the grid pane
        gridPane.add(typeLabel, 0, 0);
        gridPane.add(sinCheckBox, 0, 1);
        gridPane.add(cosCheckBox, 0, 2);
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
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add event handlers
        addButton.setOnAction(e -> {
            if (sinCheckBox.isSelected()) {
//                waveSimulationController.addWave(new Wave(WaveTypes.SIN, Integer.parseInt(frequencyField.getText()),
//                        Double.parseDouble(amplitudeField.getText())));
            } else if (cosCheckBox.isSelected()) {
//                waveSimulationController.addWave(new Wave(WaveTypes.COS, Integer.parseInt(frequencyField.getText()),
//                        Double.parseDouble(amplitudeField.getText())));
            }
            primaryStage.hide();
        });
        cancelButton.setOnAction(e -> {
            primaryStage.hide();
        });
    }
}