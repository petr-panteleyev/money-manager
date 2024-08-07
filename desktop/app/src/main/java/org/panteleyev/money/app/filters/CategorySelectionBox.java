/*
 Copyright © 2020-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.desktop.commons.ReadOnlyStringConverter;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static javafx.collections.FXCollections.observableArrayList;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.Predicates.accountByCategory;
import static org.panteleyev.money.app.Predicates.accountByCategoryType;

public class CategorySelectionBox extends HBox {

    private record TypeListItem(String text, EnumSet<CategoryType> types) {
        static TypeListItem of(String text, CategoryType type, CategoryType... types) {
            return new TypeListItem(text, EnumSet.of(type, types));
        }
    }

    private final ChoiceBox<Object> categoryTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();

    private final PredicateProperty<Account> accountFilterProperty = new PredicateProperty<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Category> categoryListener = _ ->
            Platform.runLater(this::onTypeChanged);

    private final EventHandler<ActionEvent> categoryTypeHandler =
            _ -> onTypeChanged();

    private final EventHandler<ActionEvent> categoryHandler =
            _ -> accountFilterProperty.set(getAccountFilter());

    public CategorySelectionBox() {
        super(5.0);

        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(categoryTypeChoiceBox, categoryChoiceBox);

        categoryTypeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof TypeListItem item) {
                    return item.text();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Category category) {
                    return category.name();
                } else {
                    return object != null ? object.toString() : "";
                }
            }
        });

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryChoiceBox.setOnAction(categoryHandler);

        cache().getCategories().addListener(new WeakListChangeListener<>(categoryListener));
    }

    public PredicateProperty<Account> accountFilterProperty() {
        return accountFilterProperty;
    }

    public void setupCategoryTypesBox() {
        categoryTypeChoiceBox.setOnAction(_ -> {});

        categoryTypeChoiceBox.getItems().setAll(
                TypeListItem.of("Счета, наличные, карты",
                        CategoryType.BANKS_AND_CASH, CategoryType.DEBTS),
                TypeListItem.of("Доходы и расходы",
                        CategoryType.INCOMES, CategoryType.EXPENSES),
                new Separator()
        );

        for (var t : CategoryType.values()) {
            categoryTypeChoiceBox.getItems().add(TypeListItem.of(Bundles.translate(t), t));
        }

        categoryTypeChoiceBox.setOnAction(categoryTypeHandler);
        categoryTypeChoiceBox.getSelectionModel().selectFirst();
    }

    private Optional<Category> getSelectedCategory() {
        return categoryChoiceBox.getSelectionModel().getSelectedItem() instanceof Category category ?
                Optional.of(category) : Optional.empty();
    }

    private Set<CategoryType> getSelectedCategoryTypes() {
        return categoryTypeChoiceBox.getSelectionModel().getSelectedItem() instanceof TypeListItem item ?
                item.types() : Set.of();
    }

    private Predicate<Account> getAccountFilter() {
        return getSelectedCategory().map(c -> accountByCategory(c.uuid()))
                .orElseGet(() -> {
                    var selectedTypes = getSelectedCategoryTypes();
                    return selectedTypes.isEmpty() ? _ -> false :
                            accountByCategoryType(selectedTypes);
                });
    }

    private void onTypeChanged() {
        categoryChoiceBox.setOnAction(_ -> {});

        if (!(categoryTypeChoiceBox.getSelectionModel().getSelectedItem() instanceof TypeListItem typeListItem)) {
            return;
        }

        ObservableList<Object> items =
                observableArrayList(
                        cache().getCategoriesByType(typeListItem.types()).stream()
                                .sorted(Category.COMPARE_BY_NAME)
                                .toList()
                );

        if (!items.isEmpty()) {
            items.addFirst(new Separator());
        }

        items.addFirst("Все категории");

        categoryChoiceBox.setItems(items);
        categoryChoiceBox.getSelectionModel().selectFirst();
        categoryChoiceBox.setOnAction(categoryHandler);
        accountFilterProperty.set(getAccountFilter());
    }
}
