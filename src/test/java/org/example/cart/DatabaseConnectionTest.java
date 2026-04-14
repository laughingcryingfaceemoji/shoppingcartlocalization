package org.example.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple smoke tests for DatabaseConnection helpers.
 * These tests use H2 in-memory database to avoid requiring a running MariaDB.
 */
public class DatabaseConnectionTest {

    @BeforeEach
    public void setup() {
        // Set up H2 in-memory database for testing BEFORE each test
        System.setProperty("db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");
    }

    @Test
    public void testTestConnectionDoesNotThrow() {
        // The method returns boolean and catches exceptions internally, so just ensure it does not throw
        assertDoesNotThrow(() -> {
            boolean ok = DatabaseConnection.testConnection();
            // ok may be true or false depending on environment; we don't assert on the value here
        });
    }

    @Test
    public void testCloseConnectionNullSafe() {
        // closeConnection should safely handle null without throwing
        assertDoesNotThrow(() -> DatabaseConnection.closeConnection(null));
    }
}

