package org.example.cart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection class for managing MySQL/MariaDB connections
 * Provides centralized database connection handling and configuration
 */
public class DatabaseConnection {

    // Defaults (can be overridden via system properties or environment variables)
    private static final String DEFAULT_DB_URL = "jdbc:mariadb://localhost:3306/shopping_cart_localization";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "K1224!?";

    private static String getConfiguredUrl() {
        // Check system property first, then environment variable, then default
        String prop = System.getProperty("db.url");
        if (prop != null) return prop;
        String env = System.getenv("DB_URL");
        if (env != null) return env;
        return DEFAULT_DB_URL;
    }

    private static String getConfiguredUser() {
        String prop = System.getProperty("db.user");
        if (prop != null) return prop;
        String env = System.getenv("DB_USER");
        if (env != null) return env;
        return DEFAULT_DB_USER;
    }

    private static String getConfiguredPassword() {
        String prop = System.getProperty("db.password");
        if (prop != null) return prop;
        String env = System.getenv("DB_PASSWORD");
        if (env != null) return env;
        return DEFAULT_DB_PASSWORD;
    }

    /**
     * Gets a connection to the database
     *
     * @return Connection object to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Attempt to load MariaDB driver if available (for production)
            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
                // It's fine for tests that use another driver (H2, etc.)
            }

            String url = getConfiguredUrl();
            String user = getConfiguredUser();
            String password = getConfiguredPassword();

            System.out.println("Attempting to connect to: " + url + " as user: " + user);

            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Tests the database connection
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes a database connection safely
     *
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
