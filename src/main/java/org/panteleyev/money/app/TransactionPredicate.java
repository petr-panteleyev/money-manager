/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import static org.panteleyev.money.app.Bundles.TRANSACTION_PREDICATE_BUNDLE;

public enum TransactionPredicate implements Predicate<Transaction> {
    ALL(it -> true),

    CURRENT_YEAR(it -> it.year() == LocalDate.now().getYear()),

    CURRENT_MONTH(it -> {
        var now = LocalDate.now();
        return it.year() == now.getYear() && it.month() == now.getMonthValue();
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
            return t.year() == now.getYear() && t.month() == month.getValue();
        };

        description = month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    @Override
    public String toString() {
        return description;
    }

    public static Predicate<Transaction> transactionByAccount(UUID uuid) {
        return it -> Objects.equals(it.accountDebitedUuid(), uuid)
            || Objects.equals(it.accountCreditedUuid(), uuid);
    }

    public static Predicate<Transaction> transactionByCategory(UUID uuid) {
        return it -> Objects.equals(it.accountDebitedCategoryUuid(), uuid)
            || Objects.equals(it.accountCreditedCategoryUuid(), uuid);
    }

    public static Predicate<Transaction> transactionByCategoryType(CategoryType type) {
        return it -> it.accountDebitedType() == type || it.accountCreditedType() == type;
    }

    public static Predicate<Transaction> transactionByYear(int year) {
        return it -> it.year() == year;
    }

    public static Predicate<Transaction> transactionByDates(LocalDate from, LocalDate to) {
        return it -> checkRange(it, from, to);
    }

    private static boolean checkRange(Transaction t, LocalDate from, LocalDate to) {
        var date = LocalDate.of(t.year(), t.month(), t.day());
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
