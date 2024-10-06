package edu.vanier.fxwavegenerationsimulator.models;

/**
 * This interface defines a display component for the wave simulation.
 * Which could be a printing console (when debugging) or a GUI component (graph).
 */
public interface WaveSimulationDisplay {
    /**
     * Update the display component with the latest wave simulation data.
     * @param dataPoints the array containing data points that builds the wave graph.
     */
    void update(double[] dataPoints);
}
