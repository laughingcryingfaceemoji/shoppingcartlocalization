package org.example.cart;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple calculation tests for Shopping Cart
 */
public class SimpleCalculationTest {

    @Test
    public void testSingleItemTotal() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(
            new BigDecimal("10.00"),
            2
        );
        assertEquals(new BigDecimal("20.00"), item.total());
    }

    @Test
    public void testTwoItemsTotal() {
        ShoppingCartCalculator.Item item1 = new ShoppingCartCalculator.Item(new BigDecimal("5.00"), 2);
        ShoppingCartCalculator.Item item2 = new ShoppingCartCalculator.Item(new BigDecimal("3.00"), 3);

        BigDecimal total = ShoppingCartCalculator.calculateTotal(
            Arrays.asList(item1, item2)
        );
        assertEquals(new BigDecimal("19.00"), total);
    }

    @Test
    public void testThreeItemsTotal() {
        ShoppingCartCalculator.Item item1 = new ShoppingCartCalculator.Item(new BigDecimal("10.00"), 1);
        ShoppingCartCalculator.Item item2 = new ShoppingCartCalculator.Item(new BigDecimal("5.00"), 2);
        ShoppingCartCalculator.Item item3 = new ShoppingCartCalculator.Item(new BigDecimal("2.50"), 2);

        BigDecimal total = ShoppingCartCalculator.calculateTotal(
            Arrays.asList(item1, item2, item3)
        );
        assertEquals(new BigDecimal("25.00"), total);
    }

    @Test
    public void testEmptyCart() {
        BigDecimal total = ShoppingCartCalculator.calculateTotal(Arrays.asList());
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    public void testDecimalPrices() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(
            new BigDecimal("9.99"),
            3
        );
        assertEquals(new BigDecimal("29.97"), item.total());
    }
}

