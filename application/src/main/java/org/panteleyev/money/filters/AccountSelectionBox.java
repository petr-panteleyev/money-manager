/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.filters;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.Predicates;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.Predicates.accountByCategory;
import static org.panteleyev.money.Predicates.accountByUuid;
import static org.panteleyev.money.TransactionPredicate.transactionByAccount;
import static org.panteleyev.money.TransactionPredicate.transactionByCategory;
import static org.panteleyev.money.TransactionPredicate.transactionByCategoryType;
import static org.panteleyev.money.persistence.DataCache.cache;

public class AccountSelectionBox extends HBox {
    private final ChoiceBox<Object> categoryTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> accountChoiceBox = new ChoiceBox<>();

    private final static String ALL_TYPES_STRING = RB.getString("All_Types");
    private final static String ALL_CATEGORIES_STRING = RB.getString("All_Categories");
    private final static String ALL_ACCOUNTS_STRING = RB.getString("text.All.Accounts");

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Category> categoryListener = change ->
        Platform.runLater(() -> setupCategoryBox(getSelectedCategoryType()));

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Account> accountListener =
        change -> Platform.runLater(() -> setupAccountBox(getSelectedCategory()));

    private PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();

    private final EventHandler<ActionEvent> categoryTypeHandler =
        event -> setupCategoryBox(getSelectedCategoryType());
    private final EventHandler<ActionEvent> categoryHandler =
        event -> setupAccountBox(getSelectedCategory());
    private final EventHandler<ActionEvent> accountHandler =
        event -> predicateProperty.set(getTransactionFilter());

    public AccountSelectionBox() {
        super(5.0);

        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(newLabel(RB, "text.In.Semicolon"),
            categoryTypeChoiceBox, categoryChoiceBox, accountChoiceBox);

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof CategoryType ? ((CategoryType) obj).getTypeName() : obj.toString();
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof Category ? ((Category) obj).getName() : obj.toString();
            }
        });

        accountChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof Account ? ((Account) obj).getName() : obj.toString();
            }
        });

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryChoiceBox.setOnAction(categoryHandler);
        accountChoiceBox.setOnAction(accountHandler);

        cache().categories().addListener(new WeakMapChangeListener<>(categoryListener));
        cache().accounts().addListener(new WeakMapChangeListener<>(accountListener));
    }

    public PredicateProperty<Transaction> predicateProperty() {
        return predicateProperty;
    }

    public void setupCategoryTypesBox() {
        categoryTypeChoiceBox.setOnAction(event -> {});
        var items = categoryTypeChoiceBox.getItems();
        items.setAll(ALL_TYPES_STRING, new Separator());
        items.addAll(CategoryType.values());

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryTypeChoiceBox.getSelectionModel().selectFirst();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void setupCategoryBox(Optional<CategoryType> categoryType) {
        categoryChoiceBox.setOnAction(s -> {});

        var items = categoryChoiceBox.getItems();
        items.setAll(ALL_CATEGORIES_STRING);

        categoryType.ifPresent(type -> items.addAll(cache().getCategoriesByType(type)));

        if (items.size() > 1) {
            items.add(1, new Separator());
        }

        categoryChoiceBox.setOnAction(categoryHandler);
        categoryChoiceBox.getSelectionModel().selectFirst();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void setupAccountBox(Optional<Category> category) {
        accountChoiceBox.setOnAction(event -> {});

        var items = accountChoiceBox.getItems();
        items.setAll(ALL_ACCOUNTS_STRING);

        category.ifPresent(cat -> items.addAll(cache().getAccountsByCategory(cat.getUuid())));

        if (items.size() > 1) {
            items.add(1, new Separator());
        }

        accountChoiceBox.setOnAction(accountHandler);
        accountChoiceBox.getSelectionModel().selectFirst();
    }

    private Optional<Account> getSelectedAccount() {
        var obj = accountChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Account ? Optional.of((Account) obj) : Optional.empty();
    }

    private Optional<Category> getSelectedCategory() {
        var obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Category ? Optional.of((Category) obj) : Optional.empty();
    }

    private Optional<CategoryType> getSelectedCategoryType() {
        var obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof CategoryType ? Optional.of((CategoryType) obj) : Optional.empty();
    }

    Predicate<Transaction> getTransactionFilter() {
        return getSelectedAccount().map(a -> transactionByAccount(a.getUuid()))
            .orElseGet(() -> getSelectedCategory().map(c -> transactionByCategory(c.getUuid()))
                .orElseGet(() -> getSelectedCategoryType().map(t -> transactionByCategoryType(t.getId()))
                    .orElse(x -> true)));
    }

    public Predicate<Account> getAccountFilter() {
        return getSelectedAccount().map(a -> accountByUuid(a.getUuid()))
            .orElseGet(() -> getSelectedCategory().map(c -> accountByCategory(c.getUuid()))
                .orElseGet(() -> getSelectedCategoryType().map(Predicates::accountByCategoryType)
                    .orElse(x -> true)));
    }

    public void setAccount(Account account) {
        var type = account.getType();
        var category = cache().getCategory(account.getCategoryUuid()).orElseThrow();

        categoryTypeChoiceBox.getSelectionModel().select(type);
        categoryChoiceBox.getSelectionModel().select(category);
        accountChoiceBox.getSelectionModel().select(account);
    }
}
