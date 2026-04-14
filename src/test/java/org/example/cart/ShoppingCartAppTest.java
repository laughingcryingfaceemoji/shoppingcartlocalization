package org.example.cart;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartAppTest {

    @Test
    public void testMainCalculatesTotal() throws Exception {
        String input = "1\n1\n2.50\n3\n"; // choose English, 1 item, price 2.50, qty 3
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        try {
            System.setIn(in);
            System.setOut(new PrintStream(out, true, "UTF-8"));

            ShoppingCartApp.main(new String[0]);

            String output = out.toString("UTF-8");
            assertTrue(output.contains("Total cost") || output.contains("Total cost:" ) || output.contains("Total"));
            assertTrue(output.contains("7.50") || output.contains("7.5"));
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
    }
}

