// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.currency;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.model.Currency;

import java.util.List;

final class CurrencyTableView extends TableView<Currency> {
    public CurrencyTableView(SortedList<Currency> list) {
        super(list);

        var w = widthProperty().subtract(20);

        var nameColumn = TableFactory.<Currency>tableStringColumn("Название");
        nameColumn.valueConverter(Currency::symbol);
        nameColumn.comparator(String::compareTo);
        nameColumn.widthBinding(w.multiply(0.2));

        var descrColumn = TableFactory.<Currency>tableStringColumn("Описание");
        descrColumn.valueConverter(Currency::description);
        descrColumn.widthBinding(w.multiply(0.8));

        getColumns().setAll(List.of(nameColumn, descrColumn));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(nameColumn);
        sort();
    }
}
