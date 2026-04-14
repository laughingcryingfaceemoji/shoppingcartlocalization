package org.example.cart;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Simple calculation tests for Shopping Cart
 * Tests core shopping cart calculation logic without database dependencies
 */
public class SimpleCalculationTest {

    @Test
    public void testSingleItemTotal() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(
            new BigDecimal("10.00"),
            2
        );
        assertEquals(0, item.total().compareTo(new BigDecimal("20.00")));
    }

    @Test
    public void testTwoItemsTotal() {
        ShoppingCartCalculator.Item item1 = new ShoppingCartCalculator.Item(new BigDecimal("5.00"), 2);
        ShoppingCartCalculator.Item item2 = new ShoppingCartCalculator.Item(new BigDecimal("3.00"), 3);

        BigDecimal total = ShoppingCartCalculator.calculateTotal(
            Arrays.asList(item1, item2)
        );
        assertEquals(0, total.compareTo(new BigDecimal("19.00")));
    }

    @Test
    public void testThreeItemsTotal() {
        ShoppingCartCalculator.Item item1 = new ShoppingCartCalculator.Item(new BigDecimal("10.00"), 1);
        ShoppingCartCalculator.Item item2 = new ShoppingCartCalculator.Item(new BigDecimal("5.00"), 2);
        ShoppingCartCalculator.Item item3 = new ShoppingCartCalculator.Item(new BigDecimal("2.50"), 2);

        BigDecimal total = ShoppingCartCalculator.calculateTotal(
            Arrays.asList(item1, item2, item3)
        );
        assertEquals(0, total.compareTo(new BigDecimal("25.00")));
    }

    @Test
    public void testEmptyCart() {
        BigDecimal total = ShoppingCartCalculator.calculateTotal(Arrays.asList());
        assertEquals(0, total.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testDecimalPrices() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(
            new BigDecimal("9.99"),
            3
        );
        assertEquals(0, item.total().compareTo(new BigDecimal("29.97")));
    }

    @Test
    public void testZeroQuantity() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(new BigDecimal("10.00"), 0);
        assertEquals(0, item.total().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testZeroPrice() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(BigDecimal.ZERO, 10);
        assertEquals(0, item.total().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testLargePriceAndQuantity() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(new BigDecimal("999.99"), 100);
        assertEquals(0, item.total().compareTo(new BigDecimal("99999.00")));
    }

    @Test
    public void testComplexRealWorldScenario() {
        // Simulating a real shopping scenario
        List<ShoppingCartCalculator.Item> items = new ArrayList<>();
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("29.99"), 2));   // 59.98 (shoes, qty 2)
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("14.99"), 3));   // 44.97 (shirts, qty 3)
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("49.99"), 1));   // 49.99 (jeans, qty 1)
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("9.99"), 5));    // 49.95 (socks, qty 5)

        BigDecimal total = ShoppingCartCalculator.calculateTotal(items);
        assertEquals(0, total.compareTo(new BigDecimal("204.89")));
    }

    @Test
    public void testItemWithHighPrecisionDecimals() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(new BigDecimal("3.33"), 3);
        assertEquals(0, item.total().compareTo(new BigDecimal("9.99")));
    }

    @Test
    public void testLargeNumberOfItems() {
        List<ShoppingCartCalculator.Item> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new ShoppingCartCalculator.Item(new BigDecimal("1.00"), 1));
        }
        BigDecimal total = ShoppingCartCalculator.calculateTotal(items);
        assertEquals(0, total.compareTo(new BigDecimal("100.00")));
    }

    @Test
    public void testItemGettersWork() {
        BigDecimal price = new BigDecimal("15.50");
        int quantity = 4;
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(price, quantity);

        assertEquals(price, item.getPrice());
        assertEquals(quantity, item.getQuantity());
    }

    @Test
    public void testMixedZeroAndNonZeroQuantities() {
        List<ShoppingCartCalculator.Item> items = new ArrayList<>();
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("10.00"), 2));  // 20.00
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("5.00"), 0));   // 0.00
        items.add(new ShoppingCartCalculator.Item(new BigDecimal("3.00"), 5));   // 15.00

        BigDecimal total = ShoppingCartCalculator.calculateTotal(items);
        assertEquals(0, total.compareTo(new BigDecimal("35.00")));
    }
}

