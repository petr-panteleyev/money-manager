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

package org.panteleyev.money.statements;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

class StatementPredicate implements Predicate<Transaction> {
    private final int accountId;
    private final StatementRecord record;
    private final boolean ignoreExecutionDate;

    StatementPredicate(Account account, StatementRecord record, boolean ignoreExecutionDate) {
        this.accountId = account.getId();
        this.record = record;
        this.ignoreExecutionDate = ignoreExecutionDate;
    }

    @Override
    public boolean test(Transaction transaction) {
        if (record == null || transaction == null) {
            return false;
        }

        boolean result = (transaction.getAccountDebitedId() == accountId
                || transaction.getAccountCreditedId() == accountId)
                && (compareDate(record.getActual(), transaction)
                || (!ignoreExecutionDate && compareDate(record.getExecution(), transaction)));

        result = result && (compareAmount(record.getAmountDecimal(), transaction)
                || compareAmount(record.getAccountAmountDecimal(), transaction));

        return result;
    }

    private boolean compareDate(LocalDate date, Transaction transaction) {
        return transaction.getDay() == date.getDayOfMonth()
                && transaction.getMonth() == date.getMonthValue()
                && transaction.getYear() == date.getYear();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean compareAmount(Optional<BigDecimal> amount, Transaction transaction) {
        return amount.map(BigDecimal::abs)
                .map(a -> a.compareTo(transaction.getAmount()) == 0)
                .orElse(false);
    }
}
