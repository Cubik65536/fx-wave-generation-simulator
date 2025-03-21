package edu.vanier.fxwavegenerationsimulator.models;

import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;

/**
 * This class represents a discrete wave in the simulation.
 * It have the following attributes to represent the wave: type (an `enum`, SIN or COS),
 * frequency (Hz, takes a integer), and amplitude (a double value that takes a value between -1 and 1).
 *
 * @author Qian Qian
 */
public class Wave {
    /**
     * The type of the wave (sin/cos).
     */
    private WaveTypes waveType;
    /**
     * The frequency of the wave (in Hz).
     */
    private int frequency;
    /**
     * The amplitude of the wave (a value between -1 and 1).
     */
    private double amplitude;
    /**
     * The colour code of the wave.
     * This will be automatically and randomly assigned when the wave is added to the simulation.
     */
    final private Color color;


    /**
     * Instantiate a discrete wave object with information provided, with a random colour code.
     * @param waveType The type of the wave (sin/cos).
     * @param frequency The frequency of the wave (in Hz).
     * @param amplitude The amplitude of the wave (a value between -1 and 1).
     * @throws IllegalArgumentException If the amplitude is not between -1 and 1.
     */
    public Wave(WaveTypes waveType, int frequency, double amplitude) throws IllegalArgumentException {
        this(waveType, frequency, amplitude, new Color());
    }

    /**
     * Instantiate a discrete wave object with information provided.
     * @param waveType The type of the wave (sin/cos).
     * @param frequency The frequency of the wave (in Hz).
     * @param amplitude The amplitude of the wave (a value between -1 and 1).
     * @param color The colour code of the wave.
     * @throws IllegalArgumentException If the amplitude is not between -1 and 1.
     */
    public Wave(WaveTypes waveType, int frequency, double amplitude, Color color) throws IllegalArgumentException {
        if (waveType != WaveTypes.DUMMY || frequency != -1) {
            if (amplitude < -1 || amplitude > 1) {
                throw new IllegalArgumentException("Amplitude must be between -1 and 1.");
            }
            if (frequency <= 0) {
                throw new IllegalArgumentException("Frequency must be greater than 0.");
            }
        }

        this.waveType = waveType;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.color = color;
    }

    /**
     * Calculate the wavelength of the wave, based on the frequency and the
     * sound speed as wave speed (assume the sound speed is 343 m/s).
     * @return The wavelength of the wave (in meters).
     */
    private double calculateWavelength() {
        double soundSpeed = 343.0; // m/s
        return soundSpeed / frequency;
    }

    /**
     * Calculate the position of the wave particle at a given position and time.
     * y(x, t) = A * sin(2pi * f * t - 2pi * x / lambda)
     * @param x the position of the wave (in meters)
     * @param t the time of the wave (in seconds)
     * @return the position of the wave particle at the given position and time
     */
    public double amplitude(double x, double t) {
        double omega = 2 * Math.PI * frequency;
        double k = 2 * Math.PI / calculateWavelength();
        double phase = omega * t - k * x;
        return amplitude * switch (waveType) {
            case SIN -> Math.sin(phase);
            case COS -> Math.cos(phase);
            case DUMMY -> 0;
        };
    }

    /**
     * Get the type of the wave.
     * @return The type of the wave.
     */
    public WaveTypes getWaveType() {
        return waveType;
    }

    /**
     * Change the type of the wave.
     * If the current wave type is SIN, change it to COS.
     * If the current wave type is COS, change it to SIN.
     * If it is a DUMMY wave, it should stay as a DUMMY wave.
     */
    public void switchWaveType() {
        this.waveType = switch (waveType) {
            case SIN -> WaveTypes.COS;
            case COS -> WaveTypes.SIN;
            default -> waveType;
        };
    }

    /**
     * Get the frequency of the wave.
     * @return The frequency (in Hz).
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Set a new frequency for the wave.
     * @param frequency The new frequency (in Hz).
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Get the amplitude of the wave.
     * @return The amplitude (a value between -1 and 1).
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Set a new amplitude for the wave.
     * @param amplitude The new amplitude (a value between -1 and 1).
     */
    public void setAmplitude(double amplitude) throws IllegalArgumentException {
        if (amplitude < 0 || amplitude > 1) {
            throw new IllegalArgumentException("Amplitude must be between -1 and 1.");
        }
        this.amplitude = amplitude;
    }

    /**
     * Get the colour code of the wave.
     * @return The colour code of the wave.
     */
    public Color getColor() {
        return color;
    }
}
