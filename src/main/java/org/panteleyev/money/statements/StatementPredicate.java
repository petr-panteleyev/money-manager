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

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class StatementPredicate implements Predicate<Transaction> {
    private final UUID accountUuid;
    private final StatementRecord record;
    private final boolean ignoreExecutionDate;

    public StatementPredicate(Account account, StatementRecord record, boolean ignoreExecutionDate) {
        this.accountUuid = account == null ? null : account.uuid();
        this.record = record;
        this.ignoreExecutionDate = ignoreExecutionDate;
    }

    @Override
    public boolean test(Transaction transaction) {
        if (record == null || transaction == null) {
            return false;
        }

        var result = (Objects.equals(transaction.accountDebitedUuid(), accountUuid)
                || Objects.equals(transaction.accountCreditedUuid(), accountUuid))
                && (compareDate(record.getActual(), transaction)
                || (!ignoreExecutionDate && compareDate(record.getExecution(), transaction)));

        result = result && (compareAmount(record.getAmountDecimal(), transaction)
                || compareAmount(record.getAccountAmountDecimal(), transaction));

        return result;
    }

    private boolean compareDate(LocalDate date, Transaction transaction) {
        return (transaction.day() == date.getDayOfMonth()
                && transaction.month() == date.getMonthValue()
                && transaction.year() == date.getYear()
        ) || Objects.equals(transaction.statementDate(), date);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean compareAmount(Optional<BigDecimal> amount, Transaction transaction) {
        return amount.map(BigDecimal::abs)
                .map(a -> a.compareTo(transaction.amount()) == 0)
                .orElse(false);
    }
}
