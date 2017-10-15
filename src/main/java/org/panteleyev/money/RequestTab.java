/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionFilter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class RequestTab extends BorderPane {
    private static final ResourceBundle rb = MainWindowController.RB;


    private final TransactionTableView transactionTable = new TransactionTableView(true);

    private final ChoiceBox<Object> categoryTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> accountChoiceBox = new ChoiceBox<>();

    private final SimpleStringProperty allTypesString = new SimpleStringProperty();
    private final SimpleStringProperty allCategoriesString = new SimpleStringProperty();
    private final SimpleStringProperty allAccountsString = new SimpleStringProperty();

    private final MapChangeListener<Integer, Category> categoryListener = change ->
            Platform.runLater(() -> setupCategoryBox(getSelectedCategoryType()));

    private final MapChangeListener<Integer, Account> accountListener = change ->
            Platform.runLater(() -> setupAccountBox(getSelectedCategory()));


    public RequestTab() {
        Button clearButton = new Button(rb.getString("button.Clear"));
        clearButton.setOnAction(event -> onClearButton());

        Button findButton = new Button(rb.getString("button.Find"));
        findButton.setOnAction(event -> onFindButton());

        HBox row1 = new HBox(5.0, clearButton, findButton);
        HBox row2 = new HBox(5.0, new Label(rb.getString("text.In.Semicolon")),
                categoryTypeChoiceBox, categoryChoiceBox, accountChoiceBox);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(5.0, row1, row2);

        setTop(vBox);
        setCenter(transactionTable);

        BorderPane.setMargin(vBox, new Insets(5.0, 5.0, 5.0, 5.0));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        allTypesString.set(rb.getString("account.Window.AllTypes"));
        allCategoriesString.set(rb.getString("account.Window.AllCategories"));
        allAccountsString.set(rb.getString("text.All.Accounts"));

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter<Object>() {
            @Override
            public String toString(Object obj) {
                if (obj instanceof CategoryType) {
                    return ((CategoryType) obj).getTypeName();
                } else {
                    return obj.toString();
                }
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<Object>() {
            public String toString(Object obj) {
                if (obj instanceof Category) {
                    return ((Category) obj).getName();
                } else {
                    return obj.toString();
                }
            }
        });

        accountChoiceBox.setConverter(new ReadOnlyStringConverter<Object>() {
            public String toString(Object obj) {
                if (obj instanceof Account) {
                    return ((Account) obj).getName();
                } else {
                    return obj.toString();
                }
            }
        });

        categoryTypeChoiceBox.setOnAction(event -> {
            if (categoryTypeChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
                setupCategoryBox(null);
            } else {
                setupCategoryBox((CategoryType) categoryTypeChoiceBox.getSelectionModel().getSelectedItem());
            }
        });

        categoryChoiceBox.setOnAction(event -> {
            if (categoryChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
                setupAccountBox(null);
            } else {
                setupAccountBox((Category) categoryChoiceBox.getSelectionModel().getSelectedItem());
            }
        });

        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        getDao().categories().addListener(categoryListener);
        getDao().accounts().addListener(accountListener);

        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::setupCategoryTypesBox);
            }
        });
    }

    private void setupCategoryTypesBox() {
        categoryTypeChoiceBox.getItems().clear();
        categoryTypeChoiceBox.getItems().add(allTypesString.get());
        categoryTypeChoiceBox.getItems().add(new Separator());
        categoryTypeChoiceBox.getItems().addAll(CategoryType.values());

        categoryTypeChoiceBox.getSelectionModel().select(0);

        setupCategoryBox(null);
    }

    private void setupCategoryBox(CategoryType type) {
        categoryChoiceBox.getItems().clear();
        categoryChoiceBox.getItems().add(allCategoriesString.get());

        if (type != null) {
            getDao().getCategoriesByType(type).forEach(t -> categoryChoiceBox.getItems().add(t));
        }

        categoryChoiceBox.getSelectionModel().clearAndSelect(0);
        setupAccountBox(null);
    }

    private void setupAccountBox(Category category) {
        accountChoiceBox.getItems().clear();
        accountChoiceBox.getItems().add(allAccountsString.get());

        if (category != null) {
            getDao().getAccountsByCategory(category.getId()).stream()
                    .filter(Account::getEnabled)
                    .forEach(a -> accountChoiceBox.getItems().add(a));
        }

        accountChoiceBox.getSelectionModel().clearAndSelect(0);
    }

    private Account getSelectedAccount() {
        Object obj = accountChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Account ? (Account) obj : null;
    }

    private Category getSelectedCategory() {
        Object obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Category ? (Category) obj : null;
    }

    private CategoryType getSelectedCategoryType() {
        Object obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof CategoryType ? (CategoryType) obj : null;
    }

    private void onFindButton() {
        Predicate<Transaction> filter = x -> true;

        Account account = getSelectedAccount();
        if (account != null) {
            filter = TransactionFilter.byAccount(account.getId());
        } else {
            Category category = getSelectedCategory();
            if (category != null) {
                filter = TransactionFilter.byCategory(category.getId());
            } else {
                CategoryType type = getSelectedCategoryType();
                if (type != null) {
                    filter = TransactionFilter.byCategoryType(type.getId());
                }
            }
        }

        transactionTable.setTransactionFilter(filter);
    }

    private void onClearButton() {
        transactionTable.setTransactionFilter(x -> false);
        setupCategoryTypesBox();
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (Transaction t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
    }
}
