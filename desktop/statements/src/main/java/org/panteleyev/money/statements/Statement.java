/*
 Copyright © 2017-2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import java.math.BigDecimal;
import java.util.List;

public record Statement(StatementType type, String accountNumber, List<StatementRecord> records, BigDecimal balance) {
    public Statement {
        if (accountNumber == null) {
            accountNumber = "";
        }
    }

    public Statement(StatementType type, String accountNumber, List<StatementRecord> records) {
        this(type, accountNumber, records, BigDecimal.ZERO);
    }
}
