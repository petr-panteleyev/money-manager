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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.utilities.fx.Controller;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RequestTab extends Controller implements Initializable {
    private static final String FXML = "/org/panteleyev/money/RequestTab.fxml";

    private final TransactionTableView  transactionTable = new TransactionTableView(true);

    @FXML private BorderPane root;
    @FXML private ChoiceBox categoryTypeChoiceBox;
    @FXML private ChoiceBox categoryChoiceBox;
    @FXML private ChoiceBox accountChoiceBox;

    private final SimpleBooleanProperty preloadingProperty = new SimpleBooleanProperty();
    private final SimpleMapProperty<Integer, Category> categoriesProperty = new SimpleMapProperty<>();
    private final SimpleMapProperty<Integer, Account> accountsProperty = new SimpleMapProperty<>();

    private final SimpleStringProperty allTypesString = new SimpleStringProperty();
    private final SimpleStringProperty allCategoriesString = new SimpleStringProperty();
    private final SimpleStringProperty allAccountsString = new SimpleStringProperty();

    public RequestTab() {
        super(FXML, MainWindowController.UI_BUNDLE_PATH, false);

        MoneyDAO dao = MoneyDAO.getInstance();

        preloadingProperty.bind(dao.preloadingProperty());
        categoriesProperty.bind(dao.categoriesProperty());
        accountsProperty.bind(dao.accountsProperty());

        preloadingProperty.addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                Platform.runLater(this::setupCategoryTypesBox);
            }
        });

        categoriesProperty.addListener((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(() -> setupCategoryBox(getSelectedCategoryType()));
            }
        });

        accountsProperty.addListener((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(() -> setupAccountBox(getSelectedCategory()));
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        root.setCenter(transactionTable);

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
    }

    public BorderPane getRoot() {
        return root;
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

    public void onFindButton() {
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

    public void onClearButton() {
        transactionTable.clear();

        setupCategoryTypesBox();
    }
}
