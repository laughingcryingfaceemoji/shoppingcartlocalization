package org.example.cart;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizationServiceTest {

    private static Connection conn;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        // Start in-memory H2 and set system properties for DatabaseConnection
        System.setProperty("db.url", "jdbc:h2:mem:cartdb;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            // Use H2-compatible identifiers for reserved names by quoting them
            stmt.execute("CREATE TABLE localization_strings (id IDENTITY PRIMARY KEY, \"key\" VARCHAR(100) NOT NULL, \"value\" VARCHAR(255) NOT NULL, language VARCHAR(10) NOT NULL);");
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    public void testInsertAndFetchLocalization() throws Exception {
        LocalizationService.insertLocalizationString("label.test", "Test Value", "en_US");
        assertTrue(LocalizationService.hasLocalizationData("en_US"));

        Map<String, String> map = LocalizationService.getLocalizationStrings("en_US");
        assertEquals("Test Value", map.get("label.test"));
    }

    @Test
    public void testDeleteLanguageData() throws Exception {
        LocalizationService.insertLocalizationString("label.to.delete", "DeleteMe", "fi_FI");
        assertTrue(LocalizationService.hasLocalizationData("fi_FI"));
        LocalizationService.deleteLanguageData("fi_FI");
        assertFalse(LocalizationService.hasLocalizationData("fi_FI"));
    }
}
