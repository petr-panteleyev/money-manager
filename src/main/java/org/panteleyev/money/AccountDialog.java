/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.utilities.fx.BaseDialog;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

final class AccountDialog extends BaseDialog<Account.Builder> implements Styles {
    private final ResourceBundle          rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final TextField               nameEdit = new TextField();
    private final TextField               initialEdit = new TextField();
    private final TextField               commentEdit = new TextField();
    private final ComboBox<CategoryType>  typeComboBox = new ComboBox<>();
    private final ComboBox<Category>      categoryComboBox = new ComboBox<>();
    private final ComboBox<Currency>      currencyComboBox = new ComboBox<>();
    private final CheckBox                activeCheckBox = new CheckBox(rb.getString("account.Dialog.Active"));

    private Collection<Category>    categories;

    private final Account           account;
    private final Category          initialCategory;

    AccountDialog(Account account) {
        super(MainWindowController.DIALOGS_CSS);

        this.account = account;
        this.initialCategory = null;

        initialize();
    }

    AccountDialog(Category initialCategory) {
        super(MainWindowController.DIALOGS_CSS);

        this.account = null;
        this.initialCategory = initialCategory;

        initialize();
    }

    private void initialize() {
        setTitle(rb.getString("account.Dialog.Title"));

        GridPane pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);

        int index = 0;
        pane.addRow(index++, new Label(rb.getString("label.Name")), nameEdit);
        pane.addRow(index++, new Label(rb.getString("label.Type")), typeComboBox);
        pane.addRow(index++, new Label(rb.getString("label.Category")), categoryComboBox);
        pane.addRow(index++, new Label(rb.getString("account.Dialog.InitialBalance")), initialEdit);
        pane.addRow(index++, new Label(rb.getString("label.Comment")), commentEdit);
        pane.addRow(index++, new Label(rb.getString("account.Dialog.Currency")), currencyComboBox);
        pane.add(activeCheckBox, 1, index);

        nameEdit.setPrefColumnCount(20);

        getDialogPane().setContent(pane);

        MoneyDAO dao = MoneyDAO.getInstance();

        categories = dao.getCategories();

        typeComboBox.setConverter(new ReadOnlyNamedConverter<>());
        categoryComboBox.setConverter(new ReadOnlyNamedConverter<>());

        currencyComboBox.setConverter(new ReadOnlyStringConverter<Currency>() {
            @Override
            public String toString(Currency object) {
                return object.getSymbol();
            }
        });

        Collection<Currency> currencyList = dao.getCurrencies();
        currencyComboBox.setItems(FXCollections.observableArrayList(currencyList));

        typeComboBox.setItems(FXCollections.observableArrayList(CategoryType.values()));

        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        typeComboBox.setOnAction(x -> onCategoryTypeSelected());

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            activeCheckBox.setSelected(true);

            if (initialCategory != null) {
                typeComboBox.getSelectionModel()
                        .select(initialCategory.getCatType());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                        .select(dao.getCategory(initialCategory.getId()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }

            dao.getDefaultCurrency().ifPresent(c -> currencyComboBox.getSelectionModel().select(c));
        } else {
            nameEdit.setText(account.getName());
            commentEdit.setText(account.getComment());
            initialEdit.setText(account.getOpeningBalance().toString());
            activeCheckBox.setSelected(account.isEnabled());

            typeComboBox.getSelectionModel()
                .select(account.getType());
            categoryComboBox.getSelectionModel()
                .select(dao.getCategory(account.getCategoryId()).orElse(null));

            currencyComboBox.getSelectionModel()
                    .select(dao.getCurrency(account.getCurrencyId())
                            .orElse(null));
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                // TODO: reconsider using null currency value
                Currency selectedCurrency = currencyComboBox.getSelectionModel().getSelectedItem();

                return new Account.Builder(this.account)
                    .name(nameEdit.getText())
                    .comment(commentEdit.getText())
                    .enabled(activeCheckBox.isSelected())
                    .openingBalance(new BigDecimal(initialEdit.getText()))
                    .type(typeComboBox.getSelectionModel().getSelectedItem())
                    .categoryId(categoryComboBox.getSelectionModel().getSelectedItem().getId())
                    .currencyId(selectedCurrency == null? null : selectedCurrency.getId());
            } else {
                return null;
            }
        });

        createDefaultButtons(rb);

        Platform.runLater(this::createValidationSupport);
    }

    private void onCategoryTypeSelected() {
        CategoryType type = typeComboBox.getSelectionModel().getSelectedItem();

        List<Category> filtered = categories.stream()
                .filter(x -> x.getCatType().equals(type))
                .collect(Collectors.toList());

        categoryComboBox.setItems(FXCollections.observableArrayList(filtered));

        if (!filtered.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(categoryComboBox, (Control control, Category value) ->
                ValidationResult.fromErrorIf(control, null, value == null));
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }
}
