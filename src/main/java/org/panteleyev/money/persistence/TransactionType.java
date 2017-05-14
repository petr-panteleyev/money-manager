/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
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
package org.panteleyev.money.persistence;

import java.util.Arrays;
import java.util.ResourceBundle;

public enum TransactionType implements Named, Comparable<TransactionType> {
    CARD_PAYMENT(1),
    CASH_PURCHASE(2),
    CHEQUE(3),
    S1,
    WITHDRAWAL(5),
    CACHIER(6),
    DEPOSIT(7),
    TRANSFER(8),
    S2,
    INTEREST(10),
    DIVIDEND(11),
    S3,
    DIRECT_BILLING(13),
    CHARGE(14),
    FEE(15),
    S4,
    INCOME(17),
    SALE(18),
    S5,
    REFUND(20),
    UNDEFINED(21);

    private static final String BUNDLE = "org.panteleyev.money.persistence.TransactionType";

    private final int id;
    private final String name;
    private final boolean separator;

    TransactionType() {
        id = 0;
        name = null;
        separator = true;
    }

    TransactionType(int id) {
        ResourceBundle b = ResourceBundle.getBundle(BUNDLE);

        this.id = id;
        this.name = b.getString("name" + id);
        this.separator = false;
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isSeparator() {
        return separator;
    }

    public static TransactionType get(int id) {
        return Arrays.stream(values())
                .filter(v -> v.getId() == id)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
