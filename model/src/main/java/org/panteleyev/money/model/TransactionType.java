/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public enum TransactionType {
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

    private final int id;
    private final boolean separator;
    private final String typeName;

    TransactionType(int id) {
        this(id, false);
    }

    TransactionType(int id, boolean separator) {
        this.id = id;
        this.separator = separator;

        if (separator) {
            typeName = "";
        } else {
            var bundle = ResourceBundle.getBundle("org.panteleyev.money.model.TransactionType");
            typeName = bundle.getString("name" + id);
        }
    }

    public int getId() {
        return id;
    }

    public boolean isSeparator() {
        return separator;
    }

    public String getTypeName() {
        return typeName;
    }

    public static TransactionType get(int id) {
        return Arrays.stream(TransactionType.values())
                .filter(t -> t.id == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public static List<TransactionType> valuesAsList() {
        return List.of(TransactionType.values());
    }
}
