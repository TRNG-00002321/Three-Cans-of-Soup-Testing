package com.revature.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.revature.repository.DatabaseConnection;

public class DummyDataLoader {

    private DatabaseConnection databaseConnection;

    public DummyDataLoader() {
        databaseConnection = new DatabaseConnection();
    }

    public void restoreDatabase() throws SQLException {

        if (!databaseConnection.isTestMode()) {
            return;
        }

        try {
            // Close any existing connections before copying
            Connection connection = databaseConnection.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            // Get source and target paths
            String sourcePath = databaseConnection.getDatabasePath();
            String targetPath = databaseConnection.getTestDatabasePath(); // You'll need to add this method

            // Copy the file
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database copied from " + sourcePath + " to " + targetPath);

            // Reopen connection to the newly copied database
            connection = databaseConnection.getConnection();

            // Verify the copy was successful
            boolean hasData;
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                hasData = rs.next() && rs.getInt("count") > 0;
            }

            if (!hasData) {
                throw new SQLException("Database restoration failed: no data found in 'users' table.");
            }

        } catch (IOException e) {
            throw new SQLException("Failed to copy database file: " + e.getMessage(), e);
        }
    }
}
