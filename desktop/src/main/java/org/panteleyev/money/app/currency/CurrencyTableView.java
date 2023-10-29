/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.currency;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.money.model.Currency;

import java.util.List;

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;

final class CurrencyTableView extends TableView<Currency> {
    public CurrencyTableView(SortedList<Currency> list) {
        super(list);

        var w = widthProperty().subtract(20);
        TableColumn<Currency, String> nameColumn = tableColumn("Название", b ->
                b.withPropertyCallback(Currency::symbol)
                        .withComparator(String::compareTo)
                        .withWidthBinding(w.multiply(0.2)));

        getColumns().setAll(List.of(
                nameColumn,
                tableColumn("Описание", b ->
                        b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(nameColumn);
        sort();
    }
}
