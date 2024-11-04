package edu.vanier.fxwavegenerationsimulator.controllers;

import edu.vanier.fxwavegenerationsimulator.models.Wave;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.*;

public class SoundController {
    private static final int MAX_VOLUME = 127;
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

    private byte getBufferValue(double amplitude, int waveCount) {
        return Integer.valueOf((int) Math.round(amplitude * MAX_VOLUME / waveCount)).byteValue();
    }

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

    public void addWave(Wave wave) throws LineUnavailableException, IOException {
        waves.add(wave);
        refreshBuffer();
        generateTone();
    }

    public void start() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}
