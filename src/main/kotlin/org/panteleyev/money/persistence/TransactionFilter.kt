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

package org.panteleyev.money.persistence

import org.panteleyev.money.Logging
import java.time.LocalDate
import java.util.function.Predicate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import java.util.ResourceBundle

enum class TransactionFilter {
    ALL(Predicate { _ -> true }),

    CURRENT_YEAR(Predicate { it.year == LocalDate.now().year }),

    CURRENT_MONTH(Predicate {
        val now = LocalDate.now()
        it.year == now.year && it.month == now.monthValue
    }),

    CURRENT_WEEK(Predicate {
        val now = LocalDate.now()
        val from = now.minusDays((now.dayOfWeek.value - 1).toLong())
        LocalDate.of(it.year, it.month, it.day) in from..now
    }),

    LAST_YEAR(Predicate {
        val now = java.time.LocalDate.now()
        val from = now.minusYears(1)
        LocalDate.of(it.year, it.month, it.day) in from..now
    }),

    LAST_QUARTER(Predicate {
        val now = java.time.LocalDate.now()
        val from = now.minusMonths(3)
        LocalDate.of(it.year, it.month, it.day) in from..now
    }),

    LAST_MONTH(Predicate {
        val now = LocalDate.now()
        val from = now.minusMonths(1)
        LocalDate.of(it.year, it.month, it.day) in from..now
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

    val predicate : Predicate<Transaction>
    val description : String

    constructor(p : Predicate<Transaction>) {
        predicate = p

        val bundle = ResourceBundle.getBundle("org.panteleyev.money.persistence.res.TransactionFilter")
        description = bundle.getString(name)
    }


    constructor(month : Month) {
        predicate = Predicate { t ->
            val now = LocalDate.now()
            t.year == now.year && t.month == month.value
        }

        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8146356
        var textStyle = TextStyle.FULL_STANDALONE
        val testMonth = Month.JANUARY.getDisplayName(textStyle, Locale.getDefault())
        if (testMonth == "1") {
            textStyle = TextStyle.FULL
        } else {
            Logging.logger.info("JDK-8146356 has been resolved")
        }

        description = month.getDisplayName(textStyle, Locale.getDefault())
    }

    override fun toString(): String = description

    companion object {
        fun byAccount(id: Int): Predicate<Transaction> = Predicate {
            it.accountDebitedId == id || it.accountCreditedId == id
        }

        fun byCategory(id: Int): Predicate<Transaction> = Predicate {
            it.accountDebitedCategoryId == id || it.accountCreditedCategoryId == id
        }

        fun byCategoryType(id: Int): Predicate<Transaction> = Predicate {
            it.accountDebitedTypeId == id || it.accountCreditedTypeId == id
        }
    }
}