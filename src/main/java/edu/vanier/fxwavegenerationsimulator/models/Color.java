package edu.vanier.fxwavegenerationsimulator.models;

import java.util.Random;

/**
 * This class represents a color that can be used to distinguish between different waves in the simulation.
 * The color is represented by the RGB colour value, each ranging from 0 to 255.
 *
 * @Author Qian Qian
 */
public class Color {
    /**
     * The red value of the color.
     */
    private int red;
    /**
     * The green value of the color.
     */
    private int green;
    /**
     * The blue value of the color.
     */
    private int blue;

    /**
     * Instantiate a color object with the RGB values provided.
     * @param red The red value of the color.
     * @param green The green value of the color.
     * @param blue The blue value of the color.
     */
    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Randomly generate a color object with random RGB values.
     */
    public Color() {
        Random random = new Random();

        this.red = random.nextInt(0, 256);
        this.green = random.nextInt(0, 256);
        this.blue = random.nextInt(0, 256);
    }

    /**
     * Get the red value of the color.
     * @return The red value of the color.
     */
    public int getRed() {
        return red;
    }

    /**
     * Get the green value of the color.
     * @return The green value of the color.
     */
    public int getGreen() {
        return green;
    }

    /**
     * Get the blue value of the color.
     * @return The blue value of the color.
     */
    public int getBlue() {
        return blue;
    }
}
