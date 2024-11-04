package edu.vanier.fxwavegenerationsimulator;

import edu.vanier.fxwavegenerationsimulator.controllers.AnalyzerFXMLController;
import edu.vanier.fxwavegenerationsimulator.controllers.MainAppFXMLController;
import java.io.IOException;

import edu.vanier.fxwavegenerationsimulator.stages.WaveAnalyzer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a JavaFX project template to be used for creating GUI applications.
 * The JavaFX GUI framework (version: 22.0.2) is linked to this project in the
 * build.gradle file.
 * @link: https://openjfx.io/javadoc/22/
 * @see: /Build Scripts/build.gradle
 * @author frostybee.
 */
public class MainApp extends Application {

    public static final String MAINAPP_LAYOUT = "MainApp_layout";
    public static final String ANALYZER_LAYOUT = "Analyzer_layout";
    private final static Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static Scene scene;
    private static WaveAnalyzer waveAnalyzer;

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Bootstrapping the application...");

            // Load the scene of the primary stage.
            MainAppFXMLController mainAppFXMLController = new MainAppFXMLController();
            Parent root = loadFXML(MAINAPP_LAYOUT, mainAppFXMLController);
            scene = new Scene(root, 940, 600);
            primaryStage.setScene(scene);

            // Load and initialize the analyzer.
            Parent analyzerRoot = loadFXML(ANALYZER_LAYOUT, new AnalyzerFXMLController());
            waveAnalyzer = new WaveAnalyzer(analyzerRoot, primaryStage, mainAppFXMLController.getShowAnalyzerCheckBox());

            // Put this appliation's main window on top of other already-opened windows
            // upon launching the app.
            primaryStage.setAlwaysOnTop(true);
            primaryStage.show();
            primaryStage.setAlwaysOnTop(false);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Show the individual Wave Analyzer Window.
     * @author Qian Qian
     */
    public static void showWaveAnalyzer() {
        waveAnalyzer.show();
    }

    /**
     * Hide the individual Wave Analyzer Window.
     * @author Qian Qian
     */
    public static void hideWaveAnalyzer() {
        waveAnalyzer.hide();
    }

    /**
     * Loads a scene graph from an FXML file.
     *
     * @param fxmlFile The name of the FXML file to be loaded.
     * @param fxmlController An instance of the FXML controller to be associated
     * with the loaded FXML scene graph.
     * @return The root node of the loaded scene graph.
     * @throws IOException
     */
    public static Parent loadFXML(String fxmlFile, Object fxmlController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxmlFile + ".fxml"));
        fxmlLoader.setController(fxmlController);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
