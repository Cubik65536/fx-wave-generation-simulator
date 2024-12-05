package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.MainApp;
import edu.vanier.fxwavegenerationsimulator.db.DBConnector;
import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Color;
import edu.vanier.fxwavegenerationsimulator.models.Wave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller that handles all database related operations.
 *
 * @author Qian Qian
 */
public class DatabaseController extends DBConnector {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    /**
     * If a table does not already exist within the user's computer, it will create a new SQLite table with the
     * corresponding variables. This will server as a local database to write to.
     */
    public void initializeDatabase() {
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS Wave (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            Name TEXT NOT NULL,
            waveType TEXT NOT NULL,
            frequency INTEGER NOT NULL,
            amplitude REAL NOT NULL,
            color TEXT NOT NULL
        );
    """;
        // In the event that it has not been created, it will be created.
        try (Connection conn = Connector("wave.db");
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            logger.info("Table 'Wave' created or already exists.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adding the wave to the database, which retrieves all parameters (Wave, waveType, frequency, amplitude and color)
     * and the related data points.
     *
     * @param wave a wave object
     */
    public void addWaveDB(String simulationName, Wave wave) {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", "Wave", "Name", "waveType", "frequency",
                "amplitude", "color");

        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            String data = "";
            stmt.setString(1, simulationName);
            stmt.setString(2, wave.getWaveType().toString());
            stmt.setInt(3, wave.getFrequency());
            stmt.setDouble(4, wave.getAmplitude());
            stmt.setString(5, wave.getColor().toString());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the wave to the database, which puts all parameters (Wave, waveType, frequency, amplitude and color)
     * and related data points as a resultSet
     *
     * @param simulationName the name of the simulation
     * @return an ArrayList of waves
     */
    public ArrayList<Wave> getWavesDB(String simulationName) {
        String sql = String.format("SELECT * FROM %s WHERE Name = %s", "Wave", "'" + simulationName + "'");

        ArrayList<Wave> wavesToAdd = new ArrayList<>();

        try {
            Connection conn = Connector("wave.db");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String waveType = rs.getString("waveType");
                int frequency = rs.getInt("frequency");
                double amplitude = rs.getDouble("amplitude");
                String color = rs.getString("color");
                String format = color.substring(1, color.length() - 1);
                String[] rgb = format.split(",");
                int red = Integer.parseInt(rgb[0]);
                int green = Integer.parseInt(rgb[1]);
                int blue = Integer.parseInt(rgb[2]);
                Color waveColor = new Color(red, green, blue);

                WaveTypes type = switch (waveType) {
                    case "SIN" -> WaveTypes.SIN;
                    case "COS" -> WaveTypes.COS;
                    default -> throw new IllegalArgumentException("Invalid wave type: " + waveType);
                };
                Wave wave = new Wave(type, frequency, amplitude, waveColor);
                wavesToAdd.add(wave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wavesToAdd;
    }


    /**
     * Removes the wave based on a unique modifier color in the database.
     *
     * @param simulationName the name of the given simulation
     */
    public void clearWavesDB(String simulationName) {
        String sql = String.format("DELETE FROM %s WHERE Name = %s", "Wave", "'" + simulationName + "'");
        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllWavesDB() {
        String sql = String.format("DELETE FROM %s", "Wave");
        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates pre-existing waves in the database.
     *
     * @param wave a wave object
     */
    public void updateWavesDB(Wave wave) {
        String sql = String.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?", "Wave",
                "waveType", "frequency", "amplitude", "data", "color", "color");
        try {
            Connection conn = Connector("wave.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(2, wave.getWaveType().toString());
            stmt.setInt(3, wave.getFrequency());
            stmt.setDouble(4, wave.getAmplitude());
            stmt.setString(5, wave.getColor().toString());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all simulationNames in a list from the database by using the SQL query.
     * @return A list of all simulation names
     */
    public List<String> getAllSimulationNames() {
        String sql = "SELECT DISTINCT Name FROM Wave";
        List<String> simulationNames = new ArrayList<>();

        try (Connection conn = Connector("wave.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                simulationNames.add(rs.getString("Name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return simulationNames;
    }

    /**
     * Used to increment the next simulation number in the database.
     * @return An increment for simulation numbers.
     */
    public int getNextSimulationNumber() {
        List<String> simulationNames = getAllSimulationNames();
        int maxSimulationNumber = 0;

        for (String name : simulationNames) {
            if (name.startsWith("Simulation ")) {
                try {
                    int number = Integer.parseInt(name.substring("Simulation ".length()));
                    maxSimulationNumber = Math.max(maxSimulationNumber, number);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return maxSimulationNumber + 1; // Return the next number
    }

    /**
     * Load the presets into the database.
     */
    public void loadPresets() {
        // Clear all waves in the database in the case of the default presets
        for (String name : getAllSimulationNames()) {
            switch (name) {
                case "Pure Sine", "Triangle Wave", "Square Wave", "Sawtooth Wave":
                    clearWavesDB(name);
                    break;
                default:
                    break;
            }
        }
        // Pure Sine Wave Preset
        addWaveDB("Pure Sine", new Wave(WaveTypes.SIN, 10, 1.0, new Color()));

        // Square Wave Presets
        addWaveDB("Square Wave", new Wave(WaveTypes.SIN, 10, 1.0, new Color()));
        addWaveDB("Square Wave", new Wave(WaveTypes.SIN, 30, 0.33, new Color()));
        addWaveDB("Square Wave", new Wave(WaveTypes.SIN, 50, 0.20, new Color()));
        addWaveDB("Square Wave", new Wave(WaveTypes.SIN, 70, 0.14, new Color()));

        // Triangle Wave Presets
        addWaveDB("Triangle Wave", new Wave(WaveTypes.SIN, 10, 1.0, new Color()));
        addWaveDB("Triangle Wave", new Wave(WaveTypes.SIN, 30, 0.11, new Color()));
        addWaveDB("Triangle Wave", new Wave(WaveTypes.SIN, 50, 0.04, new Color()));
        addWaveDB("Triangle Wave", new Wave(WaveTypes.SIN, 70, 0.02, new Color()));

        // Sawtooth Wave Presets
        addWaveDB("Sawtooth Wave", new Wave(WaveTypes.SIN, 10, 1.0, new Color()));
        addWaveDB("Sawtooth Wave", new Wave(WaveTypes.SIN, 20, 0.5, new Color()));
        addWaveDB("Sawtooth Wave", new Wave(WaveTypes.SIN, 30, 0.33, new Color()));
        addWaveDB("Sawtooth Wave", new Wave(WaveTypes.SIN, 40, 0.25, new Color()));
    }
}
