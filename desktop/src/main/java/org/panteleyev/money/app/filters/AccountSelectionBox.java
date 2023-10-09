/*
 Copyright © 2018-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.Predicates;
import org.panteleyev.money.app.transaction.TransactionPredicate;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;

import java.util.Optional;
import java.util.function.Predicate;

import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.Predicates.accountByCategory;
import static org.panteleyev.money.app.Predicates.accountByUuid;
import static org.panteleyev.money.app.icons.IconManager.ACCOUNT_TO_IMAGE;
import static org.panteleyev.money.app.icons.IconManager.CATEGORY_TO_IMAGE;
import static org.panteleyev.money.app.transaction.TransactionPredicate.transactionByAccount;
import static org.panteleyev.money.app.transaction.TransactionPredicate.transactionByCategory;

public class AccountSelectionBox extends HBox {
    private final ComboBox<CategoryType> categoryTypeBox =
            comboBox(CategoryType.values(),
                    b -> b.withDefaultString("Все типы")
                            .withStringConverter(Bundles::translate)
            );

    private final FilteredList<Category> filteredCategories = cache().getCategories().filtered(c -> true);
    private final ComboBox<Category> categoryBox = comboBox(
            filteredCategories.sorted(Comparators.categoriesByName()),
            b -> b.withDefaultString("Все категории")
                    .withStringConverter(Category::name)
                    .withImageConverter(CATEGORY_TO_IMAGE));
    private final FilteredList<Account> filteredAccounts = cache().getAccounts().filtered(a -> true);
    private final ComboBox<Account> accountBox = comboBox(filteredAccounts.sorted(Comparators.accountsByName()),
            b -> b.withDefaultString("Все счета")
                    .withStringConverter(Account::name)
                    .withImageConverter(ACCOUNT_TO_IMAGE));
    private final PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();

    public AccountSelectionBox() {
        super(5.0);

        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(categoryTypeBox, categoryBox, accountBox);

        categoryBox.setVisibleRowCount(20);
        accountBox.setVisibleRowCount(20);

        categoryTypeBox.setOnAction(
                event -> setupCategoryBox(getSelectedCategoryType().orElse(null))
        );
        categoryBox.setOnAction(
                event -> setupAccountBox(getSelectedCategory().orElse(null))
        );
        accountBox.setOnAction(event -> updatePredicate());

        Platform.runLater(this::reset);
    }

    public PredicateProperty<Transaction> predicateProperty() {
        return predicateProperty;
    }

    public void reset() {
        clearValueAndSelection(categoryTypeBox, categoryBox, accountBox);
    }

    private void setupCategoryBox(CategoryType type) {
        var selectedAccount = getSelectedAccount();

        filteredCategories.setPredicate(type == null ? c -> true : c -> c.type() == type);

        categoryBox.setValue(null);
        categoryBox.getSelectionModel().select(null);

        if (selectedAccount.isEmpty()) {
            updatePredicate();
        }
    }

    private void setupAccountBox(Category category) {
        var selectedAccount = getSelectedAccount();

        filteredAccounts.setPredicate(
                category == null ? a -> true : a -> a.categoryUuid().equals(category.uuid())
        );

        accountBox.setValue(null);
        accountBox.getSelectionModel().select(null);

        if (selectedAccount.isEmpty()) {
            updatePredicate();
        }
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(accountBox.getSelectionModel().getSelectedItem());
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(categoryBox.getSelectionModel().getSelectedItem());
    }

    private Optional<CategoryType> getSelectedCategoryType() {
        return Optional.ofNullable(categoryTypeBox.getSelectionModel().getSelectedItem());
    }

    Predicate<Transaction> getTransactionFilter() {
        return getSelectedAccount().map(a -> transactionByAccount(a.uuid()))
                .orElseGet(() -> getSelectedCategory().map(c -> transactionByCategory(c.uuid()))
                        .orElseGet(() -> getSelectedCategoryType().map(TransactionPredicate::transactionByCategoryType)
                                .orElse(x -> true)));
    }

    public Predicate<Account> getAccountFilter() {
        return getSelectedAccount().map(a -> accountByUuid(a.uuid()))
                .orElseGet(() -> getSelectedCategory().map(c -> accountByCategory(c.uuid()))
                        .orElseGet(() -> getSelectedCategoryType().map(Predicates::accountByCategoryType)
                                .orElse(x -> true)));
    }

    private void updatePredicate() {
        predicateProperty.set(getTransactionFilter());
    }
}
