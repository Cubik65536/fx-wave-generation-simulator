package edu.vanier.fxwavegenerationsimulator.controllers;


import edu.vanier.fxwavegenerationsimulator.db.DBConnector;
import edu.vanier.fxwavegenerationsimulator.enums.WaveSimulationStatus;
import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Color;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import edu.vanier.fxwavegenerationsimulator.models.WaveGenerator;
import edu.vanier.fxwavegenerationsimulator.models.WaveSimulationDisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * This is the controller class that handles all simulation logics for the application.
 * This class extends the Thread class to run the simulation in a separate thread, which allows
 * constant updates of the wave simulation data.
 * By default, this class is set to update the simulation every 10 milliseconds.
 *
 * @author Qian Qian
 */
public class WaveSimulationController extends DBConnector {
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
    private class UpateTask extends TimerTask {
        @Override
        public void run() {
            milliseconds += DEFAULT_UPDATE_INTERVAL;
            simulate();
        }
    };

    /**
     * Instantiate the wave simulation controller with a given length of the wave to be simulated
     * and a default sample count of 1024.
     * @param totalLength The total length of the wave (in meters) to be simulated.
     */
    public WaveSimulationController(double totalLength, WaveSimulationDisplay waveSimulationDisplay) {
        this(totalLength, DEFAULT_SAMPLE_COUNT, waveSimulationDisplay);
    }

    /**
     * Instantiate the wave simulation controller with a given length of the wave to be simulated
     * and a given number of sample count.
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
     * @param wave The wave to be added to the wave generator.
     */
    public void addWave(Wave wave) {
        this.waves.add(wave);
        waveGenerator.addWave(wave);

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
        this.timer.scheduleAtFixedRate(new UpateTask(), 0, DEFAULT_UPDATE_INTERVAL);

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
     * @param milliseconds the time to be skipped in the simulation (in milliseconds).
     */
    public void step(int milliseconds) {
        this.milliseconds += milliseconds;
        simulate();
    }

    /**
     * Adding the wave to the database, which retrieves all parameters (Wave, waveType, frequency, amplitude and color)
     * and the related data points.
     * @param wave a wave object
     */
    public void addWaveDB(String simulationName, Wave wave) {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)","Wave", "Name", "waveType", "frequency",
                "amplitude", "data", "color");

        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, simulationName);
            stmt.setString(2, wave.getWaveType().toString());
            stmt.setInt(3, wave.getFrequency());
            stmt.setDouble(4, wave.getAmplitude());
            stmt.setString(5, getDataPoints());
            stmt.setString(6, wave.getColor().toString());
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the wave to the database, which puts all parameters (Wave, waveType, frequency, amplitude and color)
     * and related data points as a resultSet
     */
    public void getWavesDB(String simulationName) {
        String sql = String.format("SELECT * FROM %s", "Wave");
        try {
            Connection conn = Connector("wave.db");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.getString("Name").equals(simulationName)) {
                while (rs.next()) {
                    String waveType = rs.getString("waveType");
                    int frequency = rs.getInt("frequency");
                    double amplitude = rs.getDouble("amplitude");
                    String color = rs.getString("color");
                    // Find the 3 rgb values from color
                    Color waveColor = new Color(Integer.parseInt(color.substring(0, 3)), Integer.parseInt(color.substring(3, 6)), Integer.parseInt(color.substring(6, 9)));

                    WaveTypes type = switch (waveType) {
                        case "SIN" -> WaveTypes.SIN;
                        case "COS" -> WaveTypes.COS;
                        default -> throw new IllegalArgumentException("Invalid wave type: " + waveType);
                    };

                    Wave wave = new Wave(type, frequency, amplitude, waveColor);
                    addWave(wave);

                    String data = rs.getString("data");
                    HashMap<Wave, double[]> dataPoints = new HashMap<>();
                    for (String s : data.split(",")) {

                    }
                    //TODO
//                waveSimulationDisplay.update(data, milliseconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the wave based on a unique modifier color in the database.
     * @param simulationName the name of the given simulation
     */
    public void clearWavesDB(String simulationName) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", "Wave", "Name");
        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "Name");
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates pre-existing waves in the database.
     * @param wave
     */
    public void updateWavesDB(Wave wave) {
        String sql = String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?", "Wave",
                "waveType", "frequency", "amplitude", "data", "color", "color");
        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, wave.getWaveType().toString());
            stmt.setInt(2, wave.getFrequency());
            stmt.setDouble(3, wave.getAmplitude());
            stmt.setString(4, getDataPoints());
            stmt.setString(5, wave.getColor().toString());
            stmt.setString(6, wave.getColor().toString());
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();}
    }

    /**
     * Retrieves
     * @return
     */
    public String getDataPoints() {
        double[] dataPointsWave;
        Map<Wave, double[]> dataPoints = new HashMap<>();

        double[] dataPointsCombined = new double[sampleCount];
        double gap = totalLength / sampleCount;
        for (int i = 0; i < sampleCount; i++) {
            double x = i * gap;
            dataPointsCombined[i] = waveGenerator.combineWaves(x, milliseconds / 1000.0);
        }
        dataPoints.put(combinedWave, dataPointsCombined);

        for (Wave wave : waves) {
            dataPointsWave = new double[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                double x = i * gap;
                dataPointsWave[i] = wave.amplitude(x, milliseconds / 1000.0);
            }
            dataPoints.put(wave, dataPointsWave);
        }
        String dataPointsString = "";
        for (Map.Entry<Wave, double[]> entry : dataPoints.entrySet()) {
            for (int i = 0; i < sampleCount; i++) {
                dataPointsString += entry.getValue()[i];
            }
        }

        return dataPointsString;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public WaveSimulationStatus getSimulationStatus() {
        return simulationStatus;
    }
}
