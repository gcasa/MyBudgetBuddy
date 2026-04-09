package com.mybudgetbuddy.infrastructure.database;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages SQLite database connections for the application.
 * Provides singleton connection management and database initialization.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_FILE = Paths.get(System.getProperty("user.home"), ".mybudgetbuddy.db").toString();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    
    private static volatile DatabaseManager instance;
    private volatile Connection connection;
    
    private DatabaseManager() {
        try {
            // Create connection (JDBC 4.0+ auto-loads drivers)
            connection = DriverManager.getConnection(DB_URL);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            LOGGER.info("Database connection established: " + DB_FILE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }
    
    /**
     * Get singleton instance of DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Get the database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                synchronized (this) {
                    // Double-check locking
                    if (connection == null || connection.isClosed()) {
                        connection = DriverManager.getConnection(DB_URL);
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("PRAGMA foreign_keys = ON");
                        }
                    }
                }
            }
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get database connection", e);
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    /**
     * Close the database connection
     */
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }
    
    /**
     * Get the database file path
     */
    public String getDatabasePath() {
        return DB_FILE;
    }
}