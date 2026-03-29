// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.card;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.cells.CardAccountCell;
import org.panteleyev.money.app.cells.CardCategoryCell;
import org.panteleyev.money.app.cells.CardExpirationDateCell;
import org.panteleyev.money.app.cells.CardNumberCell;
import org.panteleyev.money.model.Card;

import java.util.Comparator;
import java.util.List;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class CardTableView extends TableView<Card> {
    public CardTableView(SortedList<Card> list) {
        super(list);

        var w = widthProperty().subtract(20);

        var numberColumn = TableFactory.<Card>tableObjectColumn("Номер");
        numberColumn.setCellFactory(_ -> new CardNumberCell());
        numberColumn.widthBinding(w.multiply(0.2));
        numberColumn.comparator(Comparator.comparing(Card::number));
        var categoryColumn = TableFactory.<Card>tableObjectColumn("Категория");
        categoryColumn.setCellFactory(_ -> new CardCategoryCell());
        categoryColumn.widthBinding(w.multiply(0.1));
        categoryColumn.comparator(Comparators.cardsByCategory(cache()));
        var accountColumn = TableFactory.<Card>tableObjectColumn("Счёт");
        accountColumn.setCellFactory(_ -> new CardAccountCell());
        accountColumn.widthBinding(w.multiply(0.2));
        var untilColumn = TableFactory.<Card>tableObjectColumn("До");
        untilColumn.setCellFactory(_ -> new CardExpirationDateCell(settings().getAccountClosingDayDelta()));
        untilColumn.widthBinding(w.multiply(0.1));
        untilColumn.comparator(Comparator.comparing(Card::expiration));
        var commentColumn = TableFactory.<Card>tableStringColumn("Комментарий");
        commentColumn.valueConverter(Card::comment);
        commentColumn.widthBinding(w.multiply(0.4));

        getColumns().setAll(List.of(numberColumn, categoryColumn, accountColumn, untilColumn, commentColumn));
        list.comparatorProperty().bind(comparatorProperty());
    }
}
