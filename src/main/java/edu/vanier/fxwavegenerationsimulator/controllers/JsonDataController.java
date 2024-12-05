package edu.vanier.fxwavegenerationsimulator.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.vanier.fxwavegenerationsimulator.models.Wave;

import java.util.ArrayList;
import java.util.List;

/**
 * This controller handles the JSON data processing for the wave simulation import/export.
 * As only the list of waves is needed to recreate simulation,
 * only the list of waves is imported/exported.
 *
 * @author Qian Qian
 */
public class JsonDataController {
    List<Wave> waves; // The list of waves to be imported/exported>
    /**
     * Given a wave simulation controller, add the waves imported from the JSON string to the controller.
     * @param json the JSON string that contains wave data to deserialize
     * @return A list of waves deserialized from the JSON string
     */
    public static List<Wave> importWaveSimulation(String json) {
        // Create a GSON object to process the data.
        Gson gson = new Gson();

        // Convert the JSON string to a list of Wave objects.
        TypeToken<List<Wave>> collectionType = new TypeToken<>(){};

        return gson.fromJson(json, collectionType);
    }

    /**
     * Export the data of discrete waves in a wave simulation from its controller to a JSON string.
     * @param waveSimulationController the wave simulation controller to export the data from
     * @return the JSON string that contains the wave data
     */
    public static String exportWaveSimulation(WaveSimulationController waveSimulationController) {
        // Get list of wave to simulate.
        List<Wave> waves = waveSimulationController.getWaves();

        // Create a GSON object to process the data.
        Gson gson = new Gson();

        // Return the generated JSON string.
        return gson.toJson(waves);
    }
}
