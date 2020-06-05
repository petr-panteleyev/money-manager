/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.statements;

import java.math.BigDecimal;
import java.util.List;

public record Statement(StatementType type, String accountNumber, List<StatementRecord>records, BigDecimal balance) {
    public enum StatementType {
        UNKNOWN,
        RAIFFEISEN_OFX,
        SBERBANK_HTML,
        YANDEX_MONEY,
        YANDEX_MONEY_CSV,
        ALFA_BANK_CSV
    }

    public Statement {
        if (accountNumber == null) {
            accountNumber = "";
        }
    }

    public Statement(StatementType type, String accountNumber, List<StatementRecord> records) {
        this(type, accountNumber, records, BigDecimal.ZERO);
    }
}
