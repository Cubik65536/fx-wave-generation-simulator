package edu.vanier.fxwavegenerationsimulator.models;

import java.util.Map;

/**
 * This interface defines a display component for the wave simulation.
 * Which could be a printing console (when debugging) or a GUI component (graph).
 */
public interface WaveSimulationDisplay {
    /**
     * Update the display component with the latest wave simulation data.
     * @param dataPoints the map that contains the wave object and its corresponding data points to generate the wave graph.
     * @param milliseconds the time elapsed since the simulation started.
     */
    void update(Map<Wave, double[]> dataPoints, double milliseconds);
}
