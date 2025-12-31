// Copyright © 2023-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.currency;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.money.model.Currency;

import java.util.List;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableStringColumn;

final class CurrencyTableView extends TableView<Currency> {
    public CurrencyTableView(SortedList<Currency> list) {
        super(list);

        var w = widthProperty().subtract(20);
        TableColumn<Currency, String> nameColumn = apply(tableStringColumn("Название"), c -> {
            c.valueConverter(Currency::symbol);
            c.comparator(String::compareTo);
            c.widthBinding(w.multiply(0.2));
        });

        getColumns().setAll(List.of(
                nameColumn,
                apply(tableStringColumn("Описание"), c -> {
                    c.valueConverter(Currency::description);
                    c.widthBinding(w.multiply(0.8));
                })
        ));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(nameColumn);
        sort();
    }
}
