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
    
    private DatabaseManager() {
        // Test database connectivity on initialization
        try (Connection testConn = DriverManager.getConnection(DB_URL)) {
            try (Statement stmt = testConn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            LOGGER.info("Database connection tested successfully: " + DB_FILE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to test database connection", e);
            throw new DatabaseException("Failed to test database connection", e);
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
     * Get a new database connection for thread-safe operations.
     * Each caller gets their own connection to prevent SQLite thread conflicts.
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            // Enable foreign keys for each new connection
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database connection", e);
            throw new DatabaseException("Failed to create database connection", e);
        }
    }
    
    /**
     * Close a specific database connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Get the database file path
     */
    public String getDatabasePath() {
        return DB_FILE;
    }
}