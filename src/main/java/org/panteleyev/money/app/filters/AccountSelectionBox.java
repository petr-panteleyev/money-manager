/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.filters;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.Predicates;
import org.panteleyev.money.app.TransactionPredicate;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import java.util.Optional;
import java.util.function.Predicate;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Constants.ALL_TYPES_STRING;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Predicates.accountByCategory;
import static org.panteleyev.money.app.Predicates.accountByUuid;
import static org.panteleyev.money.app.TransactionPredicate.transactionByAccount;
import static org.panteleyev.money.app.TransactionPredicate.transactionByCategory;
import static org.panteleyev.money.app.icons.IconManager.ACCOUNT_TO_IMAGE;
import static org.panteleyev.money.app.icons.IconManager.CATEGORY_TO_IMAGE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ALL_ACCOUNTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ALL_CATEGORIES;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.COMPARE_ACCOUNT_BY_NAME;
import static org.panteleyev.money.persistence.MoneyDAO.COMPARE_CATEGORY_BY_NAME;

public class AccountSelectionBox extends HBox {
    private final static String ALL_CATEGORIES_STRING = UI.getString(I18N_MISC_ALL_CATEGORIES);
    private final static String ALL_ACCOUNTS_STRING = UI.getString(I18N_MISC_ALL_ACCOUNTS);

    private final ComboBox<CategoryType> categoryTypeBox =
        comboBox(CategoryType.values(), b -> b.withDefaultString(ALL_TYPES_STRING));

    private final FilteredList<Category> filteredCategories = cache().getCategories().filtered(c -> true);
    private final ComboBox<Category> categoryBox = comboBox(filteredCategories.sorted(COMPARE_CATEGORY_BY_NAME),
        b -> b.withDefaultString(ALL_CATEGORIES_STRING)
            .withStringConverter(Category::name)
            .withImageConverter(CATEGORY_TO_IMAGE));
    private final FilteredList<Account> filteredAccounts = cache().getAccounts().filtered(a -> true);
    private final ComboBox<Account> accountBox = comboBox(filteredAccounts.sorted(COMPARE_ACCOUNT_BY_NAME),
        b -> b.withDefaultString(ALL_ACCOUNTS_STRING)
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
