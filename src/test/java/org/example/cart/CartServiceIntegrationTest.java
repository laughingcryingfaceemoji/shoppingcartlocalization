package org.example.cart;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceIntegrationTest {

    private static Connection conn;

    @BeforeAll
    public static void setup() throws Exception {
        System.setProperty("db.url", "jdbc:h2:mem:cartdb2;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE cart_records (id INT AUTO_INCREMENT PRIMARY KEY, total_items INT NOT NULL, total_cost DOUBLE NOT NULL, language VARCHAR(10), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
            stmt.execute("CREATE TABLE cart_items (id INT AUTO_INCREMENT PRIMARY KEY, cart_record_id INT, item_number INT NOT NULL, price DOUBLE NOT NULL, quantity INT NOT NULL, subtotal DOUBLE NOT NULL);");
        }
    }

    @AfterAll
    public static void teardown() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    public void testSaveGetDeleteCart() throws Exception {
        List<ShoppingCartCalculator.Item> items = new ArrayList<>();
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("2.00"), 2));
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("3.50"), 1));

        int id = CartService.saveCart(3, new BigDecimal("7.50"), items, "en_US");
        assertTrue(id > 0);

        Object[] record = CartService.getCartRecord(id);
        assertNotNull(record);
        assertEquals(3, record[0]);
        assertEquals(0, ((BigDecimal)record[1]).compareTo(new BigDecimal("7.50")) );
        assertEquals("en_US", record[2]);

        boolean deleted = CartService.deleteCartRecord(id);
        assertTrue(deleted);
    }
}

