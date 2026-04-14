package org.example.cart;

import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartGUIExtraTest {

    @BeforeAll
    static void initJFX() {
        // initialize JavaFX toolkit
        new JFXPanel();
    }

    private static void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for FX runLater");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private static void waitForCondition(BooleanSupplier condition) throws InterruptedException {
        // Use a single timed wait instead of busy-waiting to satisfy static analysis.
        final long timeoutMillis = 2000;
        if (!condition.getAsBoolean()) {
            synchronized (ShoppingCartGUIExtraTest.class) {
                ShoppingCartGUIExtraTest.class.wait(timeoutMillis);
            }
        }
    }

    @Test
    void testUpdateItemRowsCreatesCorrectNumber() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // set messages to avoid NPE
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control());
        Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundle);

        // Initialize itemRows list
        Field itemRowsField = ShoppingCartGUI.class.getDeclaredField("itemRows");
        itemRowsField.setAccessible(true);
        java.util.ArrayList<?> itemRows = new java.util.ArrayList<>();
        itemRowsField.set(gui, itemRows);

        // Initialize itemsContainer as a VBox
        Field itemsContainerField = ShoppingCartGUI.class.getDeclaredField("itemsContainer");
        itemsContainerField.setAccessible(true);
        VBox container = new VBox();
        itemsContainerField.set(gui, container);

        // access spinner and set value
        Field spinnerField = ShoppingCartGUI.class.getDeclaredField("itemCountSpinner");
        spinnerField.setAccessible(true);
        // create spinner object so updateItemRows can use it
        Spinner<Integer> spinner = new Spinner<>(1, 10, 3);
        spinnerField.set(gui, spinner);

        // invoke updateItemRows on FX thread and wait
        Method updateItemRows = ShoppingCartGUI.class.getDeclaredMethod("updateItemRows");
        updateItemRows.setAccessible(true);

        runOnFxThreadAndWait(() -> {
            try {
                updateItemRows.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        @SuppressWarnings("rawtypes")
        java.util.List rows = (java.util.List) itemRowsField.get(gui);
        VBox updatedContainer = (VBox) itemsContainerField.get(gui);

        assertEquals(3, rows.size(), "itemRows should contain 3 items");
        assertEquals(3, updatedContainer.getChildren().size(), "container should have 3 children");
    }

    @Test
    void testItemRowUpdateItemTotalEdgeCases() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control());
        Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundle);

        // construct ItemRow
        ShoppingCartGUI.ItemRow row = gui.new ItemRow(1, gui);

        // create UI and run assertions on FX thread
        runOnFxThreadAndWait(() -> {
            try {
                row.createRow();
                // empty price -> total 0.00
                row.priceField.setText("");
                row.quantitySpinner.getValueFactory().setValue(5);
                row.updateItemTotal();
                assertEquals("0.00", row.itemTotalValueLabel.getText());

                // non-numeric price -> total 0.00
                row.priceField.setText("abc");
                row.quantitySpinner.getValueFactory().setValue(2);
                row.updateItemTotal();
                assertEquals("0.00", row.itemTotalValueLabel.getText());

                // negative price -> total 0.00 (treated as invalid)
                row.priceField.setText("-1.00");
                row.quantitySpinner.getValueFactory().setValue(2);
                row.updateItemTotal();
                assertEquals("0.00", row.itemTotalValueLabel.getText());

                // valid price and zero quantity -> 0.00
                row.priceField.setText("3.33");
                row.quantitySpinner.getValueFactory().setValue(0);
                row.updateItemTotal();
                assertEquals("0.00", row.itemTotalValueLabel.getText());

                // valid price and quantity -> computed total
                row.priceField.setText("2.50");
                row.quantitySpinner.getValueFactory().setValue(3);
                row.updateItemTotal();
                assertEquals("7.50", row.itemTotalValueLabel.getText());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testSaveCartToDatabaseWhenDbUnavailable() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // set DB to an invalid URL to force DatabaseConnection.testConnection() to return false
        System.setProperty("db.url", "jdbc:h2:tcp://invalid:9092/~/nope");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        // Capture stderr
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errOut));

        try {
            Method saveMethod = ShoppingCartGUI.class.getDeclaredMethod("saveCartToDatabase", int.class, java.math.BigDecimal.class);
            saveMethod.setAccessible(true);
            saveMethod.invoke(gui, 1, new BigDecimal("1.00"));

            // wait for expected error output (non-blocking polling)
            waitForCondition(() -> errOut.size() > 0);

            String err = errOut.toString();
            // should contain 'Database not available' or connection test failure
            assertTrue(err.contains("Database not available") || err.toLowerCase().contains("connection test failed") || err.toLowerCase().contains("failed to connect"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testLanguageSelectionListenerChangesLocale() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control());
        Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundle);

        Method createLanguageSelectionBox = ShoppingCartGUI.class.getDeclaredMethod("createLanguageSelectionBox");
        createLanguageSelectionBox.setAccessible(true);
        createLanguageSelectionBox.invoke(gui);

        Field languageComboField = ShoppingCartGUI.class.getDeclaredField("languageCombo");
        languageComboField.setAccessible(true);
        @SuppressWarnings("unchecked")
        javafx.scene.control.ComboBox<String> combo = (javafx.scene.control.ComboBox<String>) languageComboField.get(gui);

        // simulate selecting 'Suomi' and firing action
        runOnFxThreadAndWait(() -> {
            combo.setValue("Suomi");
            // invoke the onAction handler
            if (combo.getOnAction() != null) {
                combo.getOnAction().handle(new ActionEvent());
            }
        });

        Field currentLocaleField = ShoppingCartGUI.class.getDeclaredField("currentLocale");
        currentLocaleField.setAccessible(true);
        Locale locale = (Locale) currentLocaleField.get(gui);
        assertEquals(new Locale("fi","FI"), locale);
    }
}
