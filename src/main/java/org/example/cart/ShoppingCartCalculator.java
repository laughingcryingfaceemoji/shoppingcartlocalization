package org.example.cart;

import java.math.BigDecimal;
import java.util.List;

public class ShoppingCartCalculator {

    public static class Item {
        private final BigDecimal price;
        private final int quantity;

        public Item(BigDecimal price, int quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal total() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static BigDecimal calculateTotal(List<Item> items) {
        return items.stream()
                .map(Item::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

