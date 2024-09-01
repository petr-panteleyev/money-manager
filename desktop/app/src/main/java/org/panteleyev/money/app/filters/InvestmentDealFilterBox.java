/*
 Copyright © 2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.investment.InvestmentDealPredicate;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;

import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.investment.InvestmentDealPredicate.byDates;
import static org.panteleyev.money.app.investment.InvestmentDealPredicate.byYear;

public class InvestmentDealFilterBox extends HBox {
    private final ChoiceBox<Object> filterChoice = new ChoiceBox<>();

    private final DatePicker fromPicker = new DatePicker(LocalDate.now());
    private final DatePicker toPicker = new DatePicker(LocalDate.now());

    private final RadioButton periodRadio = new RadioButton();

    private final int filterYearsIndex;

    private final PredicateProperty<InvestmentDeal> predicateProperty =
            new PredicateProperty<>(InvestmentDealPredicate.CURRENT_MONTH);

    private final EventHandler<ActionEvent> updateHandler =
            _ -> predicateProperty.set(getDealFilter());

    public InvestmentDealFilterBox() {
        super(BIG_SPACING);
        setAlignment(Pos.CENTER_LEFT);

        var rangeRadio = new RadioButton();
        var toggleGroup = new ToggleGroup();
        periodRadio.setToggleGroup(toggleGroup);
        rangeRadio.setToggleGroup(toggleGroup);

        periodRadio.setOnAction(updateHandler);
        rangeRadio.setOnAction(updateHandler);

        fromPicker.setOnAction(updateHandler);
        toPicker.setOnAction(updateHandler);

        getChildren().addAll(
                periodRadio,
                filterChoice,
                rangeRadio,
                fromPicker,
                label(" - "),
                toPicker
        );

        filterChoice.disableProperty().bind(rangeRadio.selectedProperty());
        fromPicker.disableProperty().bind(periodRadio.selectedProperty());
        toPicker.disableProperty().bind(periodRadio.selectedProperty());

        periodRadio.setSelected(true);

        // Init constant part of the list
        filterChoice.getItems().setAll(
                InvestmentDealPredicate.ALL,
                new Separator(),
                InvestmentDealPredicate.CURRENT_YEAR,
                InvestmentDealPredicate.CURRENT_MONTH,
                InvestmentDealPredicate.CURRENT_WEEK,
                new Separator(),
                InvestmentDealPredicate.LAST_YEAR,
                InvestmentDealPredicate.LAST_QUARTER,
                InvestmentDealPredicate.LAST_MONTH,
                new Separator()
        );

        for (int i = InvestmentDealPredicate.JANUARY.ordinal(); i <= InvestmentDealPredicate.DECEMBER.ordinal(); i++) {
            filterChoice.getItems().add(InvestmentDealPredicate.values()[i]);
        }

        filterChoice.getItems().add(new Separator());
        filterYearsIndex = filterChoice.getItems().size();

        filterChoice.setOnAction(updateHandler);
        filterChoice.getSelectionModel().select(0);
    }

    public PredicateProperty<InvestmentDeal> predicateProperty() {
        return predicateProperty;
    }

    public void setFilterYears(Collection<InvestmentDeal> records) {
        filterChoice.setOnAction(_ -> {});

        // remove existing years
        if (filterYearsIndex < filterChoice.getItems().size()) {
            filterChoice.getItems().remove(filterYearsIndex, filterChoice.getItems().size());
        }

        // Add years from existing transactions
        records.stream()
                .map(deal -> deal.getDate().getYear())
                .distinct()
                .sorted()
                .forEach(filterChoice.getItems()::add);

        filterChoice.setOnAction(updateHandler);
    }

    public Predicate<InvestmentDeal> getDealFilter() {
        if (periodRadio.isSelected()) {
            var selected = filterChoice.getSelectionModel().getSelectedItem();
            if (selected instanceof InvestmentDealPredicate investmentDealPredicate) {
                return investmentDealPredicate;
            } else if (selected instanceof Integer) {
                return byYear((int) selected);
            } else
                throw new IllegalStateException("Unexpected filter selection");
        } else {
            return byDates(fromPicker.getValue(), toPicker.getValue());
        }
    }

    public void setDealFilter(InvestmentDealPredicate filter) {
        periodRadio.setSelected(true);
        filterChoice.getSelectionModel().select(filter);
        predicateProperty.set(getDealFilter());
    }

    public void reset() {
        setDealFilter(InvestmentDealPredicate.CURRENT_YEAR);
    }
}
