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

package org.panteleyev.money.persistence;

import org.panteleyev.money.Logging;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public enum TransactionFilter {
    ALL(it -> true),

    CURRENT_YEAR(it -> it.getYear() == LocalDate.now().getYear()),

    CURRENT_MONTH(it -> {
        LocalDate now = LocalDate.now();
        return it.getYear() == now.getYear() && it.getMonth() == now.getMonthValue();
    }),

    CURRENT_WEEK(it -> {
        LocalDate now = LocalDate.now();
        LocalDate from = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return checkRange(it, from, now);
    }),

    LAST_YEAR(it -> {
        LocalDate now = java.time.LocalDate.now();
        LocalDate from = now.minusYears(1);
        return checkRange(it, from, now);
    }),

    LAST_QUARTER(it -> {
        LocalDate now = java.time.LocalDate.now();
        LocalDate from = now.minusMonths(3);
        return checkRange(it, from, now);
    }),

    LAST_MONTH(it -> {
        LocalDate now = LocalDate.now();
        LocalDate from = now.minusMonths(1);
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

    TransactionFilter(Predicate<Transaction> p) {
        predicate = p;

        ResourceBundle bundle = ResourceBundle.getBundle("org.panteleyev.money.persistence.res.TransactionFilter");
        description = bundle.getString(name());
    }

    TransactionFilter(Month month) {
        predicate = (t) -> {
            LocalDate now = LocalDate.now();
            return t.getYear() == now.getYear() && t.getMonth() == month.getValue();
        };

        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8146356
        TextStyle textStyle = TextStyle.FULL_STANDALONE;
        String testMonth = Month.JANUARY.getDisplayName(textStyle, Locale.getDefault());
        if (testMonth.equals("1")) {
            textStyle = TextStyle.FULL;
        } else {
            Logging.getLogger().info("JDK-8146356 has been resolved");
        }

        description = month.getDisplayName(textStyle, Locale.getDefault());
    }

    public Predicate<Transaction> predicate() {
        return predicate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static Predicate<Transaction> byAccount(int id) {
        return it -> it.getAccountDebitedId() == id || it.getAccountCreditedId() == id;
    }

    public static Predicate<Transaction> byCategory(int id) {
        return it -> it.getAccountDebitedCategoryId() == id || it.getAccountCreditedCategoryId() == id;
    }

    public static Predicate<Transaction> byCategoryType(int id) {
        return it -> it.getAccountDebitedTypeId() == id || it.getAccountCreditedTypeId() == id;
    }

    public static Predicate<Transaction> byYear(int year) {
        return it -> it.getYear() == year;
    }

    public static Predicate<Transaction> byDates(LocalDate from, LocalDate to) {
        return it -> checkRange(it, from , to);
    }

    private static boolean checkRange(Transaction t, LocalDate from, LocalDate to) {
        LocalDate date = LocalDate.of(t.getYear(), t.getMonth(), t.getDay());
        return date.compareTo(from) >= 0 && date.compareTo(to) <= 0;
    }
}
