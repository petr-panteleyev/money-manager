/*
 * Copyright (c) 2019, 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money;

import org.panteleyev.money.model.Transaction;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import static org.panteleyev.money.Bundles.TRANSACTION_PREDICATE_BUNDLE;

public enum TransactionPredicate implements Predicate<Transaction> {
    ALL(it -> true),

    CURRENT_YEAR(it -> it.getYear() == LocalDate.now().getYear()),

    CURRENT_MONTH(it -> {
        var now = LocalDate.now();
        return it.getYear() == now.getYear() && it.getMonth() == now.getMonthValue();
    }),

    CURRENT_WEEK(it -> {
        var now = LocalDate.now();
        var from = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return checkRange(it, from, now);
    }),

    LAST_YEAR(it -> {
        var now = java.time.LocalDate.now();
        var from = now.minusYears(1);
        return checkRange(it, from, now);
    }),

    LAST_QUARTER(it -> {
        var now = java.time.LocalDate.now();
        var from = now.minusMonths(3);
        return checkRange(it, from, now);
    }),

    LAST_MONTH(it -> {
        var now = LocalDate.now();
        var from = now.minusMonths(1);
        return checkRange(it, from, now);
    }),

    JANUARY(Month.JANUARY),
    FEBRUARY(Month.FEBRUARY),
    MARCH(Month.MARCH),
    APRIL(Month.APRIL),
    MAY(Month.MAY),
    JUNE(Month.JUNE),
    JULY(Month.JULY),
    AUGUST(Month.AUGUST),
    SEPTEMBER(Month.SEPTEMBER),
    OCTOBER(Month.OCTOBER),
    NOVEMBER(Month.NOVEMBER),
    DECEMBER(Month.DECEMBER);

    private final Predicate<Transaction> predicate;
    private final String description;

    TransactionPredicate(Predicate<Transaction> p) {
        predicate = p;
        description = TRANSACTION_PREDICATE_BUNDLE.getString(name());
    }

    TransactionPredicate(Month month) {
        predicate = (t) -> {
            var now = LocalDate.now();
            return t.getYear() == now.getYear() && t.getMonth() == month.getValue();
        };

        description = month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    @Override
    public String toString() {
        return description;
    }

    public static Predicate<Transaction> transactionByAccount(UUID uuid) {
        return it -> Objects.equals(it.getAccountDebitedUuid(), uuid)
            || Objects.equals(it.getAccountCreditedUuid(), uuid);
    }

    public static Predicate<Transaction> transactionByCategory(UUID uuid) {
        return it -> Objects.equals(it.getAccountDebitedCategoryUuid(), uuid)
            || Objects.equals(it.getAccountCreditedCategoryUuid(), uuid);
    }

    public static Predicate<Transaction> transactionByCategoryType(int id) {
        return it -> it.getAccountDebitedTypeId() == id || it.getAccountCreditedTypeId() == id;
    }

    public static Predicate<Transaction> transactionByYear(int year) {
        return it -> it.getYear() == year;
    }

    public static Predicate<Transaction> transactionByDates(LocalDate from, LocalDate to) {
        return it -> checkRange(it, from, to);
    }

    private static boolean checkRange(Transaction t, LocalDate from, LocalDate to) {
        var date = LocalDate.of(t.getYear(), t.getMonth(), t.getDay());
        return date.compareTo(from) >= 0 && date.compareTo(to) <= 0;
    }

    @Override
    public boolean test(Transaction transaction) {
        return predicate.test(transaction);
    }

    @Override
    public Predicate<Transaction> and(Predicate<? super Transaction> other) {
        return predicate.and(other);
    }

    @Override
    public Predicate<Transaction> negate() {
        return predicate.negate();
    }

    @Override
    public Predicate<Transaction> or(Predicate<? super Transaction> other) {
        return predicate.or(other);
    }
}
