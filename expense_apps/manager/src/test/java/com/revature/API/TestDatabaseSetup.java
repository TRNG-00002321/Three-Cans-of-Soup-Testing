package com.revature.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import com.revature.repository.DatabaseConnection;

/**
 * Test Database Setup Helper for Integration Tests
 *
 *
 * Provides setup and teardown utilities for integration tests using a separate
 * test database.
 */
public class TestDatabaseSetup {

    public static final String TEST_DB_NAME = "test_expense_manager.db";
    public static final String TEST_DIR = "expense_integration_test";
    private static Path testDbPath;
    private static DatabaseConnection testDbConnection;

    /**
     * Initialize the test database with schema and seed data. Creates a fresh
     * test database in a temp directory.
     */
    public static DatabaseConnection initializeTestDatabase() throws SQLException, IOException {
        // Create temp directory for test database
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        testDbPath = tempDir.resolve(TEST_DB_NAME);

        // Create database connection with test path
        testDbConnection = new DatabaseConnection(testDbPath.toString());

        // Create tables
        createTables();

        // Load and execute seed data
        seedDatabase();

        return testDbConnection;
    }

    /**
     * Create database tables matching the production schema.
     */
    private static void createTables() throws SQLException {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                )
                """;

        String createExpensesTable = """
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    description TEXT NOT NULL,
                    date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users (id)
                )
                """;

        String createApprovalsTable = """
                CREATE TABLE IF NOT EXISTS approvals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    expense_id INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'pending',
                    reviewer INTEGER,
                    comment TEXT,
                    review_date TEXT,
                    FOREIGN KEY (expense_id) REFERENCES expenses (id),
                    FOREIGN KEY (reviewer) REFERENCES users (id)
                )
                """;

        try (Connection conn = testDbConnection.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createExpensesTable);
            stmt.execute(createApprovalsTable);
        }
    }

    /**
     * Load and execute seed SQL from resources.
     */
    private static void seedDatabase() throws SQLException, IOException {
        // Read seed SQL from resources
        String seedSql;
        try (InputStream is = TestDatabaseSetup.class.getResourceAsStream(
                "/seed_data.sql")) {
            if (is == null) {
                throw new IOException("Could not find seed_data.sql in resources");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                seedSql = reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
            }
        }

        // Execute seed SQL
        try (Connection conn = testDbConnection.getConnection(); Statement stmt = conn.createStatement()) {
            // Split by semicolon and execute each statement
            for (String sql : seedSql.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    /**
     * Clean up test database after tests.
     */
    public static void cleanup() {
        if (testDbPath != null) {
            try {
                Files.deleteIfExists(testDbPath);
                Files.deleteIfExists(testDbPath.getParent());
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Get the test database connection.
     */
    public static DatabaseConnection getTestDbConnection() {
        return testDbConnection;
    }

    /**
     * Get the test database path.
     */
    public static Path getTestDbPath() {
        return testDbPath;
    }
}
