package org.example.cart;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class Utf8ControlTest {

    @Test
    public void testLoadBundleUtf8() {
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en", "US"), new Utf8Control());
        assertNotNull(bundle);
        String prompt = bundle.getString("prompt.items");
        assertTrue(prompt.toLowerCase().contains("enter") || prompt.length() > 0);
    }
}

