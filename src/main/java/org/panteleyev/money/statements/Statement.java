package org.panteleyev.money.statements;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class Statement {
    public enum StatementType {
        UNKNOWN,
        RAIFFEISEN_OFX,
        SBERBANK_HTML,
        YANDEX_MONEY,
        YANDEX_MONEY_CSV,
        ALFA_BANK_CSV
    }

    private final StatementType type;
    private final String accountNumber;
    private final List<StatementRecord> records;
    private final BigDecimal balance;

    public Statement(StatementType type, String accountNumber, List<StatementRecord> records) {
        this(type, accountNumber, records, BigDecimal.ZERO);
    }

    public Statement(StatementType type, String accountNumber, List<StatementRecord> records, BigDecimal balance) {
        this.type = type;
        this.accountNumber = accountNumber == null ? "" : accountNumber;
        this.records = records;
        this.balance = balance;
    }

    public StatementType getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
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
            && Objects.equals(accountNumber, that.accountNumber)
            && Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, accountNumber, records);
    }
}
