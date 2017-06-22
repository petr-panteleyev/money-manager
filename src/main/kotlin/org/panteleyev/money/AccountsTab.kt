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

package org.panteleyev.money

import javafx.application.Platform
import javafx.collections.MapChangeListener
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionFilter
import java.util.function.Predicate

class AccountsTab : BorderPane() {
    companion object {
        private const val DIVIDER_POSITION = 0.85
    }

    private val transactionTable = TransactionTableView(true)
    private var selectedAccount: Account? = null
    private var transactionFilter = TransactionFilter.ALL.predicate
    private val transactionListener = MapChangeListener<Int,Transaction> {
        Platform.runLater { this.reloadTransactions() }
    }

    init {
        val accountTree = AccountTree()

        center = SplitPane(accountTree, BorderPane(transactionTable)).apply {
            orientation = Orientation.VERTICAL
            setDividerPosition(0, DIVIDER_POSITION)
        }

        accountTree.setOnAccountSelected({ onAccountSelected(it) })
        accountTree.setOnTransactionFilterSelected({ onTransactionFilterSelected(it) })
        transactionTable.setOnCheckTransaction({ transactions, check -> onCheckTransaction(transactions, check) })

        MoneyDAO.transactions().addListener(transactionListener)
        MoneyDAO.preloadingProperty().addListener { _, _, newValue ->
            if (!newValue) {
                Platform.runLater { reloadTransactions() }
            }
        }
    }

    private fun onTransactionFilterSelected(filter: Predicate<Transaction>) {
        reloadTransactions(filter)
    }

    private fun onAccountSelected(account: Account?) {
        selectedAccount = account
        reloadTransactions()
    }

    private fun onCheckTransaction(transactions: List<Transaction>, check: Boolean?) {
        transactions.forEach { t ->
            MoneyDAO.updateTransaction(Transaction.Builder(t)
                    .checked(check!!)
                    .build())
        }

        reloadTransactions()
    }

    private fun reloadTransactions() {
        reloadTransactions(transactionFilter)
    }

    private fun reloadTransactions(f: Predicate<Transaction>) {
        var filter = f
        transactionFilter = filter

        if (selectedAccount != null) {
            filter = filter.and { t -> t.accountDebitedId == selectedAccount!!.id || t.accountCreditedId == selectedAccount!!.id }
        } else {
            filter = Predicate<Transaction> { false }
        }

        transactionTable.setTransactionFilter(filter)
    }
}