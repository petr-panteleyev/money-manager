/*
 * Copyright (c) 2018, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionFilter;
import java.time.LocalDate;
import java.util.function.Predicate;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TransactionFilterBox extends HBox {
    private ChoiceBox<Object> filterChoice = new ChoiceBox<>();

    private DatePicker fromPicker = new DatePicker(LocalDate.now());
    private DatePicker toPicker = new DatePicker(LocalDate.now());

    private final RadioButton periodRadio = new RadioButton();

    private final int filterYearsIndex;

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

            getChildren().addAll(rangeRadio, fromPicker, new Label(" - "), toPicker);

            filterChoice.disableProperty().bind(rangeRadio.selectedProperty());
            fromPicker.disableProperty().bind(periodRadio.selectedProperty());
            toPicker.disableProperty().bind(periodRadio.selectedProperty());

            periodRadio.setSelected(true);
        }

        // Init constant part of the list
        filterChoice.getItems().setAll(
            TransactionFilter.ALL,
            new Separator(),
            TransactionFilter.CURRENT_YEAR,
            TransactionFilter.CURRENT_MONTH,
            TransactionFilter.CURRENT_WEEK,
            new Separator(),
            TransactionFilter.LAST_YEAR,
            TransactionFilter.LAST_QUARTER,
            TransactionFilter.LAST_MONTH,
            new Separator()
        );

        for (int i = TransactionFilter.JANUARY.ordinal(); i <= TransactionFilter.DECEMBER.ordinal(); i++) {
            filterChoice.getItems().add(TransactionFilter.values()[i]);
        }

        filterChoice.getItems().add(new Separator());
        filterYearsIndex = filterChoice.getItems().size();

        filterChoice.getSelectionModel().select(0);

        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::setFilterYears);
            }
        });
    }

    private void setFilterYears() {
        // remove existing years
        if (filterYearsIndex < filterChoice.getItems().size()) {
            filterChoice.getItems().remove(filterYearsIndex, filterChoice.getItems().size());
        }

        // Add years from existing transactions
        cache().getTransactions().stream()
            .map(Transaction::getYear)
            .distinct()
            .sorted()
            .forEach(filterChoice.getItems()::add);
    }

    public Predicate<Transaction> getTransactionFilter() {
        if (periodRadio.isSelected()) {
            var selected = filterChoice.getSelectionModel().getSelectedItem();
            if (selected instanceof TransactionFilter) {
                return ((TransactionFilter) selected).predicate();
            } else if (selected instanceof Integer) {
                return TransactionFilter.byYear((int) selected);
            } else throw new IllegalStateException("Unexpected filter selection");
        } else {
            return TransactionFilter.byDates(fromPicker.getValue(), toPicker.getValue());
        }
    }
}
