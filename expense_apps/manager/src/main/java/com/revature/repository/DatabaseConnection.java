package com.revature.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Database connection utility for SQLite database. Handles connection
 * management for the shared expense manager database.
 */
public class DatabaseConnection {

    private final String databasePath;
    private boolean testMode;
    private final String testDatabasePath;

    public DatabaseConnection() {
        //for local testing
        Dotenv dotenv = Dotenv.load();
        testMode = dotenv.get("TEST_MODE", "false").equals("true");
        databasePath = System.getenv("DATABASE_PATH") != null ? System.getenv("DATABASE_PATH") : dotenv.get("DATABASE_PATH", "./expense_manager.db");
        testDatabasePath = System.getenv("TEST_DATABASE_PATH") != null ? System.getenv("TEST_DATABASE_PATH") : dotenv.get("TEST_DATABASE_PATH", "./test.db");
    }

    public DatabaseConnection(String databasePath, String testDatabasePath) {
        this.databasePath = databasePath;
        this.testDatabasePath = testDatabasePath;
    }

    /**
     * Get a database connection.
     *
     * @return SQLite database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (testMode) {
            // Use file-based test database instead of in-memory
            String testDbPath = "jdbc:sqlite:" + testDatabasePath;
            return DriverManager.getConnection(testDbPath);
        } else {
            String url = "jdbc:sqlite:" + databasePath;
            return DriverManager.getConnection(url);
        }
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public String getTestDatabasePath() {
        return testDatabasePath;
    }

    public boolean isTestMode() {
        return testMode;
    }
}
