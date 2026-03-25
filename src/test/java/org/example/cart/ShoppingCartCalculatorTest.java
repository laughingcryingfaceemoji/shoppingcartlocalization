package org.example.cart;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartCalculatorTest {

    @Test
    public void testItemTotal() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(new BigDecimal("2.50"), 3);
        assertEquals(new BigDecimal("7.50"), item.total());
    }

    @Test
    public void testCalculateTotalMultipleItems() {
        ShoppingCartCalculator.Item a = new ShoppingCartCalculator.Item(new BigDecimal("1.00"), 2);
        ShoppingCartCalculator.Item b = new ShoppingCartCalculator.Item(new BigDecimal("2.50"), 4);
        assertEquals(new BigDecimal("12.00"), ShoppingCartCalculator.calculateTotal(Arrays.asList(a, b)));
    }

    @Test
    public void testCalculateTotalEmpty() {
        assertEquals(BigDecimal.ZERO, ShoppingCartCalculator.calculateTotal(Arrays.asList()));
    }

    @Test
    public void testNegativeQuantity() {
        ShoppingCartCalculator.Item item = new ShoppingCartCalculator.Item(new BigDecimal("1.00"), -1);
        assertEquals(new BigDecimal("-1.00"), item.total());
    }
}

