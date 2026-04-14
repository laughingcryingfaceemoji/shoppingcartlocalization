package org.example.cart;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartGUICoverageTest {

    @BeforeAll
    public static void init() {
        // initialize JavaFX toolkit
        new JFXPanel();
    }

    private void runOnFxAndWait(Runnable action) throws InterruptedException {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try { action.run(); } finally { latch.countDown(); }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for FX action");
        }
    }

    private void waitForCondition(BooleanSupplier condition, long timeoutMillis) throws InterruptedException {
        if (condition.getAsBoolean()) return;
        long end = System.currentTimeMillis() + timeoutMillis;
        synchronized (this) {
            while (!condition.getAsBoolean()) {
                long remaining = end - System.currentTimeMillis();
                if (remaining <= 0) break;
                this.wait(remaining);
            }
        }
    }

    @Test
    public void testLoadMessagesFromDatabase() throws Exception {
        // Point DB to an H2 in-memory instance
        System.setProperty("db.url", "jdbc:h2:mem:gui_loc1;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            // create table suitable for H2 (quoted identifiers)
            s.execute("CREATE TABLE localization_strings (id IDENTITY PRIMARY KEY, \"key\" VARCHAR(100) NOT NULL, \"value\" VARCHAR(255) NOT NULL, language VARCHAR(10) NOT NULL);");
            s.execute("INSERT INTO localization_strings (\"key\", \"value\", language) VALUES ('label.total', 'DB Total', 'en_US')");
        }

        ShoppingCartGUI gui = new ShoppingCartGUI();
        // set currentLocale to en_US
        Field localeField = ShoppingCartGUI.class.getDeclaredField("currentLocale");
        localeField.setAccessible(true);
        localeField.set(gui, new Locale("en", "US"));

        // call private loadMessages()
        Method loadMessages = ShoppingCartGUI.class.getDeclaredMethod("loadMessages");
        loadMessages.setAccessible(true);
        loadMessages.invoke(gui);

        // verify useDatabase == true and getString returns DB value
        Field useDb = ShoppingCartGUI.class.getDeclaredField("useDatabase");
        useDb.setAccessible(true);
        boolean usedb = useDb.getBoolean(gui);
        assertTrue(usedb, "Expected GUI to load localization from DB");

        Method getString = ShoppingCartGUI.class.getDeclaredMethod("getString", String.class);
        getString.setAccessible(true);
        String totalLabel = (String) getString.invoke(gui, "label.total");
        assertEquals("DB Total", totalLabel);
    }

    @Test
    public void testCalculateTotalAndSaveToDatabase() throws Exception {
        // Set DB to H2 in-memory and create cart tables
        System.setProperty("db.url", "jdbc:h2:mem:cartdb_gui;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE cart_records (id INT AUTO_INCREMENT PRIMARY KEY, total_items INT NOT NULL, total_cost DOUBLE NOT NULL, language VARCHAR(10), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
            s.execute("CREATE TABLE cart_items (id INT AUTO_INCREMENT PRIMARY KEY, cart_record_id INT, item_number INT NOT NULL, price DOUBLE NOT NULL, quantity INT NOT NULL, subtotal DOUBLE NOT NULL);");
        }

        ShoppingCartGUI gui = new ShoppingCartGUI();
        // set messages bundle so createMainScene can use them when building UI
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", new Locale("en","US"), new Utf8Control());
        Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundle);

        // create UI components so totalValueLabel and itemsContainer are initialized - do it on FX thread and wait
        Method createMainScene = ShoppingCartGUI.class.getDeclaredMethod("createMainScene");
        createMainScene.setAccessible(true);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                createMainScene.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        // wait up to 2s for FX task
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timeout waiting for createMainScene to complete");
        }

        // Ensure totalValueLabel (and other UI fields) are initialized by polling the field
        Field totalValueLabelField = ShoppingCartGUI.class.getDeclaredField("totalValueLabel");
        totalValueLabelField.setAccessible(true);
        // Sometimes createMainScene doesn't initialize controls in headless test env; ensure label exists by creating it on FX thread if missing
        if (totalValueLabelField.get(gui) == null) {
            java.util.concurrent.CountDownLatch initLatch = new java.util.concurrent.CountDownLatch(1);
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.scene.control.Label lbl = new javafx.scene.control.Label("0.00");
                    totalValueLabelField.set(gui, lbl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    initLatch.countDown();
                }
            });
            if (!initLatch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Failed to set totalValueLabel on FX thread");
            }
        }

        // ensure useDatabase is false so getString falls back to bundle
        Field useDb = ShoppingCartGUI.class.getDeclaredField("useDatabase");
        useDb.setAccessible(true);
        useDb.setBoolean(gui, false);

        // create two ItemRows and add them to private itemRows list
        Method createItemRows = ShoppingCartGUI.class.getDeclaredMethod("updateItemRows");
        createItemRows.setAccessible(true);
        // First, set itemCountSpinner value to 0 then manually add rows
        // We'll directly construct ItemRow instances and add to the list

        // access itemRows field
        Field itemRowsField = ShoppingCartGUI.class.getDeclaredField("itemRows");
        itemRowsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<Object> itemRows = (java.util.List<Object>) itemRowsField.get(gui);

        // create rows on JavaFX thread
        java.util.concurrent.CountDownLatch latch2 = new java.util.concurrent.CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                ShoppingCartGUI.ItemRow r1 = (ShoppingCartGUI.ItemRow) gui.new ItemRow(1, gui);
                r1.createRow();
                r1.priceField.setText("2.00");
                r1.quantitySpinner.getValueFactory().setValue(2);

                ShoppingCartGUI.ItemRow r2 = (ShoppingCartGUI.ItemRow) gui.new ItemRow(2, gui);
                r2.createRow();
                r2.priceField.setText("3.50");
                r2.quantitySpinner.getValueFactory().setValue(1);

                itemRows.add(r1);
                itemRows.add(r2);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                latch2.countDown();
            }
        });
        if (!latch2.await(2, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timeout waiting for ItemRow creation");
        }

        // call calculateTotal()
        Method calculateTotal = ShoppingCartGUI.class.getDeclaredMethod("calculateTotal");
        calculateTotal.setAccessible(true);
        calculateTotal.invoke(gui);

        // wait up to 3s for the background thread to write DB record
        waitForCondition(() -> {
            try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) AS cnt FROM cart_records")) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            } catch (Exception e) {
                // ignore and retry until timeout
            }
            return false;
        }, 3000);

        // verify DB has one cart_records entry with total_cost 7.5
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT total_items, total_cost, language FROM cart_records")) {
            assertTrue(rs.next(), "Expected a cart_records row");
            int totalItems = rs.getInt("total_items");
            double totalCost = rs.getDouble("total_cost");
            assertEquals(2, totalItems); // items with qty>0 -> two items
            assertEquals(7.5, totalCost, 0.001);
        }
    }

    @Test
    public void testUpdateAllUITextUpdatesControls() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        // set messages bundle to Finnish to observe changed texts
        ResourceBundle bundleFi = ResourceBundle.getBundle("MessagesBundle", new Locale("fi","FI"), new Utf8Control());
        Field messagesField = ShoppingCartGUI.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        messagesField.set(gui, bundleFi);

        // Build the Scene/UI components by invoking createMainScene on FX thread
        Method createMainScene = ShoppingCartGUI.class.getDeclaredMethod("createMainScene");
        createMainScene.setAccessible(true);

        // Run createMainScene on FX thread and wait for completion
        runOnFxAndWait(() -> {
            try {
                createMainScene.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Now call updateAllUIText and verify some controls text changed
        Method updateAllUIText = ShoppingCartGUI.class.getDeclaredMethod("updateAllUIText");
        updateAllUIText.setAccessible(true);
        updateAllUIText.invoke(gui);

        // access addItemButton and totalLabel
        Field addItemButtonField = ShoppingCartGUI.class.getDeclaredField("addItemButton");
        addItemButtonField.setAccessible(true);
        Button addBtn = (Button) addItemButtonField.get(gui);

        Field totalLabelField = ShoppingCartGUI.class.getDeclaredField("totalLabel");
        totalLabelField.setAccessible(true);
        javafx.scene.control.Label totalLbl = (javafx.scene.control.Label) totalLabelField.get(gui);

        assertNotNull(addBtn);
        assertNotNull(totalLbl);
        // button text should match resource key 'button.add'
        String expectedAdd = bundleFi.getString("button.add");
        assertEquals(expectedAdd, addBtn.getText());

        String expectedTotal = bundleFi.getString("label.total");
        assertEquals(expectedTotal, totalLbl.getText());
    }
}
