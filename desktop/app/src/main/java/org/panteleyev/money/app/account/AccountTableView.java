// Copyright © 2023-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.account;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.account.cells.AccountBalanceCell;
import org.panteleyev.money.app.account.cells.AccountCategoryCell;
import org.panteleyev.money.app.account.cells.AccountClosingDateCell;
import org.panteleyev.money.app.account.cells.AccountCommentCell;
import org.panteleyev.money.app.account.cells.AccountCurrencyCell;
import org.panteleyev.money.app.account.cells.AccountInterestCell;
import org.panteleyev.money.app.account.cells.AccountNameCell;
import org.panteleyev.money.model.Account;

import java.math.BigDecimal;
import java.util.List;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class AccountTableView extends TableView<Account> {
    private static final int NAME_COLUMN_INDEX = 0;
    private static final int CATEGORY_COLUMN_INDEX = 1;

    public AccountTableView(SortedList<Account> list) {
        super(list);

        var w = widthProperty().subtract(20);

        getColumns().setAll(List.of(
                apply(tableObjectColumn("Название"), c -> {
                    c.setCellFactory(_ -> new AccountNameCell());
                    c.comparator(Comparators.accountsByName());
                    c.widthBinding(w.multiply(0.15));
                }),
                apply(tableObjectColumn("Категория"), c -> {
                    c.setCellFactory(_ -> new AccountCategoryCell());
                    c.comparator(Comparators.accountsByCategory(cache()));
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableObjectColumn("Валюта"), c -> {
                    c.setCellFactory(_ -> new AccountCurrencyCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(TableFactory.<Account, BigDecimal>tableValueColumn("%%"), c -> {
                    c.setCellFactory(_ -> new AccountInterestCell());
                    c.valueConverter(Account::interest);
                    c.widthBinding(w.multiply(0.03));
                }),
                apply(tableObjectColumn("До"), c -> {
                    c.setCellFactory(_ -> new AccountClosingDateCell(settings().getAccountClosingDayDelta()));
                    c.comparator(Comparators.accountsByClosingDate());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Комментарий"), c -> {
                    c.setCellFactory(_ -> new AccountCommentCell());
                    c.widthBinding(w.multiply(0.3));
                }),
                apply(tableObjectColumn("Баланс"), c -> {
                    c.setCellFactory(_ -> new AccountBalanceCell(true));
                    c.widthBinding(w.multiply(0.11));
                }),
                apply(tableObjectColumn("Ожидает"), c -> {
                    c.setCellFactory(_ -> new AccountBalanceCell(false));
                    c.widthBinding(w.multiply(0.11));
                })
        ));

        list.comparatorProperty().bind(comparatorProperty());

        getSortOrder().addAll(List.of(
                getColumns().get(CATEGORY_COLUMN_INDEX),
                getColumns().get(NAME_COLUMN_INDEX)
        ));
        sort();
    }
}
