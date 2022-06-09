/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
