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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;

class CategoryTypeComboBox extends ComboBox<CategoryType> {
    // TODO: get rid of these objects
    private static final CategoryType DUMMY = new CategoryType(0, null, null);


    public CategoryTypeComboBox() {
        setConverter(new ReadOnlyStringConverter<CategoryType>() {
            @Override
            public String toString(CategoryType object) {
                return object == DUMMY? "-- All Types --" : object.getName();
            }
        });
    }

    public CategoryType getCategoryType() {
        CategoryType type = getSelectionModel().getSelectedItem();
        return type == DUMMY? null : type;
    }

    public void reload(boolean dbOpen) {
        getItems().clear();
        getItems().add(DUMMY);

        if (dbOpen) {
            getItems().addAll(MoneyDAO.getInstance().getCategoryTypes());
        }

        getSelectionModel().select(0);
    }
}

class CategoryComboBox extends ComboBox<Category> {
    // TODO: get rid of these objects
    private static final Category DUMMY = new Category(0, null, null, 0, false);

    public CategoryComboBox() {
        setConverter(new ReadOnlyStringConverter<Category>() {
            @Override
            public String toString(Category object) {
                return object == DUMMY? "-- All Categories --" : object.getName();
            }
        });
    }

    public Category getCategory() {
        Category category = getSelectionModel().getSelectedItem();
        return category == DUMMY? null : category;
    }

    public void reload(boolean dbOpen, CategoryType type) {
        getItems().clear();
        getItems().add(DUMMY);

        if (dbOpen && type != null) {
            getItems().addAll(MoneyDAO.getInstance().getCategoriesByType(type));
        }

        getSelectionModel().select(0);
    }
}

class AccountComboBox extends ComboBox<Account> {
    // TODO: get rid of these objects
    private static final Account DUMMY = new Account(null, null, null, null, null, null, null, null, null, false);

    public AccountComboBox() {
        setConverter(new ReadOnlyStringConverter<Account>() {
            @Override
            public String toString(Account object) {
                return object == DUMMY? "-- All Accounts --" : object.getName();
            }
        });
    }

    public Account getAccount() {
        Account acc = getSelectionModel().getSelectedItem();
        return acc == DUMMY? null : acc;
    }

    public void reload(boolean dbOpen, Category category) {
        getItems().clear();
        getItems().add(DUMMY);

        if (dbOpen && category != null) {
            getItems().addAll(MoneyDAO.getInstance().getAccountsByCategory(category.getId())
                .stream().filter(Account::isEnabled)
                .collect(Collectors.toList())
            );
        }

        getSelectionModel().select(0);
    }
}

public class RequestTab extends BorderPane {
    private final TransactionTableView  transactionTable = new TransactionTableView(true);
    private final CategoryTypeComboBox categoryTypeComboBox = new CategoryTypeComboBox();
    private final CategoryComboBox categoryComboBox = new CategoryComboBox();
    private final AccountComboBox accountComboBox = new AccountComboBox();

    public RequestTab() {
        setCenter(transactionTable);

        VBox topPanel = new VBox();

        setTop(topPanel);


        HBox buttonPanel = new HBox();
        Button findButton = new Button("Find");
        findButton.setOnAction(e -> onFindButton());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> onClearButton());

        buttonPanel.getChildren().addAll(
            clearButton,
            findButton
        );

        GridPane requestPanel = new GridPane();

        int row = 0;

        requestPanel.add(new Label("In:"), 1, ++row);
        requestPanel.add(categoryTypeComboBox, 2, row);
        requestPanel.add(categoryComboBox, 3, row);
        requestPanel.add(accountComboBox, 4, row);

        topPanel.getChildren().addAll(
            buttonPanel,
            requestPanel
        );
    }

    public void initControls(boolean dbOpen) {
        categoryTypeComboBox.reload(dbOpen);
        categoryComboBox.reload(false, null);
        accountComboBox.reload(false, null);

        categoryTypeComboBox.setOnAction(e -> {
            accountComboBox.reload(false, null);
            if (categoryTypeComboBox.getSelectionModel().getSelectedIndex() == 0) {
                categoryComboBox.reload(false, null);
            } else {
                categoryComboBox.reload(true, categoryTypeComboBox.getSelectionModel().getSelectedItem());
            }
        });

        categoryComboBox.setOnAction(e -> {
            if (categoryComboBox.getSelectionModel().getSelectedIndex() == 0) {
                accountComboBox.reload(false, null);
            } else {
                accountComboBox.reload(true, categoryComboBox.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void onFindButton() {
        Collection<Transaction> transactions;

        MoneyDAO dao = MoneyDAO.getInstance();

        Account account = accountComboBox.getAccount();
        if (account != null) {
            transactions = dao.getTransactions(Collections.singletonList(account));
        } else {
            Category category = categoryComboBox.getCategory();
            if (category != null) {
                List<Account> accounts = dao.getAccountsByCategory(category.getId());
                transactions = dao.getTransactions(accounts);
            } else {
                CategoryType type = categoryTypeComboBox.getCategoryType();
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

        categoryTypeComboBox.reload(MoneyDAO.isOpen());
        categoryComboBox.reload(false, null);
        accountComboBox.reload(false, null);
    }
}
