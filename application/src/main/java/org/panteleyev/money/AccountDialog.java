package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.cells.CardTypeComboBoxCell;
import org.panteleyev.money.icons.IconManager;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.Constants.COLON;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.persistence.DataCache.cache;

class AccountDialog extends BaseDialog<Account> {
    private final TextField nameEdit = new TextField();
    private final TextField initialEdit = new TextField();
    private final TextField creditEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final TextField accountNumberEdit = new TextField();
    private final ComboBox<CategoryType> typeComboBox = new ComboBox<>();
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>();
    private final CheckBox activeCheckBox = newCheckBox(RB, "account.Dialog.Active");
    private final TextField interestEdit = new TextField();
    private final DatePicker closingDatePicker = new DatePicker();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();
    private final ComboBox<CardType> cardTypeComboBox = new ComboBox<>();
    private final TextField cardNumberEdit = new TextField();

    private final Collection<Category> categories;

    AccountDialog(Controller owner, Category initialCategory) {
        this(owner, null, initialCategory, cache());
    }

    AccountDialog(Controller owner, Category initialCategory, DataCache cache) {
        this(owner, null, initialCategory, cache);
    }

    AccountDialog(Controller owner, Account account, Category initialCategory) {
        this(owner, account, initialCategory, cache());
    }

    AccountDialog(Controller owner, Account account, Category initialCategory, DataCache cache) {
        super(owner, MainWindowController.CSS_PATH);

        setTitle(RB.getString("account.Dialog.Title"));

        var gridPane = new GridPane();
        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, newLabel(RB, "label.Name"), nameEdit);
        gridPane.addRow(index++, newLabel(RB, "label.Type"), typeComboBox, iconComboBox);
        gridPane.addRow(index++, newLabel(RB, "label.Category"), categoryComboBox);
        gridPane.addRow(index++, newLabel(RB, "account.Dialog.InitialBalance"), initialEdit);
        gridPane.addRow(index++, newLabel(RB, "label.credit"), creditEdit);
        gridPane.addRow(index++, newLabel(RB, "label.Account.Number"), accountNumberEdit);
        gridPane.addRow(index++, newLabel(RB, "Comment", COLON), commentEdit);
        gridPane.addRow(index++, newLabel(RB, "Currency", COLON), currencyComboBox);
        gridPane.addRow(index++, newLabel(RB, "label.interest"), interestEdit);
        gridPane.addRow(index++, newLabel(RB, "label.closing.date"), closingDatePicker);
        gridPane.addRow(index++, newLabel(RB, "label.card.type"), cardTypeComboBox);
        gridPane.addRow(index++, newLabel(RB, "label.card.number"), cardNumberEdit);
        gridPane.add(activeCheckBox, 1, index);

        GridPane.setColumnSpan(nameEdit, 2);
        GridPane.setColumnSpan(categoryComboBox, 2);
        GridPane.setColumnSpan(initialEdit, 2);
        GridPane.setColumnSpan(creditEdit, 2);
        GridPane.setColumnSpan(accountNumberEdit, 2);
        GridPane.setColumnSpan(commentEdit, 2);
        GridPane.setColumnSpan(currencyComboBox, 2);
        GridPane.setColumnSpan(interestEdit, 2);
        GridPane.setColumnSpan(closingDatePicker, 2);
        GridPane.setColumnSpan(cardTypeComboBox, 2);
        GridPane.setColumnSpan(cardNumberEdit, 2);
        GridPane.setColumnSpan(activeCheckBox, 2);

        getDialogPane().setContent(gridPane);

        nameEdit.setPrefColumnCount(20);

        categories = cache.getCategories();

        typeComboBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(CategoryType object) {
                return object.getTypeName();
            }
        });
        categoryComboBox.setConverter(new ReadOnlyNamedConverter<>());
        currencyComboBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Currency currency) {
                return currency == null ? "" : currency.symbol();
            }
        });

        var currencyList = cache.getCurrencies();
        currencyComboBox.setItems(FXCollections.observableArrayList(currencyList));
        typeComboBox.setItems(FXCollections.observableArrayList(CategoryType.values()));
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        IconManager.setupComboBox(iconComboBox);
        cardTypeComboBox.setItems(FXCollections.observableArrayList(CardType.values()));

        typeComboBox.setOnAction(event -> onCategoryTypeSelected());

        cardTypeComboBox.setButtonCell(new CardTypeComboBoxCell());
        cardTypeComboBox.setCellFactory(p -> new CardTypeComboBoxCell());
        cardTypeComboBox.setOnAction(event -> onCardTypeSelected());

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            creditEdit.setText("0.0");
            activeCheckBox.setSelected(true);
            interestEdit.setText("0.0");
            closingDatePicker.setValue(null);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
            cardTypeComboBox.getSelectionModel().select(CardType.NONE);
            cardNumberEdit.setText("");

            if (initialCategory != null) {
                typeComboBox.getSelectionModel().select(initialCategory.type());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                    .select(cache.getCategory(initialCategory.uuid()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }

            currencyComboBox.getSelectionModel().select(cache.getDefaultCurrency().orElse(null));
        } else {
            nameEdit.setText(account.name());
            commentEdit.setText(account.comment());
            accountNumberEdit.setText(account.accountNumber());
            initialEdit.setText(account.openingBalance().toString());
            creditEdit.setText(account.accountLimit().toString());
            activeCheckBox.setSelected(account.enabled());
            interestEdit.setText(account.interest().toString());
            closingDatePicker.setValue(account.closingDate());
            iconComboBox.getSelectionModel().select(cache.getIcon(account.iconUuid()).orElse(EMPTY_ICON));
            cardTypeComboBox.getSelectionModel().select(account.cardType());
            cardNumberEdit.setText(account.cardNumber());

            typeComboBox.getSelectionModel().select(account.type());
            categoryComboBox.getSelectionModel()
                .select(cache.getCategory(account.categoryUuid()).orElse(null));
            currencyComboBox.getSelectionModel()
                .select(cache.getCurrency(account.currencyUuid()).orElse(null));
        }
        onCardTypeSelected();

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                // TODO: reconsider using null currency value
                var selectedCurrency = currencyComboBox.getSelectionModel().getSelectedItem();

                long now = System.currentTimeMillis();

                var builder = new Account.Builder(account)
                    .name(nameEdit.getText())
                    .comment(commentEdit.getText())
                    .accountNumber(accountNumberEdit.getText())
                    .openingBalance(new BigDecimal(initialEdit.getText()))
                    .accountLimit(new BigDecimal(creditEdit.getText()))
                    .type(typeComboBox.getSelectionModel().getSelectedItem())
                    .categoryUuid(categoryComboBox.getSelectionModel().getSelectedItem().uuid())
                    .currencyUuid(selectedCurrency != null ? selectedCurrency.uuid() : null)
                    .enabled(activeCheckBox.isSelected())
                    .interest(new BigDecimal(interestEdit.getText()))
                    .closingDate(closingDatePicker.getValue())
                    .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().uuid())
                    .cardType(cardTypeComboBox.getSelectionModel().getSelectedItem())
                    .cardNumber(cardNumberEdit.getText())
                    .modified(now);

                if (account == null) {
                    builder.guid(UUID.randomUUID())
                        .created(now);
                }

                return builder.build();
            } else {
                return null;
            }
        });

        createDefaultButtons(RB);

        Platform.runLater(this::createValidationSupport);
    }

    private void onCategoryTypeSelected() {
        var type = typeComboBox.getSelectionModel().getSelectedItem();

        List<Category> filtered = categories.stream()
            .filter(c -> c.type().equals(type))
            .collect(Collectors.toList());

        categoryComboBox.setItems(FXCollections.observableArrayList(filtered));

        if (!filtered.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    private void onCardTypeSelected() {
        cardNumberEdit.setDisable(
            cardTypeComboBox.getSelectionModel().getSelectedItem() == CardType.NONE
        );
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit,
            (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(categoryComboBox,
            (Control control, Category value) -> ValidationResult.fromErrorIf(control, null, value == null));
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(creditEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(interestEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    TextField getCommentEdit() {
        return commentEdit;
    }

    TextField getAccountNumberEdit() {
        return accountNumberEdit;
    }

    TextField getInterestEdit() {
        return interestEdit;
    }

    ComboBox<Currency> getCurrencyComboBox() {
        return currencyComboBox;
    }

    ComboBox<CardType> getCardTypeComboBox() {
        return cardTypeComboBox;
    }

    TextField getCardNumberEdit() {
        return cardNumberEdit;
    }

    TextField getCreditEdit() {
        return creditEdit;
    }
}
