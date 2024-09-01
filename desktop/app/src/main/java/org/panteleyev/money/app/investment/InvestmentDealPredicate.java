/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.panteleyev.money.model.investment.InvestmentDeal;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import static org.panteleyev.money.app.Bundles.translate;

public enum InvestmentDealPredicate implements Predicate<InvestmentDeal> {
    ALL(_ -> true),

    CURRENT_YEAR(it -> it.getDate().getYear() == LocalDate.now().getYear()),

    CURRENT_MONTH(it -> {
        var now = LocalDate.now();
        return it.getDate().getYear() == now.getYear()
                && it.getDate().getMonthValue() == now.getMonthValue();
    }),

    CURRENT_WEEK(it -> {
        var now = LocalDate.now();
        var from = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return checkRange(it, from, now);
    }),

    LAST_YEAR(it -> {
        var now = LocalDate.now();
        var from = now.minusYears(1);
        return checkRange(it, from, now);
    }),

    LAST_QUARTER(it -> {
        var now = LocalDate.now();
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

    private final Predicate<InvestmentDeal> predicate;
    private final String description;

    InvestmentDealPredicate(Predicate<InvestmentDeal> p) {
        predicate = p;
        description = translate(this);
    }

    InvestmentDealPredicate(Month month) {
        predicate = (t) -> {
            var now = LocalDate.now();
            return t.getDate().getYear() == now.getYear()
                    && t.getDate().getMonthValue() == month.getValue();
        };

        description = month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    @Override
    public String toString() {
        return description;
    }

    public static Predicate<InvestmentDeal> byYear(int year) {
        return it -> it.getDate().getYear() == year;
    }

    public static Predicate<InvestmentDeal> byDates(LocalDate from, LocalDate to) {
        return it -> checkRange(it, from, to);
    }

    public static Predicate<InvestmentDeal> byExchangeSecurity(UUID uuid) {
        return it -> Objects.equals(it.securityUuid(), uuid);
    }

    private static boolean checkRange(InvestmentDeal deal, LocalDate from, LocalDate to) {
        return !deal.getDate().isBefore(from)
                && !deal.getDate().isAfter(to);
    }

    @Override
    public boolean test(InvestmentDeal deal) {
        return predicate.test(deal);
    }

    @Override
    public Predicate<InvestmentDeal> and(Predicate<? super InvestmentDeal> other) {
        return predicate.and(other);
    }

    @Override
    public Predicate<InvestmentDeal> negate() {
        return predicate.negate();
    }

    @Override
    public Predicate<InvestmentDeal> or(Predicate<? super InvestmentDeal> other) {
        return predicate.or(other);
    }
}
