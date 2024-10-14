package edu.vanier.fxwavegenerationsimulator.exceptions;

/**
 * This represents an exception that is thrown when, while importing from past simulation or exporting current simulation,
 * the target path represents a directory.
 *
 * @author Qian Qian
 */
public class ChosenFileIsDirectoryException extends RuntimeException {
    public ChosenFileIsDirectoryException(String path) {
        super("The chosen path " + path + " is a directory, you cannot import data from a directory or export data to a directory.");
    }
}
