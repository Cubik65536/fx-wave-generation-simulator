package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.enums.WaveSimulationStatus;
import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Color;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import edu.vanier.fxwavegenerationsimulator.models.WaveGenerator;
import edu.vanier.fxwavegenerationsimulator.models.WaveSimulationDisplay;

import java.util.*;

/**
 * This is the controller class that handles all simulation logics for the application.
 * This class extends the Thread class to run the simulation in a separate thread, which allows
 * constant updates of the wave simulation data.
 * By default, this class is set to update the simulation every 10 milliseconds.
 *
 * @author Qian Qian
 */
public class WaveSimulationController {
    /**
     * The default sample count of the wave simulation.
     */
    private static final int DEFAULT_SAMPLE_COUNT = 1024;
    /**
     * The default update interval of the wave simulation (in milliseconds).
     */
    private static final int DEFAULT_UPDATE_INTERVAL = 10;
    /**
     * The dummy wave object that represents the combined wave of all waves in the simulation.
     */
    private static final Wave combinedWave = new Wave(WaveTypes.DUMMY, -1, 0, new Color(0, 0, 0));

    /**
     * The status of the wave simulation (e.g. playing, paused, stopped).
     */
    private WaveSimulationStatus simulationStatus;

    /**
     * List that contains all Wave objects.
     */
    private List<Wave> waves;

    /**
     * The time elapsed in the simulation (in milliseconds).
     */
    private int milliseconds;

    /**
     * The timer that constantly updates the time of the simulation, and triggers the data update.
     */
    private Timer timer;

    /**
     * The wave generator that handles the wave generation logic (calculating the amplitude of the combined waves).
     */
    private WaveGenerator waveGenerator;

    /**
     * The total length of the wave (in meters) to be simulated.
     * This is a given value (e.g. the x-axis length of the graph) that determines the range
     * of the wave to be simulated.
     */
    private double totalLength;

    /**
     * The number of sample (data points) to be generated used to generate the wave graph.
     * The gap between each sample point is calculated by dividing the totalLength by the sample count.
     * This can be manually set when instantiating the controller, or a default value of 1024 points.
     */
    private int sampleCount;

    /**
     * The display component that shows the wave simulation.
     */
    private WaveSimulationDisplay waveSimulationDisplay;

    /**
     * The task that updates the simulation data.
     */
    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            milliseconds += DEFAULT_UPDATE_INTERVAL;
            simulate();
        }
    }

    /**
     * Instantiate the wave simulation controller with a given length of the wave to be simulated
     * and a default sample count of 1024.
     *
     * @param totalLength The total length of the wave (in meters) to be simulated.
     */
    public WaveSimulationController(double totalLength, WaveSimulationDisplay waveSimulationDisplay) {
        this(totalLength, DEFAULT_SAMPLE_COUNT, waveSimulationDisplay);
    }

    /**
     * Instantiate the wave simulation controller with a given length of the wave to be simulated
     * and a given number of sample count.
     *
     * @param totalLength The total length of the wave (in meters) to be simulated.
     * @param sampleCount The number of sample (data points) to be generated used to generate the wave graph.
     */
    public WaveSimulationController(double totalLength, int sampleCount, WaveSimulationDisplay waveSimulationDisplay) {
        this.simulationStatus = WaveSimulationStatus.STOPPED;
        this.waves = new ArrayList<>();
        this.milliseconds = 0;
        this.timer = new Timer();
        this.waveGenerator = new WaveGenerator();
        this.totalLength = totalLength;
        this.sampleCount = sampleCount;
        this.waveSimulationDisplay = waveSimulationDisplay;
    }

    /**
     * Adds a wave to the wave generator.
     *
     * @param wave The wave to be added to the wave generator.
     */
    public void addWave(Wave wave) {
        this.waves.add(wave);
        waveGenerator.addWave(wave);
    }

    public void clearWaves() {
        this.waves.clear();
        waveGenerator.clearWaves();
    }

    public void removeWave(Wave wave) {
        this.waves.remove(wave);
        waveGenerator.removeWave(wave);
    }

    /**
     * The main simulation logic, that calculates the amplitude of the combined waves at each sample point,
     * and updates the wave simulation display.
     */
    public void simulate() {
        Map<Wave, double[]> dataPoints = new HashMap<>();

        double[] dataPointsCombined = new double[sampleCount];
        double gap = totalLength / sampleCount;
        for (int i = 0; i < sampleCount; i++) {
            double x = i * gap;
            dataPointsCombined[i] = waveGenerator.combineWaves(x, milliseconds / 1000.0);
        }
        dataPoints.put(combinedWave, dataPointsCombined);
        for (Wave wave : waves) {
            double[] dataPointsWave = new double[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                double x = i * gap;
                dataPointsWave[i] = wave.amplitude(x, milliseconds / 1000.0);
            }
            dataPoints.put(wave, dataPointsWave);
        }

        waveSimulationDisplay.update(dataPoints, milliseconds);
    }

    /**
     * Start the wave simulation by starting the timer and the update task execution.
     */
    public void start() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new UpdateTask(), 0, DEFAULT_UPDATE_INTERVAL);

        // Set the simulation status to playing.
        this.simulationStatus = WaveSimulationStatus.PLAYING;
    }

    /**
     * Pause the wave simulation by stopping the timer.
     */
    public void pause() {
        this.timer.cancel();

        // Set the simulation status to paused.
        this.simulationStatus = WaveSimulationStatus.PAUSED;
    }

    /**
     * Stop the wave simulation by stopping the timer and resetting the time elapsed.
     */
    public void stop() {
        this.timer.cancel();
        this.milliseconds = 0;

        // Set the simulation status to stopped.
        this.simulationStatus = WaveSimulationStatus.STOPPED;
    }

    /**
     * Step the wave simulation by a given number of time (in milliseconds) (that is,
     * ship the simulation by a given time and update the simulation to the status at the new time).
     *
     * @param milliseconds the time to be skipped in the simulation (in milliseconds).
     */
    public void step(int milliseconds) {
        this.milliseconds += milliseconds;
        simulate();
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public WaveSimulationStatus getSimulationStatus() {
        return simulationStatus;
    }
}
