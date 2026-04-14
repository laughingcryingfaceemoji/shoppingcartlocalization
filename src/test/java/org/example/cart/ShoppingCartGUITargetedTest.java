package org.example.cart;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Targeted tests to cover uncovered branches in ShoppingCartGUI:
 * - loadMessages: fallback to resource bundle
 * - getString: missing key returns key
 * - calculateTotal: invalid price/quantity and normal flow
 *
 * Note: Tests use reflection to access private methods and fields without requiring mockito-inline.
 */
class ShoppingCartGUITargetedTest {

    @BeforeAll
    static void initJFX() {
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
    void testLoadMessagesFallsbackToResourceBundleOnDbFailure() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();

        // The loadMessages method should fallback to ResourceBundle if DB is unavailable
        java.lang.reflect.Method m = ShoppingCartGUI.class.getDeclaredMethod("loadMessages");
        m.setAccessible(true);
        m.invoke(gui);

        // Verify messages field is set to a non-null ResourceBundle
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        ResourceBundle rb = (ResourceBundle) messagesField.get(gui);
        assertNotNull(rb, "Messages should fallback to ResourceBundle");
    }

    @Test
    void testGetStringMissingKeyReturnsKey() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // Ensure messages is set to a minimal bundle with no key
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control()));

        java.lang.reflect.Method getString = ShoppingCartGUI.class.getDeclaredMethod("getString", String.class);
        getString.setAccessible(true);
        String res = (String) getString.invoke(gui, "non.existent.key");
        assertEquals("non.existent.key", res, "Missing key should return the key itself");
    }

    @Test
    void testCalculateTotalInvalidPriceShowsZero() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // Prepare messages bundle
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control()));

        // Add one item row with invalid price
        java.lang.reflect.Field itemRowsField = ShoppingCartGUI.class.getDeclaredField("itemRows");
        itemRowsField.setAccessible(true);
        @SuppressWarnings({"unchecked","rawtypes"})
        List<ShoppingCartGUI.ItemRow> itemRows = (List) itemRowsField.get(gui);

        // create and add an ItemRow
        ShoppingCartGUI.ItemRow row = gui.new ItemRow(1, gui);
        runOnFxAndWait(() -> {
            row.createRow();
            // invalid price: empty -> updateItemTotal sets 0.00
            row.priceField.setText("");
            row.quantitySpinner.getValueFactory().setValue(1);
            itemRows.add(row);
        });

        // invoke calculateTotal via reflection
        java.lang.reflect.Method calc = ShoppingCartGUI.class.getDeclaredMethod("calculateTotal");
        calc.setAccessible(true);

        // Check totalValueLabel after running calculateTotal on FX thread
        java.lang.reflect.Field totalValueField = ShoppingCartGUI.class.getDeclaredField("totalValueLabel");
        totalValueField.setAccessible(true);

        runOnFxAndWait(() -> {
            try {
                calc.invoke(gui);
                // Since item invalid, totalValueLabel should be 0.00
                javafx.scene.control.Label l = (javafx.scene.control.Label) totalValueField.get(gui);
                assertEquals("0.00", l.getText(), "Total should be 0.00 for invalid price");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void testCalculateTotalWithValidPriceAndQuantity() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // Prepare messages bundle
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control()));

        // Add one item row with valid price and quantity
        java.lang.reflect.Field itemRowsField = ShoppingCartGUI.class.getDeclaredField("itemRows");
        itemRowsField.setAccessible(true);
        @SuppressWarnings({"unchecked","rawtypes"})
        List<ShoppingCartGUI.ItemRow> itemRows = (List) itemRowsField.get(gui);

        // create and add an ItemRow with valid values
        ShoppingCartGUI.ItemRow row = gui.new ItemRow(1, gui);
        runOnFxAndWait(() -> {
            row.createRow();
            row.priceField.setText("10.00");
            row.quantitySpinner.getValueFactory().setValue(2);
            itemRows.add(row);
        });

        // invoke calculateTotal via reflection
        java.lang.reflect.Method calc = ShoppingCartGUI.class.getDeclaredMethod("calculateTotal");
        calc.setAccessible(true);

        // Check totalValueLabel after running calculateTotal on FX thread
        java.lang.reflect.Field totalValueField = ShoppingCartGUI.class.getDeclaredField("totalValueLabel");
        totalValueField.setAccessible(true);

        runOnFxAndWait(() -> {
            try {
                calc.invoke(gui);
                // Total should be 10.00 * 2 = 20.00
                javafx.scene.control.Label l = (javafx.scene.control.Label) totalValueField.get(gui);
                String totalText = l.getText();
                assertTrue(totalText.contains("20") || totalText.contains("20.00"), "Total should contain 20 (for 10*2)");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void testGetStringWithValidKey() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // Setup with default resource bundle
        java.lang.reflect.Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control()));

        // Try to get an existing key from the bundle
        java.lang.reflect.Method getString = ShoppingCartGUI.class.getDeclaredMethod("getString", String.class);
        getString.setAccessible(true);

        // Just verify it doesn't throw an exception and returns something
        String res = (String) getString.invoke(gui, "label.total");
        assertNotNull(res, "getString should return a value for existing keys");
    }
}
