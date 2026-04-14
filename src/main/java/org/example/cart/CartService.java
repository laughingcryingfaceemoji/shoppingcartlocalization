package org.example.cart;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * CartService class for managing shopping cart database operations
 * Handles saving cart records, items, and related data to the database
 */
public class CartService {

    /**
     * Saves a complete shopping cart to the database
     *
     * @param totalItems the total number of items in the cart
     * @param totalCost the total cost of all items
     * @param items list of items with their prices and quantities
     * @param language the language code (e.g., "en_US", "fi_FI")
     * @return the ID of the saved cart record
     * @throws SQLException if database operation fails
     */
    public static int saveCart(int totalItems, BigDecimal totalCost, List<ShoppingCartCalculator.Item> items, String language) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert main cart record
            int cartRecordId = insertCartRecord(conn, totalItems, totalCost, language);

            // Insert individual items
            for (int i = 0; i < items.size(); i++) {
                ShoppingCartCalculator.Item item = items.get(i);
                insertCartItem(conn, cartRecordId, i + 1, item.getPrice(), item.getQuantity(), item.total());
            }

            conn.commit();
            System.out.println("Cart saved successfully with ID: " + cartRecordId);
            return cartRecordId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new SQLException("Error saving cart: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    /**
     * Inserts a cart record into the cart_records table
     *
     * @param conn database connection
     * @param totalItems number of items
     * @param totalCost total cost
     * @param language language code
     * @return the generated ID of the cart record
     * @throws SQLException if database operation fails
     */
    private static int insertCartRecord(Connection conn, int totalItems, BigDecimal totalCost, String language) throws SQLException {
        String sql = "INSERT INTO cart_records (total_items, total_cost, language, created_at) VALUES (?, ?, ?, NOW())";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, totalItems);
            pstmt.setBigDecimal(2, totalCost);
            pstmt.setString(3, language);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating cart record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating cart record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Inserts a cart item into the cart_items table
     *
     * @param conn database connection
     * @param cartRecordId the ID of the parent cart record
     * @param itemNumber the item number
     * @param price the price of the item
     * @param quantity the quantity
     * @param subtotal the subtotal (price × quantity)
     * @throws SQLException if database operation fails
     */
    private static void insertCartItem(Connection conn, int cartRecordId, int itemNumber, BigDecimal price, int quantity, BigDecimal subtotal) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartRecordId);
            pstmt.setInt(2, itemNumber);
            pstmt.setBigDecimal(3, price);
            pstmt.setInt(4, quantity);
            pstmt.setBigDecimal(5, subtotal);

            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a cart record by ID from the database
     *
     * @param cartRecordId the ID of the cart record
     * @return an array containing [totalItems, totalCost, language] or null if not found
     * @throws SQLException if database operation fails
     */
    public static Object[] getCartRecord(int cartRecordId) throws SQLException {
        String sql = "SELECT total_items, total_cost, language FROM cart_records WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartRecordId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("total_items"),
                            rs.getBigDecimal("total_cost"),
                            rs.getString("language")
                    };
                }
            }
        }
        return null;
    }

    /**
     * Deletes a cart record and all associated items
     *
     * @param cartRecordId the ID of the cart record to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public static boolean deleteCartRecord(int cartRecordId) throws SQLException {
        String sql = "DELETE FROM cart_records WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartRecordId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}

