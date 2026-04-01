package org.example.cart;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.*;

/**
 * JavaFX GUI Application for Shopping Cart with Multi-language Support
 *
 * Features:
 * - Language selection via ComboBox
 * - Dynamic UI localization
 * - Item input with price and quantity
 * - Calculation of per-item and total costs
 * - Real-time UI updates when language changes
 */
public class ShoppingCartGUI extends Application {

    private ResourceBundle messages;
    private Locale currentLocale = new Locale("en", "US");
    private List<ItemRow> itemRows = new ArrayList<>();
    private List<ShoppingCartCalculator.Item> items = new ArrayList<>();

    // UI Components
    private ComboBox<String> languageCombo;
    private Label itemCountLabel;
    private Spinner<Integer> itemCountSpinner;
    private Button addItemButton;
    private Button calculateButton;
    private Button clearButton;
    private VBox itemsContainer;
    private Label totalLabel;
    private Label totalValueLabel;
    private ScrollPane scrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadMessages();

        primaryStage.setTitle("Shopping Cart");
        primaryStage.setScene(createMainScene());
        primaryStage.setWidth(600);
        primaryStage.setHeight(700);
        primaryStage.show();
    }

    private void loadMessages() {
        try {
            messages = ResourceBundle.getBundle("MessagesBundle", currentLocale, new Utf8Control());
        } catch (Exception e) {
            System.err.println("Error loading messages: " + e.getMessage());
            messages = ResourceBundle.getBundle("MessagesBundle", new Locale("en", "US"), new Utf8Control());
        }
    }

    private Scene createMainScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-font-size: 11;");

        // Language selection section
        HBox languageBox = createLanguageSelectionBox();
        root.getChildren().add(languageBox);

        // Separator
        Separator separator1 = new Separator();
        root.getChildren().add(separator1);

        // Item count section
        HBox itemCountBox = createItemCountBox();
        root.getChildren().add(itemCountBox);

        // Separator
        Separator separator2 = new Separator();
        root.getChildren().add(separator2);

        // Items section with scroll pane
        VBox itemsSection = createItemsSection();
        root.getChildren().add(itemsSection);

        // Buttons section
        HBox buttonsBox = createButtonsBox();
        root.getChildren().add(buttonsBox);

        // Separator
        Separator separator3 = new Separator();
        root.getChildren().add(separator3);

        // Total section
        HBox totalBox = createTotalBox();
        root.getChildren().add(totalBox);

        return new Scene(root);
    }

    private HBox createLanguageSelectionBox() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label();
        label.setText(messages.getString("label.select_language"));

        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("English", "Suomi", "Svenska", "日本語", "العربية");
        languageCombo.setValue("English");
        languageCombo.setPrefWidth(150);

        languageCombo.setOnAction(event -> {
            String selected = languageCombo.getValue();
            Locale newLocale = getLocaleFromSelection(selected);
            if (!newLocale.equals(currentLocale)) {
                currentLocale = newLocale;
                loadMessages();
                updateAllUIText();
            }
        });

        hbox.getChildren().addAll(label, languageCombo);
        return hbox;
    }

    private Locale getLocaleFromSelection(String selection) {
        switch (selection) {
            case "Suomi": return new Locale("fi", "FI");
            case "Svenska": return new Locale("sv", "SE");
            case "日本語": return new Locale("ja", "JP");
            case "العربية": return new Locale("ar", "AR");
            default: return new Locale("en", "US");
        }
    }

    private HBox createItemCountBox() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        itemCountLabel = new Label();
        itemCountLabel.setText(messages.getString("prompt.items"));

        itemCountSpinner = new Spinner<>(1, 100, 1);
        itemCountSpinner.setPrefWidth(80);
        itemCountSpinner.setEditable(true);

        addItemButton = new Button();
        addItemButton.setText(messages.getString("button.add"));
        addItemButton.setStyle("-fx-font-size: 11; -fx-padding: 5 15;");
        addItemButton.setOnAction(event -> updateItemRows());

        hbox.getChildren().addAll(itemCountLabel, itemCountSpinner, addItemButton);
        return hbox;
    }

    private VBox createItemsSection() {
        VBox section = new VBox();

        itemsContainer = new VBox(10);
        itemsContainer.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");

        scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-control-inner-background: #ffffff;");

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        section.getChildren().add(scrollPane);

        return section;
    }

    private HBox createButtonsBox() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);

        calculateButton = new Button();
        calculateButton.setText(messages.getString("button.calculate"));
        calculateButton.setStyle("-fx-font-size: 11; -fx-padding: 8 20;");
        calculateButton.setPrefWidth(120);
        calculateButton.setOnAction(event -> calculateTotal());

        clearButton = new Button();
        clearButton.setText(messages.getString("button.clear"));
        clearButton.setStyle("-fx-font-size: 11; -fx-padding: 8 20;");
        clearButton.setPrefWidth(120);
        clearButton.setOnAction(event -> clearAll());

        hbox.getChildren().addAll(calculateButton, clearButton);
        return hbox;
    }

    private HBox createTotalBox() {
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setStyle("-fx-border-color: #0066cc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f0f0f0;");

        totalLabel = new Label();
        totalLabel.setText(messages.getString("label.total"));
        totalLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        totalValueLabel = new Label("0.00");
        totalValueLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0066cc;");

        hbox.getChildren().addAll(totalLabel, totalValueLabel);
        return hbox;
    }

    private void updateItemRows() {
        int count = itemCountSpinner.getValue();
        itemRows.clear();
        itemsContainer.getChildren().clear();

        for (int i = 1; i <= count; i++) {
            ItemRow row = new ItemRow(i, messages);
            itemRows.add(row);
            itemsContainer.getChildren().add(row.createRow());
        }
    }

    private void calculateTotal() {
        items.clear();

        for (ItemRow row : itemRows) {
            try {
                BigDecimal price = row.getPrice();
                int quantity = row.getQuantity();

                if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                    showError(String.format(messages.getString("error.invalid_price")));
                    return;
                }

                if (quantity < 0) {
                    showError(String.format(messages.getString("error.invalid_quantity")));
                    return;
                }

                if (quantity > 0) {
                    items.add(new ShoppingCartCalculator.Item(price, quantity));
                }
            } catch (NumberFormatException e) {
                showError(messages.getString("error.invalid_number"));
                return;
            }
        }

        if (items.isEmpty()) {
            totalValueLabel.setText("0.00");
            return;
        }

        BigDecimal total = ShoppingCartCalculator.calculateTotal(items);
        totalValueLabel.setText(total.toString());
    }

    private void clearAll() {
        itemCountSpinner.getValueFactory().setValue(1);
        itemRows.clear();
        itemsContainer.getChildren().clear();
        items.clear();
        totalValueLabel.setText("0.00");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.getString("error.invalid_number"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateAllUIText() {
        // Update language label
        if (languageCombo != null) {
            String currentLang = languageCombo.getValue();
            languageCombo.setOnAction(null); // Disable listener temporarily
            languageCombo.setValue(currentLang);
            languageCombo.setOnAction(event -> {
                String selected = languageCombo.getValue();
                Locale newLocale = getLocaleFromSelection(selected);
                if (!newLocale.equals(currentLocale)) {
                    currentLocale = newLocale;
                    loadMessages();
                    updateAllUIText();
                }
            });
        }

        // Update all labels
        if (itemCountLabel != null) {
            itemCountLabel.setText(messages.getString("prompt.items"));
        }
        if (addItemButton != null) {
            addItemButton.setText(messages.getString("button.add"));
        }
        if (calculateButton != null) {
            calculateButton.setText(messages.getString("button.calculate"));
        }
        if (clearButton != null) {
            clearButton.setText(messages.getString("button.clear"));
        }
        if (totalLabel != null) {
            totalLabel.setText(messages.getString("label.total"));
        }

        // Update item rows
        for (ItemRow row : itemRows) {
            row.updateLabels(messages);
        }
    }

    /**
     * Inner class representing a single item input row
     */
    private static class ItemRow {
        private int itemNumber;
        private ResourceBundle messages;
        private TextField priceField;
        private Spinner<Integer> quantitySpinner;
        private Label itemTotalLabel;
        private Label priceLabel;
        private Label quantityLabel;
        private Label itemNumberLabel;
        private Label itemTotalValueLabel;

        public ItemRow(int itemNumber, ResourceBundle messages) {
            this.itemNumber = itemNumber;
            this.messages = messages;
        }

        public VBox createRow() {
            VBox rowBox = new VBox(5);
            rowBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-padding: 10; -fx-background-color: #f9f9f9;");

            // Item number
            itemNumberLabel = new Label("Item " + itemNumber);
            itemNumberLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

            // Price field
            HBox priceBox = new HBox(10);
            priceBox.setAlignment(Pos.CENTER_LEFT);
            priceLabel = new Label(messages.getString("label.price"));
            priceField = new TextField();
            priceField.setPromptText("0.00");
            priceField.setPrefWidth(100);
            priceBox.getChildren().addAll(priceLabel, priceField);

            // Quantity spinner
            HBox quantityBox = new HBox(10);
            quantityBox.setAlignment(Pos.CENTER_LEFT);
            quantityLabel = new Label(messages.getString("label.quantity"));
            quantitySpinner = new Spinner<>(0, 1000, 0);
            quantitySpinner.setPrefWidth(100);
            quantitySpinner.setEditable(true);
            quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);

            // Item total
            HBox totalBox = new HBox(10);
            totalBox.setAlignment(Pos.CENTER_LEFT);
            itemTotalLabel = new Label(messages.getString("label.item_total"));
            itemTotalLabel.setStyle("-fx-font-weight: bold;");
            itemTotalValueLabel = new Label("0.00");
            itemTotalValueLabel.setStyle("-fx-text-fill: #0066cc; -fx-font-weight: bold;");
            totalBox.getChildren().addAll(itemTotalLabel, itemTotalValueLabel);

            rowBox.getChildren().addAll(itemNumberLabel, priceBox, quantityBox, totalBox);

            // Update item total when price or quantity changes
            priceField.textProperty().addListener((obs, oldVal, newVal) -> updateItemTotal());
            quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateItemTotal());

            return rowBox;
        }

        public void updateLabels(ResourceBundle newMessages) {
            this.messages = newMessages;
            if (priceLabel != null) {
                priceLabel.setText(messages.getString("label.price"));
            }
            if (quantityLabel != null) {
                quantityLabel.setText(messages.getString("label.quantity"));
            }
            if (itemTotalLabel != null) {
                itemTotalLabel.setText(messages.getString("label.item_total"));
            }
        }

        private void updateItemTotal() {
            try {
                String priceStr = priceField.getText().trim();
                if (priceStr.isEmpty()) {
                    itemTotalValueLabel.setText("0.00");
                    return;
                }

                BigDecimal price = new BigDecimal(priceStr);
                int quantity = quantitySpinner.getValue();

                if (price.compareTo(BigDecimal.ZERO) >= 0 && quantity >= 0) {
                    BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
                    itemTotalValueLabel.setText(total.toString());
                } else {
                    itemTotalValueLabel.setText("0.00");
                }
            } catch (NumberFormatException e) {
                itemTotalValueLabel.setText("0.00");
            }
        }

        public BigDecimal getPrice() throws NumberFormatException {
            String priceStr = priceField.getText().trim();
            if (priceStr.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(priceStr);
        }

        public int getQuantity() {
            return quantitySpinner.getValue();
        }
    }
}

