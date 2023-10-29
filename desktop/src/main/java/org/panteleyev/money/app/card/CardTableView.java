/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
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

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class CardTableView extends TableView<Card> {
    public CardTableView(SortedList<Card> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                tableObjectColumn("Номер", b ->
                        b.withCellFactory(x -> new CardNumberCell()).withWidthBinding(w.multiply(0.2))
                                .withComparator(Comparator.comparing(Card::number))),
                tableObjectColumn("Категория", b ->
                        b.withCellFactory(x -> new CardCategoryCell()).withWidthBinding(w.multiply(0.1))
                                .withComparator(Comparators.cardsByCategory(cache()))),
                tableObjectColumn("Счёт", b ->
                        b.withCellFactory(x -> new CardAccountCell()).withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("До", b ->
                        b.withCellFactory(x -> new CardExpirationDateCell(settings().getAccountClosingDayDelta()))
                                .withWidthBinding(w.multiply(0.1))
                                .withComparator(Comparator.comparing(Card::expiration))),
                tableColumn("Комментарий", b ->
                        b.withPropertyCallback(Card::comment).withWidthBinding(w.multiply(0.4)))
        ));

        list.comparatorProperty().bind(comparatorProperty());
    }
}
