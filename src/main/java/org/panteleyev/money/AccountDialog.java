/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.utilities.fx.BaseDialog;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class AccountDialog extends BaseDialog<Account> {
    private final TextField nameEdit = new TextField();
    private final TextField initialEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final ComboBox<CategoryType> typeComboBox = new ComboBox<>();
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>();
    private final CheckBox activeCheckBox = new CheckBox(RB.getString("account.Dialog.Active"));

    private final Collection<Category> categories;

    AccountDialog(Category initialCategory) {
        this(null, initialCategory);
    }

    AccountDialog(Account account, Category initialCategory) {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("account.Dialog.Title"));


        var gridPane = new GridPane();
        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, new Label(RB.getString("label.Name")), nameEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.Type")), typeComboBox);
        gridPane.addRow(index++, new Label(RB.getString("label.Category")), categoryComboBox);
        gridPane.addRow(index++, new Label(RB.getString("account.Dialog.InitialBalance")), initialEdit);
        gridPane.addRow(index++, new Label(RB.getString("label.Comment")), commentEdit);
        gridPane.addRow(index++, new Label(RB.getString("account.Dialog.Currency")), currencyComboBox);
        gridPane.add(activeCheckBox, 1, index);

        getDialogPane().setContent(gridPane);

        nameEdit.setPrefColumnCount(20);

        categories = getDao().getCategories();

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
                return currency.getSymbol();
            }
        });

        var currencyList = getDao().getCurrencies();
        currencyComboBox.setItems(FXCollections.observableArrayList(currencyList));
        typeComboBox.setItems(FXCollections.observableArrayList(CategoryType.values()));
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        typeComboBox.setOnAction(event -> onCategoryTypeSelected());

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            activeCheckBox.setSelected(true);

            if (initialCategory != null) {
                typeComboBox.getSelectionModel().select(initialCategory.getType());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                        .select(getDao().getCategory(initialCategory.getId()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }

            currencyComboBox.getSelectionModel().select(getDao().getDefaultCurrency().orElse(null));
        } else {
            nameEdit.setText(account.getName());
            commentEdit.setText(account.getComment());
            initialEdit.setText(account.getOpeningBalance().toString());
            activeCheckBox.setSelected(account.getEnabled());

            typeComboBox.getSelectionModel().select(account.getType());
            categoryComboBox.getSelectionModel()
                    .select(getDao().getCategory(account.getCategoryId()).orElse(null));
            currencyComboBox.getSelectionModel()
                    .select(getDao().getCurrency(account.getCurrencyId()).orElse(null));
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                // TODO: reconsider using null currency value
                var selectedCurrency = currencyComboBox.getSelectionModel().getSelectedItem();

                return new Account(account != null ? account.getId() : 0,
                        nameEdit.getText(),
                        commentEdit.getText(),
                        new BigDecimal(initialEdit.getText()),
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        typeComboBox.getSelectionModel().getSelectedItem().getId(),
                        categoryComboBox.getSelectionModel().getSelectedItem().getId(),
                        selectedCurrency != null ? selectedCurrency.getId() : 0,
                        activeCheckBox.isSelected(),
                        account != null ? account.getGuid() : UUID.randomUUID().toString(),
                        System.currentTimeMillis()
                );
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
        validation.initInitialDecoration();
    }
}
