package edu.vanier.fxwavegenerationsimulator.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Database Connector Class that handles the connecting and locating of the database to the JavaFX Application.
 *
 * @author CihaoZhang
 */
public class DBConnector {
    /**
     * Setting up the connection with the SQLite database. This system uses a local directory to store the database.
     * @param database the name of the database
     * @return the connection to the database
     */
    public Connection Connector(String database) {
        try {
            // Define a local location for the database
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + "/AppData/" + database;

            // Create directories if they don't exist
            File dbDirectory = new File(userHome + "/AppData/");
            if (!dbDirectory.exists()) {
                dbDirectory.mkdirs();
            }
            // Connect to the SQLite database
            String url = "jdbc:sqlite:" + dbPath;
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(url);
        } catch (ClassNotFoundException | java.sql.SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
