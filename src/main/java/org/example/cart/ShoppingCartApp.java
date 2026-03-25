package org.example.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ShoppingCartApp {
    public static void main(String[] args) {
        // Ensure stdout/stderr use UTF-8 so non-ASCII resource bundle strings print correctly
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // JVM should support UTF-8; if not, fall back to default streams
        }
        // Ask user for language
        System.out.println("Select language / Valitse kieli / Välj språk / 言語を選択してください:");
        System.out.println("1) English");
        System.out.println("2) Finnish (suomi)");
        System.out.println("3) Swedish (svenska)");
        System.out.println("4) Japanese (日本語)");
        System.out.print("Choice: ");

        Scanner scanner = new Scanner(System.in, "UTF-8");
        String choice = scanner.nextLine().trim();
        Locale locale;
        switch (choice) {
            case "2": locale = new Locale("fi", "FI"); break;
            case "3": locale = new Locale("sv", "SE"); break;
            case "4": locale = new Locale("ja", "JP"); break;
            case "1":
            default: locale = new Locale("en", "US"); break;
        }

        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", locale);

        System.out.println(messages.getString("prompt.items"));
        int n;
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                n = Integer.parseInt(line);
                if (n < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println(messages.getString("error.invalid_number"));
                System.out.println(messages.getString("prompt.items"));
            }
        }

        List<ShoppingCartCalculator.Item> items = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.println(String.format(messages.getString("prompt.price_for"), i));
            BigDecimal price;
            while (true) {
                String p = scanner.nextLine().trim();
                try {
                    price = new BigDecimal(p);
                    if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(messages.getString("error.invalid_price"));
                    System.out.println(String.format(messages.getString("prompt.price_for"), i));
                }
            }

            System.out.println(String.format(messages.getString("prompt.quantity_for"), i));
            int qty;
            while (true) {
                String q = scanner.nextLine().trim();
                try {
                    qty = Integer.parseInt(q);
                    if (qty < 0) throw new NumberFormatException();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(messages.getString("error.invalid_quantity"));
                    System.out.println(String.format(messages.getString("prompt.quantity_for"), i));
                }
            }

            items.add(new ShoppingCartCalculator.Item(price, qty));
        }

        BigDecimal total = ShoppingCartCalculator.calculateTotal(items);
        System.out.println(messages.getString("label.total") + " " + total);

        scanner.close();
    }
}
