/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.commons.fx.BaseDialog;
import org.panteleyev.money.icons.IconManager;
import org.panteleyev.money.model.Account;
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
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.persistence.DataCache.cache;

class AccountDialog extends BaseDialog<Account> {
    private final TextField nameEdit = new TextField();
    private final TextField initialEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final TextField accountNumberEdit = new TextField();
    private final ComboBox<CategoryType> typeComboBox = new ComboBox<>();
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>();
    private final CheckBox activeCheckBox = new CheckBox(RB.getString("account.Dialog.Active"));
    private final TextField interestEdit = new TextField();
    private final DatePicker closingDatePicker = new DatePicker();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();

    private final Collection<Category> categories;

    AccountDialog(Category initialCategory) {
        this(null, initialCategory, cache());
    }

    AccountDialog(Category initialCategory, DataCache cache) {
        this(null, initialCategory, cache);
    }

    AccountDialog(Account account, Category initialCategory) {
        this(account, initialCategory, cache());
    }

    AccountDialog(Account account, Category initialCategory, DataCache cache) {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("account.Dialog.Title"));

        var gridPane = new GridPane();
        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, new Label(RB.getString("label.Name")), nameEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.Type")), typeComboBox, iconComboBox);
        gridPane.addRow(index++, new Label(RB.getString("label.Category")), categoryComboBox);
        gridPane.addRow(index++, new Label(RB.getString("account.Dialog.InitialBalance")), initialEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.Account.Number")), accountNumberEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.Comment")), commentEdit);
        gridPane.addRow(index++, new Label(RB.getString("account.Dialog.Currency")), currencyComboBox);
        gridPane.addRow(index++, new Label(RB.getString("label.interest")), interestEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.closing.date")), closingDatePicker);
        gridPane.add(activeCheckBox, 1, index);

        GridPane.setColumnSpan(nameEdit, 2);
        GridPane.setColumnSpan(categoryComboBox, 2);
        GridPane.setColumnSpan(initialEdit, 2);
        GridPane.setColumnSpan(accountNumberEdit, 2);
        GridPane.setColumnSpan(commentEdit, 2);
        GridPane.setColumnSpan(currencyComboBox, 2);
        GridPane.setColumnSpan(interestEdit, 2);
        GridPane.setColumnSpan(closingDatePicker, 2);
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
                return currency == null ? "" : currency.getSymbol();
            }
        });

        var currencyList = cache.getCurrencies();
        currencyComboBox.setItems(FXCollections.observableArrayList(currencyList));
        typeComboBox.setItems(FXCollections.observableArrayList(CategoryType.values()));
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        IconManager.setupComboBox(iconComboBox);

        typeComboBox.setOnAction(event -> onCategoryTypeSelected());

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            activeCheckBox.setSelected(true);
            interestEdit.setText("0.0");
            closingDatePicker.setValue(null);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);

            if (initialCategory != null) {
                typeComboBox.getSelectionModel().select(initialCategory.getType());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                    .select(cache.getCategory(initialCategory.getUuid()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }

            currencyComboBox.getSelectionModel().select(cache.getDefaultCurrency().orElse(null));
        } else {
            nameEdit.setText(account.getName());
            commentEdit.setText(account.getComment());
            accountNumberEdit.setText(account.getAccountNumber());
            initialEdit.setText(account.getOpeningBalance().toString());
            activeCheckBox.setSelected(account.getEnabled());
            interestEdit.setText(account.getInterest().toString());
            closingDatePicker.setValue(account.getClosingDate().orElse(null));
            iconComboBox.getSelectionModel().select(cache.getIcon(account.getIconUuid()).orElse(EMPTY_ICON));

            typeComboBox.getSelectionModel().select(account.getType());
            categoryComboBox.getSelectionModel()
                .select(cache.getCategory(account.getCategoryUuid()).orElse(null));
            currencyComboBox.getSelectionModel()
                .select(cache.getCurrency(account.getCurrencyUuid().orElse(null)).orElse(null));
        }

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
                    .typeId(typeComboBox.getSelectionModel().getSelectedItem().getId())
                    .categoryUuid(categoryComboBox.getSelectionModel().getSelectedItem().getUuid())
                    .currencyUuid(selectedCurrency != null ? selectedCurrency.getUuid() : null)
                    .enabled(activeCheckBox.isSelected())
                    .interest(new BigDecimal(interestEdit.getText()))
                    .closingDate(closingDatePicker.getValue())
                    .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().getUuid())
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
            .filter(c -> c.getType().equals(type))
            .collect(Collectors.toList());

        categoryComboBox.setItems(FXCollections.observableArrayList(filtered));

        if (!filtered.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit,
            (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(categoryComboBox,
            (Control control, Category value) -> ValidationResult.fromErrorIf(control, null, value == null));
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
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
}
