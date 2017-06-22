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
import javafx.scene.control.TableCell
import org.panteleyev.money.Styles.BLACK_TEXT
import org.panteleyev.money.Styles.BLUE_TEXT
import org.panteleyev.money.Styles.RED_TEXT
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.SplitTransaction
import org.panteleyev.money.persistence.Transaction
import java.math.BigDecimal

class TransactionSumCell : TableCell<Transaction, Transaction>() {
    override fun updateItem(item: Transaction?, empty: Boolean) {
        super.updateItem(item, empty)
        this.alignment = Pos.CENTER_RIGHT
        if (empty || item == null) {
            text = ""
        } else {
            val amount = item.signedAmount

            styleClass.removeAll(RED_TEXT, BLUE_TEXT, BLACK_TEXT)

            if (item is SplitTransaction) {
                styleClass.add(if (amount.signum() < 0) RED_TEXT else BLACK_TEXT)
            } else {
                var s = BLACK_TEXT
                if (item.accountCreditedType != item.accountDebitedType) {
                    if (item.accountDebitedType == CategoryType.INCOMES) {
                        s = BLUE_TEXT
                    } else {
                        s = RED_TEXT
                    }
                }
                styleClass.add(s)
            }

            val format = if (item is SplitTransaction || item.groupId == 0)
                "%s"
            else
                "(%s)"
            text = String.format(format, amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString())
        }
    }
}