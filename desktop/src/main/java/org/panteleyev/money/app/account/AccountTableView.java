/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.TableColumnBuilder;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.account.cells.AccountBalanceCell;
import org.panteleyev.money.app.account.cells.AccountCategoryCell;
import org.panteleyev.money.app.account.cells.AccountClosingDateCell;
import org.panteleyev.money.app.account.cells.AccountCommentCell;
import org.panteleyev.money.app.account.cells.AccountCurrencyCell;
import org.panteleyev.money.app.account.cells.AccountInterestCell;
import org.panteleyev.money.app.account.cells.AccountNameCell;
import org.panteleyev.money.app.cells.DocumentCountCell;
import org.panteleyev.money.model.Account;

import java.math.BigDecimal;
import java.util.List;

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class AccountTableView extends TableView<Account> {
    private static final int NAME_COLUMN_INDEX = 0;
    private static final int CATEGORY_COLUMN_INDEX = 1;

    public AccountTableView(SortedList<Account> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                tableObjectColumn("Название", b ->
                        b.withCellFactory(x -> new AccountNameCell())
                                .withComparator(Comparators.accountsByName())
                                .withWidthBinding(w.multiply(0.15))),
                tableObjectColumn("Категория", b ->
                        b.withCellFactory(x -> new AccountCategoryCell())
                                .withComparator(Comparators.accountsByCategory(cache()))
                                .withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("Валюта", b ->
                        b.withCellFactory(x -> new AccountCurrencyCell()).withWidthBinding(w.multiply(0.05))),
                tableColumn("%%", (TableColumnBuilder<Account, BigDecimal> b) ->
                        b.withCellFactory(x -> new AccountInterestCell())
                                .withPropertyCallback(Account::interest)
                                .withWidthBinding(w.multiply(0.03))),
                tableObjectColumn("До", b ->
                        b.withCellFactory(x -> new AccountClosingDateCell(settings().getAccountClosingDayDelta()))
                                .withComparator(Comparators.accountsByClosingDate())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Комментарий", b ->
                        b.withCellFactory(x -> new AccountCommentCell()).withWidthBinding(w.multiply(0.29))),
                tableObjectColumn("Баланс", b ->
                        b.withCellFactory(x -> new AccountBalanceCell(true)).withWidthBinding(w.multiply(0.1))),
                tableObjectColumn("Ожидает", b ->
                        b.withCellFactory(x -> new AccountBalanceCell(false)).withWidthBinding(w.multiply(0.1))),
                tableObjectColumn("", b ->
                        b.withCellFactory(x -> new DocumentCountCell<>()).withWidthBinding(w.multiply(0.03)))
        ));

        list.comparatorProperty().bind(comparatorProperty());

        getSortOrder().addAll(List.of(
                getColumns().get(CATEGORY_COLUMN_INDEX),
                getColumns().get(NAME_COLUMN_INDEX)
        ));
        sort();
    }
}
