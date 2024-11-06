package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.models.Wave;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.*;

/**
 * The controller class that handles the sound generation (from Wave data) and play.
 *
 * @author Qian Qian
 */
public class SoundController {
    /**
     * The maximum volume that is allowed for the combined waves.
     * (To prevent overpowered sound for the computer)
     */
    private static final int MAX_VOLUME = 127;
    /**
     * The sample rate for the audio data.
     */
    private static final int SAMPLE_RATE = 44100;

    /**
     * List that contains all Wave objects.
     */
    private final List<Wave> waves;

    /**
     * The audio data clip to be played.
     */
    Clip clip;

    /**
     * Buffer to store the audio data.
     */
    byte[] buffer;

    public SoundController() throws LineUnavailableException, IOException {
        waves = new ArrayList<>();
        this.clip = AudioSystem.getClip();
        refreshBuffer();
        generateTone();
    }

    /**
     * Get the byte value of the amplitude that will be put into the sound data buffer.
     * @param amplitude the amplitude of the wave
     * @param waveCount the total number of waves
     * @return the byte value of the amplitude
     */
    private byte getBufferValue(double amplitude, int waveCount) {
        // The byte value is first converted from a range of -1 to 1 with decimal
        // to a range of -127 to 127 (byte range) so it can be put into the buffer which is a byte array.
        // It is then divided by the number of wave existing so all waves amplitude added up will not exceed the maximum volume.
        return Integer.valueOf((int) Math.round(amplitude * MAX_VOLUME / waveCount)).byteValue();
    }

    /**
     * Refresh the sound data buffer by populating it with new calculated wave amplitude data.
     */
    private void refreshBuffer() {
        buffer = new byte[clip.getBufferSize()];
        for (int i = 0; i < buffer.length; i++) {
            double amplitude = 0;
            for (Wave wave : waves) {
                amplitude += wave.amplitude(0, i / (double) SAMPLE_RATE);
            }
            buffer[i] = getBufferValue(amplitude, waves.size());
        }
    }

    /**
     * Generate the sound from the wave amplitude data in the buffer.
     */
    private void generateTone() throws LineUnavailableException, IOException {
        clip.stop();
        clip.close();

        AudioFormat audioFormat = new AudioFormat(
                SAMPLE_RATE,  // sample rate
                8,  // sample size in bits
                1,  // channels
                true,  // signed
                false  // bigEndian
        );

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        AudioInputStream ais = new AudioInputStream(
                byteArrayInputStream,
                audioFormat,
                buffer.length
        );
        
        clip.open(ais);
    }

    /**
     * Add a new wave to the sound controller.
     * @param wave the wave to be added
     */
    public void addWave(Wave wave) throws LineUnavailableException, IOException {
        // Add wave to the list.
        waves.add(wave);
        // Refresh the buffer and generate the sound.
        refreshBuffer();
        generateTone();
    }

    /**
     * Start playing the generated sound.
     */
    public void start() {
        if (clip != null) {
            // The sound will be played infinitely.
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Stop playing the generated sound.
     */
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}
