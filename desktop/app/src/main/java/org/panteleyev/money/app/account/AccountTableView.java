// Copyright © 2023-2026 Petr Panteleyev
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

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;

final class AccountTableView extends TableView<Account> {
    public AccountTableView(SortedList<Account> list) {
        super(list);

        var w = widthProperty().subtract(20);

        var nameColumn = TableFactory.<Account>tableObjectColumn("Название");
        nameColumn.setCellFactory(_ -> new AccountNameCell());
        nameColumn.comparator(Comparators.accountsByName());
        nameColumn.widthBinding(w.multiply(0.15));

        var categoryColumn = TableFactory.<Account>tableObjectColumn("Категория");
        categoryColumn.setCellFactory(_ -> new AccountCategoryCell());
        categoryColumn.comparator(Comparators.accountsByCategory(cache()));
        categoryColumn.widthBinding(w.multiply(0.2));

        var currencyColumn = TableFactory.<Account>tableObjectColumn("Валюта");
        currencyColumn.setCellFactory(_ -> new AccountCurrencyCell());
        currencyColumn.widthBinding(w.multiply(0.05));

        var interestColumn = TableFactory.<Account, BigDecimal>tableValueColumn("%%");
        interestColumn.setCellFactory(_ -> new AccountInterestCell());
        interestColumn.valueConverter(Account::interest);
        interestColumn.widthBinding(w.multiply(0.03));

        var closingDateColumn = TableFactory.<Account>tableObjectColumn("До");
        closingDateColumn.setCellFactory(_ -> new AccountClosingDateCell(settings().getAccountClosingDayDelta()));
        closingDateColumn.comparator(Comparators.accountsByClosingDate());
        closingDateColumn.widthBinding(w.multiply(0.05));

        var commentColumn = TableFactory.<Account>tableObjectColumn("Комментарий");
        commentColumn.setCellFactory(_ -> new AccountCommentCell());
        commentColumn.widthBinding(w.multiply(0.3));

        var balanceColumn = TableFactory.<Account>tableObjectColumn("Баланс");
        balanceColumn.setCellFactory(_ -> new AccountBalanceCell(true));
        balanceColumn.widthBinding(w.multiply(0.11));

        var waitingColumn = TableFactory.<Account>tableObjectColumn("Ожидает");
        waitingColumn.setCellFactory(_ -> new AccountBalanceCell(false));
        waitingColumn.widthBinding(w.multiply(0.11));

        getColumns().setAll(List.of(nameColumn, categoryColumn, currencyColumn, interestColumn,
                closingDateColumn, commentColumn, balanceColumn, waitingColumn));

        list.comparatorProperty().bind(comparatorProperty());

        getSortOrder().addAll(List.of(categoryColumn, nameColumn));
        sort();
    }
}
