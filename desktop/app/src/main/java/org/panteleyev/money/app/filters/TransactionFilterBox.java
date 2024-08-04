/*
 Copyright © 2018-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.transaction.TransactionPredicate;
import org.panteleyev.money.model.Transaction;

import java.time.LocalDate;
import java.util.function.Predicate;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.transaction.TransactionPredicate.transactionByDates;
import static org.panteleyev.money.app.transaction.TransactionPredicate.transactionByYear;

public class TransactionFilterBox extends HBox {
    private final ChoiceBox<Object> filterChoice = new ChoiceBox<>();

    private final DatePicker fromPicker = new DatePicker(LocalDate.now());
    private final DatePicker toPicker = new DatePicker(LocalDate.now());

    private final RadioButton periodRadio = new RadioButton();

    private final int filterYearsIndex;

    private final PredicateProperty<Transaction> predicateProperty =
            new PredicateProperty<>(TransactionPredicate.CURRENT_MONTH);

    private final EventHandler<ActionEvent> updateHandler =
            _ -> predicateProperty.set(getTransactionFilter());

    public TransactionFilterBox() {
        this(false, false);
    }

    public TransactionFilterBox(boolean showLabel, boolean showRange) {
        super(5.0);
        setAlignment(Pos.CENTER_LEFT);

        if (showRange) {
            getChildren().add(periodRadio);
        }

        getChildren().add(filterChoice);

        if (showRange) {
            var rangeRadio = new RadioButton();
            var toggleGroup = new ToggleGroup();
            periodRadio.setToggleGroup(toggleGroup);
            rangeRadio.setToggleGroup(toggleGroup);

            periodRadio.setOnAction(updateHandler);
            rangeRadio.setOnAction(updateHandler);

            fromPicker.setOnAction(updateHandler);
            toPicker.setOnAction(updateHandler);

            getChildren().addAll(rangeRadio, fromPicker, new Label(" - "), toPicker);

            filterChoice.disableProperty().bind(rangeRadio.selectedProperty());
            fromPicker.disableProperty().bind(periodRadio.selectedProperty());
            toPicker.disableProperty().bind(periodRadio.selectedProperty());

            periodRadio.setSelected(true);
        }

        // Init constant part of the list
        filterChoice.getItems().setAll(
                TransactionPredicate.ALL,
                new Separator(),
                TransactionPredicate.CURRENT_YEAR,
                TransactionPredicate.CURRENT_MONTH,
                TransactionPredicate.CURRENT_WEEK,
                new Separator(),
                TransactionPredicate.LAST_YEAR,
                TransactionPredicate.LAST_QUARTER,
                TransactionPredicate.LAST_MONTH,
                new Separator()
        );

        for (int i = TransactionPredicate.JANUARY.ordinal(); i <= TransactionPredicate.DECEMBER.ordinal(); i++) {
            filterChoice.getItems().add(TransactionPredicate.values()[i]);
        }

        filterChoice.getItems().add(new Separator());
        filterYearsIndex = filterChoice.getItems().size();

        filterChoice.setOnAction(updateHandler);
        filterChoice.getSelectionModel().select(0);
    }

    public PredicateProperty<Transaction> predicateProperty() {
        return predicateProperty;
    }

    public void setFilterYears() {
        filterChoice.setOnAction(_ -> {});

        // remove existing years
        if (filterYearsIndex < filterChoice.getItems().size()) {
            filterChoice.getItems().remove(filterYearsIndex, filterChoice.getItems().size());
        }

        // Add years from existing transactions
        cache().getTransactions().stream()
                .map(t -> t.transactionDate().getYear())
                .distinct()
                .sorted()
                .forEach(filterChoice.getItems()::add);

        filterChoice.setOnAction(updateHandler);
    }

    public Predicate<Transaction> getTransactionFilter() {
        if (periodRadio.isSelected()) {
            var selected = filterChoice.getSelectionModel().getSelectedItem();
            if (selected instanceof TransactionPredicate transactionPredicate) {
                return transactionPredicate;
            } else if (selected instanceof Integer) {
                return transactionByYear((int) selected);
            } else
                throw new IllegalStateException("Unexpected filter selection");
        } else {
            return transactionByDates(fromPicker.getValue(), toPicker.getValue());
        }
    }

    public void setTransactionFilter(TransactionPredicate filter) {
        periodRadio.setSelected(true);
        filterChoice.getSelectionModel().select(filter);
        predicateProperty.set(getTransactionFilter());
    }

    public void reset() {
        setTransactionFilter(TransactionPredicate.CURRENT_MONTH);
    }
}
