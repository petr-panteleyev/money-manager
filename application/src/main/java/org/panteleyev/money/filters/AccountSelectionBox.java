package org.panteleyev.money.filters;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
import org.panteleyev.money.TransactionPredicate;
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

    private final PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();

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
                return obj instanceof CategoryType type ? type.getTypeName() : obj.toString();
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof Category category ? category.name() : obj.toString();
            }
        });

        accountChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof Account account ? account.name() : obj.toString();
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

        category.ifPresent(cat -> items.addAll(cache().getAccountsByCategory(cat.uuid())));

        if (items.size() > 1) {
            items.add(1, new Separator());
        }

        accountChoiceBox.setOnAction(accountHandler);
        accountChoiceBox.getSelectionModel().selectFirst();
    }

    private Optional<Account> getSelectedAccount() {
        var obj = accountChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Account account ? Optional.of(account) : Optional.empty();
    }

    private Optional<Category> getSelectedCategory() {
        var obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Category category ? Optional.of(category) : Optional.empty();
    }

    private Optional<CategoryType> getSelectedCategoryType() {
        var obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof CategoryType type ? Optional.of(type) : Optional.empty();
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

    public void setAccount(Account account) {
        var type = account.type();
        var category = cache().getCategory(account.categoryUuid()).orElseThrow();

        categoryTypeChoiceBox.getSelectionModel().select(type);
        categoryChoiceBox.getSelectionModel().select(category);
        accountChoiceBox.getSelectionModel().select(account);
    }
}
