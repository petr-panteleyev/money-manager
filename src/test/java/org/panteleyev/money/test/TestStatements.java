/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementParser;
import org.panteleyev.money.statements.StatementRecord;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

public class TestStatements {
    private static final String RESOURCES = "src/test/resources/org/panteleyev/money/test/TestStatements/";

    @Test
    public void testRaiffeisenCardStatement() throws Exception {
        Statement expected = new Statement(Statement.StatementType.RAIFFEISEN_CREDIT_CARD_CSV,
                List.of(
                        new StatementRecord(
                                LocalDate.of(2017, 9, 7),
                                LocalDate.of(2017, 9, 9),
                                "STARBUCKS  COFFEE HOUS SANKT-PETERSB RUS",
                                "STARBUCKS  COFFEE HOUS",
                                "SANKT-PETERSB",
                                "RUS",
                                "RUB",
                                "-1234.00",
                                "RUB",
                                "-1234.00"
                        ),
                        new StatementRecord(
                                LocalDate.of(2017, 9, 6),
                                LocalDate.of(2017, 9, 8),
                                "JAZZ AUSTIN TX",
                                "6TH STREET AUSTIN TX",
                                "AUSTIN",
                                "US",
                                "USD",
                                "-200.00",
                                "USD",
                                "-200.00"
                        ),
                        new StatementRecord(
                                LocalDate.of(2017, 9, 6),
                                LocalDate.of(2017, 9, 9),
                                "GROCERIES 756 SANKT-PETERBU RUS",
                                "GROCERIES 756",
                                "SANKT-PETERBU",
                                "RUS",
                                "RUB",
                                "-645.00",
                                "RUB",
                                "-645.00"
                        ),
                        new StatementRecord(
                                LocalDate.of(2017, 9, 5),
                                LocalDate.of(2017, 9, 8),
                                "IMAGINARY FOOD PARIS FR",
                                "IMAGINARY FOOD",
                                "PARIS",
                                "FR",
                                "EUR",
                                "-200.56",
                                "EUR",
                                "-200.56"
                        ),
                        new StatementRecord(
                                LocalDate.of(2017, 9, 4),
                                LocalDate.of(2017, 9, 7),
                                "IMAGINARY FOOD PARIS FR",
                                "IMAGINARY FOOD",
                                "PARIS",
                                "FR",
                                "EUR",
                                "-200.00",
                                "EUR",
                                "-200.00"
                        )
                )
        );

        File file = new File(RESOURCES + "rba_credit_card_statement.csv");
        try (InputStream inputStream = new FileInputStream(file)) {
            Statement actual = StatementParser.parse(Statement.StatementType.RAIFFEISEN_CREDIT_CARD_CSV, inputStream);
            Assert.assertEquals(actual, expected);
        }
    }

    @Test
    public void testRaiffeisenAccountStatement() throws Exception {
        Statement expected = new Statement(Statement.StatementType.RAIFFEISEN_ACCOUNT_CSV,
                List.of(
                        new StatementRecord(
                                LocalDate.of(1998, 1, 1),
                                LocalDate.of(1998, 1, 1),
                                "Interest",
                                "",
                                "",
                                "",
                                "RUB",
                                "10.12",
                                "RUB",
                                "10.12"
                        ),
                        new StatementRecord(
                                LocalDate.of(1998, 1, 3),
                                LocalDate.of(1998, 1, 3),
                                "Transfer",
                                "",
                                "",
                                "",
                                "RUB",
                                "-12456.67",
                                "RUB",
                                "-12456.67"
                        ),
                        new StatementRecord(
                                LocalDate.of(1998, 1, 10),
                                LocalDate.of(1998, 1, 10),
                                "SALARY",
                                "",
                                "",
                                "",
                                "RUB",
                                "1000.00",
                                "RUB",
                                "1000.00"
                        )
                )
        );

        File file = new File(RESOURCES + "rba_account_statement.csv");
        try (InputStream inputStream = new FileInputStream(file)) {
            Statement actual = StatementParser.parse(Statement.StatementType.RAIFFEISEN_ACCOUNT_CSV, inputStream);
            Assert.assertEquals(actual, expected);
        }
    }
}
