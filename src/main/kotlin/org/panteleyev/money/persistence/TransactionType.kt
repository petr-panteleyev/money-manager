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

package org.panteleyev.money.persistence

import java.util.ResourceBundle

enum class TransactionType(val id : Int, val separator: Boolean = false) {
    CARD_PAYMENT(1),
    CASH_PURCHASE(2),
    CHEQUE(3),
    S1(4, true),
    WITHDRAWAL(5),
    CACHIER(6),
    DEPOSIT(7),
    TRANSFER(8),
    S2(9, true),
    INTEREST(10),
    DIVIDEND(11),
    S3(12, true),
    DIRECT_BILLING(13),
    CHARGE(14),
    FEE(15),
    S4(16, true),
    INCOME(17),
    SALE(18),
    S5(19, true),
    REFUND(20),
    UNDEFINED(21);

    val typeName : String

    init {
        if (separator) {
            typeName = ""
        } else {
            val bundle : ResourceBundle = ResourceBundle.getBundle("org.panteleyev.money.persistence.res.TransactionType")
            typeName = bundle.getString("name" + id)
        }
    }

    companion object {
        fun get(id : Int) : TransactionType {
            if (id == 0) {
                throw java.lang.IllegalArgumentException()
            }
            return TransactionType.values().find { it.id == id } ?: throw java.lang.IllegalArgumentException()
        }
    }
}