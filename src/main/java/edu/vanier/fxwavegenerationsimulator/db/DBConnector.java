package edu.vanier.fxwavegenerationsimulator.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Database Connector Class that handles the connecting and locating of the database to the JavaFX Application.
 *
 * @author CihaoZhang
 */
public class DBConnector {

    public Connection Connector(String database) {
        try {
            String url = String.valueOf(getClass().getResource("/data/" + database));
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + url);
            return conn;
        } catch (ClassNotFoundException | java.sql.SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
