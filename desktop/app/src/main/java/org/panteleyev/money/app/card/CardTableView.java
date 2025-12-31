// Copyright © 2023-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.card;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.cells.CardAccountCell;
import org.panteleyev.money.app.cells.CardCategoryCell;
import org.panteleyev.money.app.cells.CardExpirationDateCell;
import org.panteleyev.money.app.cells.CardNumberCell;
import org.panteleyev.money.model.Card;

import java.util.Comparator;
import java.util.List;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableObjectColumn;
import static org.panteleyev.fx.factories.TableFactory.tableStringColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class CardTableView extends TableView<Card> {
    public CardTableView(SortedList<Card> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                apply(tableObjectColumn("Номер"), c -> {
                    c.setCellFactory(_ -> new CardNumberCell());
                    c.widthBinding(w.multiply(0.2));
                    c.comparator(Comparator.comparing(Card::number));
                }),
                apply(tableObjectColumn("Категория"), c -> {
                    c.setCellFactory(_ -> new CardCategoryCell());
                    c.widthBinding(w.multiply(0.1));
                    c.comparator(Comparators.cardsByCategory(cache()));
                }),
                apply(tableObjectColumn("Счёт"), c -> {
                    c.setCellFactory(_ -> new CardAccountCell());
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableObjectColumn("До"), c -> {
                    c.setCellFactory(_ -> new CardExpirationDateCell(settings().getAccountClosingDayDelta()));
                    c.widthBinding(w.multiply(0.1));
                    c.comparator(Comparator.comparing(Card::expiration));
                }),
                apply(tableStringColumn("Комментарий"), c -> {
                    c.valueConverter(Card::comment);
                    c.widthBinding(w.multiply(0.4));
                })
        ));

        list.comparatorProperty().bind(comparatorProperty());
    }
}
