package org.example.cart;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * LocalizationService class for fetching localized UI strings from the database
 * Manages language-specific UI text and provides dynamic localization support
 */
public class LocalizationService {

    /**
     * Fetches all localized strings for a specific language from the database
     *
     * @param language the language code (e.g., "en_US", "fi_FI", "sv_SE", "ja_JP", "ar_AR")
     * @return a Map of key-value pairs for the localized strings
     * @throws SQLException if database operation fails
     */
    public static Map<String, String> getLocalizationStrings(String language) throws SQLException {
        Map<String, String> strings = new HashMap<>();
        String sql;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String product = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (product.contains("h2")) {
                sql = "SELECT \"key\", \"value\" FROM localization_strings WHERE language = ?";
            } else {
                sql = "SELECT `key`, value FROM localization_strings WHERE language = ?";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, language);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        String value = rs.getString(2);
                        strings.put(key, value);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error fetching localization strings for language '" + language + "': " + e.getMessage(), e);
        }

        return strings;
    }

    /**
     * Inserts a localization string into the database
     *
     * @param key the key for the string (e.g., "label.price")
     * @param value the localized value
     * @param language the language code
     * @throws SQLException if database operation fails
     */
    public static void insertLocalizationString(String key, String value, String language) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String product = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (product.contains("h2")) {
                // H2: try update then insert
                String updateSql = "UPDATE localization_strings SET \"value\" = ? WHERE \"key\" = ? AND language = ?";
                try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                    up.setString(1, value);
                    up.setString(2, key);
                    up.setString(3, language);
                    int updated = up.executeUpdate();
                    if (updated > 0) return;
                }

                String insertSql = "INSERT INTO localization_strings (\"key\", \"value\", language) VALUES (?, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setString(1, key);
                    ins.setString(2, value);
                    ins.setString(3, language);
                    ins.executeUpdate();
                    return;
                }
            } else {
                String sql = "INSERT INTO localization_strings (`key`, value, language) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, key);
                    pstmt.setString(2, value);
                    pstmt.setString(3, language);
                    pstmt.setString(4, value);

                    pstmt.executeUpdate();
                    return;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error inserting localization string: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if localization data exists for a specific language
     *
     * @param language the language code
     * @return true if localization data exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public static boolean hasLocalizationData(String language) throws SQLException {
        String sql = "SELECT COUNT(*) FROM localization_strings WHERE language = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, language);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error checking localization data: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Initializes the database with localization strings from ResourceBundle
     * This is useful for populating the database on first run
     *
     * @param language the language code
     * @throws SQLException if database operation fails
     */
    public static void initializeFromResourceBundle(String language, java.util.ResourceBundle bundle) throws SQLException {
        for (String key : bundle.keySet()) {
            String value = bundle.getString(key);
            insertLocalizationString(key, value, language);
        }
    }

    /**
     * Deletes all localization strings for a specific language
     *
     * @param language the language code
     * @throws SQLException if database operation fails
     */
    public static void deleteLanguageData(String language) throws SQLException {
        String sql = "DELETE FROM localization_strings WHERE language = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, language);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error deleting localization data: " + e.getMessage(), e);
        }
    }
}
