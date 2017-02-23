/*
 *  Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice,
 *        this list of conditions and the following disclaimer in the documentation
 *        and/or other materials provided with the distribution.
 *     3. The name of the author may not be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.panteleyev.money;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionFilter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AccountsTab extends BorderPane {
    private final AccountTree accountTree = (AccountTree)new AccountTree().load();
    private final TransactionTableView transactionTable = new TransactionTableView(true);
    private final SplitPane split = new SplitPane(accountTree.getPane(), new BorderPane(transactionTable));

    private final SimpleBooleanProperty preloadingProperty = new SimpleBooleanProperty();
    private final SimpleMapProperty<Integer, Transaction> transactionsProperty =
            new SimpleMapProperty<>();

    private Account selectedAccount = null;

    private Predicate<Transaction> transactionFilter = TransactionFilter.ALL.getPredicate();

    public AccountsTab() {
        split.setOrientation(Orientation.VERTICAL);
        setCenter(split);

        accountTree.setOnAccountSelected(this::onAccountSelected);
        accountTree.setOnTransactionFilterSelected(this::onTransactionFilterSelected);
        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        MoneyDAO dao = MoneyDAO.getInstance();

        preloadingProperty.bind(dao.preloadingProperty());
        transactionsProperty.bind(dao.transactionsProperty());

        transactionsProperty.addListener((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(this::reloadTransactions);
            }
        });

        preloadingProperty.addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                Platform.runLater(this::reloadTransactions);
            }
        });
    }

    private void onTransactionFilterSelected(Predicate<Transaction> filter) {
        reloadTransactions(filter);
    }

    private void onAccountSelected(Account account) {
        // TODO: make amount of transactions an option

        selectedAccount = account;
        reloadTransactions();
    }

    private void onCheckTransaction(List<Transaction> transactions, Boolean check) {
        MoneyDAO dao = MoneyDAO.getInstance();

        transactions.forEach(t -> {
            dao.updateTransaction(new Transaction.Builder(t)
                    .checked(check)
                    .build());
        });

        reloadTransactions();
    }

    private void reloadTransactions() {
        reloadTransactions(transactionFilter);
    }

    private void reloadTransactions(Predicate<Transaction> filter) {
        this.transactionFilter = filter;

        transactionTable.clear();

        if (selectedAccount != null) {
            List<Transaction> transactions = MoneyDAO.getInstance()
                    .getTransactions(selectedAccount)
                    .stream()
                    .filter(filter)
                    .collect(Collectors.toList());

            transactionTable.addRecords(transactions);
            transactionTable.sort();
        }
    }
}
