/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.statements;

import java.math.BigDecimal;
import java.util.List;

public record Statement(StatementType type, String accountNumber, List<StatementRecord> records, BigDecimal balance) {
    public enum StatementType {
        UNKNOWN,
        RAIFFEISEN_OFX,
        SBERBANK_HTML
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
