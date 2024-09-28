package edu.vanier.fxwavegenerationsimulator.models;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the generation logic behind wave calculation.
 * It creates a list of the Wave object to be added to the simulation and allows to combine waves together by adding
 * its amplitudes together.
 * @author CihaoZhang
 */
public class WaveGenerator {
    /**
     * List that contains all Wave objects.
     */
    private List<Wave> waves;

    /**
     * Constructor for Waves Generator.
     */
    public WaveGenerator() {
        waves = new ArrayList<>();
    }

    /**
     * Adds a wave to the Generator.
     * @param wave
     */
    public void addWave(Wave wave) {
        waves.add(wave);
    }

    /**
     * Calculates the amplitude of the combined waves at a given position and time.
     * @param pos
     * @param time
     * @return the amplitude of the combined waves
     */
    public double combineWaves (double pos, double time) {
        double ampFinal = 0;
        for (Wave wave : waves) {
            ampFinal += wave.amplitude(pos, time);
        }
        return ampFinal;
    }
}
