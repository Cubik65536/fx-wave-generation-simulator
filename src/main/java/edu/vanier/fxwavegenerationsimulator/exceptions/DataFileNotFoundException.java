package edu.vanier.fxwavegenerationsimulator.exceptions;

/**
 * This represents an exception that is thrown when, while importing from past simulation,
 * the target data file is not found.
 *
 * @author Qian Qian
 */
public class DataFileNotFoundException extends Exception{
    public DataFileNotFoundException(String path) {
        super("The exported data file at " + path + " is not found.");
    }
}
