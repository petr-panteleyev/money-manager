/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.collections.MapChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionFilter;
import java.util.List;
import java.util.function.Predicate;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class AccountsTab extends BorderPane {
    private static final double DIVIDER_POSITION = 0.85;

    private TransactionTableView transactionTable = new TransactionTableView(true);
    private Account selectedAccount = null;
    private Predicate<Transaction> transactionFilter = TransactionFilter.ALL.getPredicate();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<Integer, Transaction> transactionListener =
            change -> Platform.runLater(this::reloadTransactions);

    AccountsTab() {
        AccountTree accountTree = new AccountTree();

        SplitPane pane = new SplitPane(accountTree, new BorderPane(transactionTable));
        pane.setOrientation(Orientation.VERTICAL);
        pane.setDividerPosition(0, DIVIDER_POSITION);
        setCenter(pane);

        accountTree.setOnAccountSelected(this::onAccountSelected);

        accountTree.setOnTransactionFilterSelected(this::onTransactionFilterSelected);
        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        getDao().transactions().addListener(transactionListener);
        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::reloadTransactions);
            }
        });
    }

    private void onTransactionFilterSelected(Predicate<Transaction> filter) {
        reloadTransactions(filter);
    }

    private void onAccountSelected(Account account) {
        selectedAccount = account;
        reloadTransactions();
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (Transaction t : transactions) {
            getDao().updateTransaction(t.check(check));
        }

        reloadTransactions();
    }

    private void reloadTransactions() {
        reloadTransactions(transactionFilter);
    }

    private void reloadTransactions(Predicate<Transaction> filter) {
        transactionFilter = filter;

        if (selectedAccount != null) {
            filter = filter.and(t -> t.getAccountDebitedId() == selectedAccount.getId() || t.getAccountCreditedId()
                    == selectedAccount.getId());
        } else {
            filter = t -> false;
        }

        transactionTable.setTransactionFilter(filter);
    }
}
