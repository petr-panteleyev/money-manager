/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class RequestTab extends BorderPane {
    private final TransactionTableView  transactionTable = new TransactionTableView(true);

    private final ResourceBundle rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final ChoiceBox categoryTypeChoiceBox = new ChoiceBox();
    private final ChoiceBox categoryChoiceBox = new ChoiceBox();
    private final ChoiceBox accountChoiceBox = new ChoiceBox();

    private final SimpleStringProperty allTypesString = new SimpleStringProperty();
    private final SimpleStringProperty allCategoriesString = new SimpleStringProperty();
    private final SimpleStringProperty allAccountsString = new SimpleStringProperty();

    private final MapChangeListener<Integer, Category> categoryListener =
            l -> Platform.runLater(() -> setupCategoryBox(getSelectedCategoryType()));
    private final MapChangeListener<Integer, Account> accountListener =
            l -> Platform.runLater(() -> setupAccountBox(getSelectedCategory()));

    RequestTab() {
        initialize();
    }

    private void initialize() {
        Button clearButton = new Button(rb.getString("button.Clear"));
        clearButton.setOnAction((ae) -> onClearButton());

        Button findButton = new Button(rb.getString("button.Find"));
        findButton.setOnAction((ae) -> onFindButton());

        HBox row1 = new HBox(5, clearButton, findButton);
        HBox row2 = new HBox(5, new Label(rb.getString("text.In.Semicolon")),
                categoryTypeChoiceBox, categoryChoiceBox, accountChoiceBox);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(5, row1, row2);

        setTop(vBox);
        setCenter(transactionTable);

        BorderPane.setMargin(vBox, new Insets(5, 5, 5, 5));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        allTypesString.set(rb.getString("account.Window.AllTypes"));
        allCategoriesString.set(rb.getString("account.Window.AllCategories"));
        allAccountsString.set(rb.getString("text.All.Accounts"));

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object obj) {
                return (obj instanceof CategoryType)?
                        ((CategoryType)obj).getName() : obj.toString();
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object obj) {
                return (obj instanceof Category)?
                        ((Category)obj).getName() : obj.toString();
            }
        });

        accountChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object obj) {
                return (obj instanceof Account)?
                        ((Account)obj).getName() : obj.toString();
            }
        });

        categoryTypeChoiceBox.setOnAction(e -> {
            if (categoryTypeChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
                setupCategoryBox(null);
            } else {
                setupCategoryBox((CategoryType)categoryTypeChoiceBox.getSelectionModel().getSelectedItem());
            }
        });

        categoryChoiceBox.setOnAction(e -> {
            if (categoryChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
                setupAccountBox(null);
            } else {
                setupAccountBox((Category)categoryChoiceBox.getSelectionModel().getSelectedItem());
            }
        });

        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        final MoneyDAO dao = MoneyDAO.getInstance();
        dao.categories().addListener(categoryListener);
        dao.accounts().addListener(accountListener);

        dao.preloadingProperty().addListener((x, y, newValue) -> {
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
            categoryChoiceBox.getItems().addAll(MoneyDAO.getInstance().getCategoriesByType(type));
        }

        categoryChoiceBox.getSelectionModel().select(0);

        setupAccountBox(null);
    }

    private void setupAccountBox(Category category) {
        accountChoiceBox.getItems().clear();
        accountChoiceBox.getItems().add(allAccountsString.get());

        if (category != null) {
            accountChoiceBox.getItems().addAll(MoneyDAO.getInstance().getAccountsByCategory(category.getId())
                    .stream().filter(Account::isEnabled)
                    .collect(Collectors.toList())
            );
        }

        accountChoiceBox.getSelectionModel().select(0);
    }

    private Account getSelectedAccount() {
        Object obj = accountChoiceBox.getSelectionModel().getSelectedItem();
        if (obj instanceof Account) {
            return (Account) obj;
        } else {
            return null;
        }
    }

    private Category getSelectedCategory() {
        Object obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        if (obj instanceof Category) {
            return (Category)obj;
        } else {
            return null;
        }
    }

    private CategoryType getSelectedCategoryType() {
        Object obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (obj instanceof CategoryType) {
            return (CategoryType)obj;
        } else {
            return null;
        }
    }

    private void onFindButton() {
        Collection<Transaction> transactions;

        MoneyDAO dao = MoneyDAO.getInstance();

        Account account = getSelectedAccount();
        if (account != null) {
            transactions = dao.getTransactions(Collections.singletonList(account));
        } else {
            Category category = getSelectedCategory();
            if (category != null) {
                List<Account> accounts = dao.getAccountsByCategory(category.getId());
                transactions = dao.getTransactions(accounts);
            } else {
                CategoryType type = getSelectedCategoryType();
                if (type != null) {
                    List<Category> categories = dao.getCategoriesByType(type);
                    transactions = dao.getTransactionsByCategories(categories);
                } else {
                    transactions = dao.getTransactions();
                }
            }
        }

        transactionTable.clear();
        transactionTable.addRecords(transactions.stream()
            .sorted(Transaction.BY_DATE)
            .collect(Collectors.toList()));
        transactionTable.sort();
    }

    private void onClearButton() {
        transactionTable.clear();

        setupCategoryTypesBox();
    }

    private void onCheckTransaction(List<Transaction> transactions, Boolean check) {
        MoneyDAO dao = MoneyDAO.getInstance();

        transactions.forEach(t -> dao.updateTransaction(new Transaction.Builder(t)
                .checked(check)
                .build()));
    }
}
