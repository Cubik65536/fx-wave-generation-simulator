package edu.vanier.fxwavegenerationsimulator.stages;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * The Wave Analyzer Window that contains wave analyze charts
 * (amplitude for each wave, overall amplitude, etc.)
 * @author Qian Qian
 */
public class WaveAnalyzer extends Stage {
    /**
     * The constructor of the Wave Analyzer Stage.
     * @param root the root object loaded from FXML by the MainApp
     * @param owner the parent window of this window (the Main App Window)
     * @param toggleCheckBox the CheckBox object in the Main Window that toggles this Analyzer Window
     */
    public WaveAnalyzer(Parent root, Window owner, CheckBox toggleCheckBox) {
        // Make sure this Window is closed when the Main Window is closed.
        this.initOwner(owner);
        // Make sure that the Window header only has a "close" button.
        this.initStyle(StageStyle.UTILITY);
        // Set the scene to the loaded root.
        this.setScene(new Scene(root, 800, 600));
        this.setOnHidden(event -> {
            // Automatically Uncheck the "Show Analyzer" CheckBox when the Window is closed.
            toggleCheckBox.setSelected(false);
        });
    }
}
