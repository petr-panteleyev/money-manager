/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.AccountFilter;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionFilter;
import java.util.Optional;
import java.util.function.Predicate;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class AccountFilterSelectionBox extends HBox {
    private final ChoiceBox<Object> categoryTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> accountChoiceBox = new ChoiceBox<>();

    private final SimpleStringProperty allTypesString = new SimpleStringProperty();
    private final SimpleStringProperty allCategoriesString = new SimpleStringProperty();
    private final SimpleStringProperty allAccountsString = new SimpleStringProperty();

    public AccountFilterSelectionBox() {
        super(5.0);
        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(new Label(RB.getString("text.In.Semicolon")),
                categoryTypeChoiceBox, categoryChoiceBox, accountChoiceBox);

        allTypesString.set(RB.getString("account.Window.AllTypes"));
        allCategoriesString.set(RB.getString("account.Window.AllCategories"));
        allAccountsString.set(RB.getString("text.All.Accounts"));

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                if (obj instanceof CategoryType) {
                    return ((CategoryType) obj).getTypeName();
                } else {
                    return obj.toString();
                }
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                if (obj instanceof Category) {
                    return ((Category) obj).getName();
                } else {
                    return obj.toString();
                }
            }
        });

        accountChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                if (obj instanceof Account) {
                    return ((Account) obj).getName();
                } else {
                    return obj.toString();
                }
            }
        });

        categoryTypeChoiceBox.setOnAction(event -> setupCategoryBox(getSelectedCategoryType()));
        categoryChoiceBox.setOnAction(event -> setupAccountBox(getSelectedCategory()));

        MapChangeListener<Integer, Category> categoryListener = change ->
                Platform.runLater(() -> setupCategoryBox(getSelectedCategoryType()));

        MapChangeListener<Integer, Account> accountListener = change ->
                Platform.runLater(() -> setupAccountBox(getSelectedCategory()));

        getDao().categories().addListener(categoryListener);
        getDao().accounts().addListener(accountListener);
        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::setupCategoryTypesBox);
            }
        });
    }

    public void setupCategoryTypesBox() {
        categoryTypeChoiceBox.getItems().clear();
        categoryTypeChoiceBox.getItems().add(allTypesString.get());
        categoryTypeChoiceBox.getItems().add(new Separator());
        categoryTypeChoiceBox.getItems().addAll(CategoryType.values());

        categoryTypeChoiceBox.getSelectionModel().select(0);
        setupCategoryBox(Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void setupCategoryBox(Optional<CategoryType> categoryType) {
        categoryChoiceBox.getItems().clear();
        categoryChoiceBox.getItems().add(allCategoriesString.get());

        categoryType.ifPresent(type -> getDao().getCategoriesByType(type)
                .forEach(t -> categoryChoiceBox.getItems().add(t)));

        categoryChoiceBox.getSelectionModel().clearAndSelect(0);
        setupAccountBox(Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void setupAccountBox(Optional<Category> category) {
        accountChoiceBox.getItems().clear();
        accountChoiceBox.getItems().add(allAccountsString.get());

        category.ifPresent(cat -> getDao().getAccountsByCategory(cat.getId()).stream()
                .filter(Account::getEnabled)
                .forEach(a -> accountChoiceBox.getItems().add(a)));

        accountChoiceBox.getSelectionModel().clearAndSelect(0);
    }

    private Optional<Account> getSelectedAccount() {
        Object obj = accountChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Account ? Optional.of((Account) obj) : Optional.empty();
    }

    private Optional<Category> getSelectedCategory() {
        Object obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Category ? Optional.of((Category) obj) : Optional.empty();
    }

    private Optional<CategoryType> getSelectedCategoryType() {
        Object obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof CategoryType ? Optional.of((CategoryType) obj) : Optional.empty();
    }

    public Predicate<Transaction> getTransactionFilter() {
        return getSelectedAccount().map(a -> TransactionFilter.byAccount(a.getId()))
                .orElseGet(() -> getSelectedCategory().map(c -> TransactionFilter.byCategory(c.getId()))
                        .orElseGet(() -> getSelectedCategoryType().map(t -> TransactionFilter.byCategoryType(t.getId()))
                                .orElse(x -> true)));
    }

    public Predicate<Account> getAccountFilter() {
        return getSelectedAccount().map(a -> AccountFilter.byAccount(a.getId()))
                .orElseGet(() -> getSelectedCategory().map(c -> AccountFilter.byCategory(c.getId()))
                        .orElseGet(() -> getSelectedCategoryType().map(t -> AccountFilter.byCategoryType(t.getId()))
                                .orElse(x -> true)));
    }
}
