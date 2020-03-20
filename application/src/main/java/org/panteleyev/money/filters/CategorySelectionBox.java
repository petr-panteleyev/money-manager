package org.panteleyev.money.filters;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import static org.panteleyev.money.Predicates.accountByCategory;
import static org.panteleyev.money.Predicates.accountByCategoryType;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;

public class CategorySelectionBox extends HBox {
    private static class TypeListItem {
        private final String text;
        private final EnumSet<CategoryType> types;

        TypeListItem(String text, CategoryType type, CategoryType... types) {
            this.text = text;
            this.types = EnumSet.of(type, types);
        }

        String getText() {
            return text;
        }

        EnumSet<CategoryType> getTypes() {
            return types;
        }
    }

    private final ChoiceBox<Object> categoryTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();

    private final PredicateProperty<Account> accountFilterProperty = new PredicateProperty<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Category> categoryListener = change ->
        Platform.runLater(this::onTypeChanged);

    private final EventHandler<ActionEvent> categoryTypeHandler =
        event -> onTypeChanged();

    private final EventHandler<ActionEvent> categoryHandler =
        event -> accountFilterProperty.set(getAccountFilter());

    public CategorySelectionBox() {
        super(5.0);

        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(categoryTypeChoiceBox, categoryChoiceBox);

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof TypeListItem item) {
                    return item.getText();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object obj) {
                return obj instanceof Category category ? category.name() : obj.toString();
            }
        });

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryChoiceBox.setOnAction(categoryHandler);

        cache().categories().addListener(new WeakMapChangeListener<>(categoryListener));
    }

    public PredicateProperty<Account> accountFilterProperty() {
        return accountFilterProperty;
    }

    public void setupCategoryTypesBox() {
        categoryTypeChoiceBox.setOnAction(event -> {});

        categoryTypeChoiceBox.getItems().setAll(
            new TypeListItem(RB.getString("text.AccountsCashCards"),
                CategoryType.BANKS_AND_CASH, CategoryType.DEBTS),
            new TypeListItem(RB.getString("Incomes_and_Expenses"),
                CategoryType.INCOMES, CategoryType.EXPENSES),
            new Separator()
        );

        for (var t : CategoryType.values()) {
            categoryTypeChoiceBox.getItems().add(new TypeListItem(t.getTypeName(), t));
        }

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryTypeChoiceBox.getSelectionModel().selectFirst();
    }

    private Optional<Category> getSelectedCategory() {
        var obj = categoryChoiceBox.getSelectionModel().getSelectedItem();
        return obj instanceof Category category ? Optional.of(category) : Optional.empty();
    }

    private Set<CategoryType> getSelectedCategoryTypes() {
        var obj = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (obj instanceof TypeListItem item) {
            return item.getTypes();
        } else {
            return Set.of();
        }
    }

    private Predicate<Account> getAccountFilter() {
        return getSelectedCategory().map(c -> accountByCategory(c.uuid()))
            .orElseGet(() -> {
                var selectedTypes = getSelectedCategoryTypes();
                return selectedTypes.isEmpty() ? a -> false :
                    accountByCategoryType(selectedTypes);
            });
    }

    private void onTypeChanged() {
        categoryChoiceBox.setOnAction(x -> {});
        var object = categoryTypeChoiceBox.getSelectionModel().getSelectedItem();

        if (!(object instanceof TypeListItem typeListItem)) {
            return;
        }

        ObservableList<Object> items =
            FXCollections.observableArrayList(cache().getCategoriesByType(typeListItem.getTypes()));

        if (!items.isEmpty()) {
            items.add(0, new Separator());
        }

        items.add(0, RB.getString("All_Categories"));

        categoryChoiceBox.setItems(items);
        categoryChoiceBox.getSelectionModel().selectFirst();
        categoryChoiceBox.setOnAction(categoryHandler);
        accountFilterProperty.set(getAccountFilter());
    }
}
