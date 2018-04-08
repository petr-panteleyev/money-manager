/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.statements;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class Statement {
    public enum StatementType {
        UNKNOWN,
        RAIFFEISEN_CREDIT_CARD_CSV,
        RAIFFEISEN_ACCOUNT_CSV,
        RAIFFEISEN_CARD_OFX,
        SBERBANK_HTML,
        YANDEX_MONEY
    }

    private final StatementType type;
    private final List<StatementRecord> records;
    private final BigDecimal balance;

    public Statement(StatementType type, List<StatementRecord> records) {
        this(type, records, BigDecimal.ZERO);
    }

    public Statement(StatementType type, List<StatementRecord> records, BigDecimal balance) {
        this.type = type;
        this.records = records;
        this.balance = balance;
    }

    public StatementType getType() {
        return type;
    }

    public List<StatementRecord> getRecords() {
        return records;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Statement)) {
            return false;
        }

        var that = (Statement) o;
        return type == that.type
                && Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, records);
    }
}
