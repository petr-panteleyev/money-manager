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
package org.panteleyev.money.cells

import javafx.geometry.Pos
import javafx.scene.control.TreeTableCell
import org.panteleyev.money.AccountTreeItem
import org.panteleyev.money.Styles.RED_TEXT
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.Transaction
import java.math.BigDecimal
import java.util.function.Predicate

class AccountBalanceCell(val total : Boolean, val filter : Predicate<Transaction>) : TreeTableCell<AccountTreeItem, Account>() {

    constructor(filter : Predicate<Transaction>) : this(false, filter)

    override fun updateItem(account: Account?, empty: Boolean) {
        super.updateItem(account, empty)
        alignment = Pos.CENTER_RIGHT

        if (empty || account == null) {
            text = ""
        } else {
            val sum = MoneyDAO.getTransactions(account)
                    .filter { filter.test(it) }
                    .map { t ->
                        var amount = t.amount
                        if (account.id == t.accountCreditedId) {
                            // handle conversion rate
                            val rate = t.rate
                            if (rate.compareTo(BigDecimal.ZERO) != 0) {
                                if (t.rateDirection == 0) {
                                    amount = amount.divide(rate, BigDecimal.ROUND_HALF_UP)
                                } else {
                                    amount = amount.multiply(rate)
                                }
                            }
                        } else {
                            amount = amount.negate()
                        }
                        amount
                    }
                    .fold(BigDecimal.ZERO) { acc, next -> acc.add(next) }

            if (total) {
                sum.add(account.openingBalance)
            }

            text = sum.setScale(2, BigDecimal.ROUND_HALF_UP).toString()

            styleClass.remove(RED_TEXT)
            if (sum.signum() < 0) {
                styleClass.add(RED_TEXT)
            }
        }
    }
}