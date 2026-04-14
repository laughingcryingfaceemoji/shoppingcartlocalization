package org.example.cart;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests to exercise non-UI logic in ShoppingCartGUI.ItemRow and helpers.
 * We don't launch the full JavaFX application; instead we initialize JavaFX toolkit
 * with JFXPanel and construct the GUI class to call methods.
 */
public class ShoppingCartGUITest {

    @BeforeAll
    public static void initJFX() {
        // Initializes JavaFX runtime for tests that construct controls
        new JFXPanel();
    }

    private void runOnFxAndWait(Runnable r) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try { r.run(); } finally { latch.countDown(); }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) throw new RuntimeException("FX timeout");
    }

    @Test
    public void testGetLocaleFromSelection() {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        assertEquals(new Locale("en", "US"), gui.getLocaleFromSelection("English"));
        assertEquals(new Locale("fi", "FI"), gui.getLocaleFromSelection("Suomi"));
        assertEquals(new Locale("sv", "SE"), gui.getLocaleFromSelection("Svenska"));
        assertEquals(new Locale("ja", "JP"), gui.getLocaleFromSelection("日本語"));
        assertEquals(new Locale("ar", "AR"), gui.getLocaleFromSelection("العربية"));
    }

    @Test
    public void testGetLanguageCodeForLocale() {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        assertEquals("en_US", gui.getLanguageCodeForLocale(new Locale("en", "US")));
        assertEquals("fi_FI", gui.getLanguageCodeForLocale(new Locale("fi", "FI")));
    }

    @Test
    public void testItemRowPriceQuantityAndTotal() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // ensure messages bundle is set so getString doesn't NPE
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control());
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundle);
        // ensure useDatabase is false
        java.lang.reflect.Field useDb = ShoppingCartGUI.class.getDeclaredField("useDatabase");
        useDb.setAccessible(true);
        useDb.setBoolean(gui, false);

        ShoppingCartGUI.ItemRow row = gui.new ItemRow(1, gui);
        // Run creation and assertions on FX thread synchronously
        runOnFxAndWait(() -> {
            try {
                row.createRow();
                // set price and quantity
                row.priceField.setText("2.50");
                row.quantitySpinner.getValueFactory().setValue(3);
                // Force update
                row.updateItemTotal();
                assertEquals(new BigDecimal("7.50"), row.getPrice().multiply(java.math.BigDecimal.valueOf(row.getQuantity())));
                assertEquals(new BigDecimal("2.50"), row.getPrice());
                assertEquals(3, row.getQuantity());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }
}
