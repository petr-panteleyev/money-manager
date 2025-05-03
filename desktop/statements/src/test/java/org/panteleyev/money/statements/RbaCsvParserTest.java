/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.model.Currency;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

public class RbaCsvParserTest {
    private static final UUID RUB_UUID = UUID.randomUUID();

    private static final DataCache dataCache = new DataCache() {{
        getCurrencies().add(new Currency.Builder().uuid(RUB_UUID).symbol("RUB").build());
    }};

    private final RbaCsvParser parser = new RbaCsvParser();

    private static List<Arguments> testDetectTypeArguments() {
        return List.of(
                argumentSet("Old", "rba_account_old.csv", StatementType.RBA_CSV_OLD),
                argumentSet("New", "rba_account_new.csv", StatementType.RBA_CSV_NEW)
        );
    }

    @ParameterizedTest
    @MethodSource("testDetectTypeArguments")
    public void testDetectType(String fileName, StatementType expected) throws IOException {
        try (var inputStream = getClass().getResourceAsStream("/" + fileName)) {
            assertEquals(expected, parser.detectType(new RawStatementData(inputStream)));
        }
    }

    private static List<Arguments> testParseArguments() {
        return List.of(
                argumentSet("RBA_CSV_OLD", "rba_account_old.csv", StatementType.RBA_CSV_OLD, new Statement(
                                StatementType.RBA_CSV_OLD, "", List.of(
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2010, 12, 31))
                                        .execution(LocalDate.of(2010, 12, 31))
                                        .description("Interest")
                                        .amount("1 234.56")
                                        .currency("RUB")
                                        .build(dataCache),
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2011, 11, 2))
                                        .execution(LocalDate.of(2011, 11, 2))
                                        .description("Перевод")
                                        .amount("-100 000.00")
                                        .currency("RUB")
                                        .build(dataCache),
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2012, 10, 31))
                                        .execution(LocalDate.of(2012, 10, 31))
                                        .description("Проценты")
                                        .amount("7 856.12")
                                        .currency("RUB")
                                        .build(dataCache),
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2013, 10, 26))
                                        .execution(LocalDate.of(2013, 10, 26))
                                        .description("FX RUR/USD [12.3400] WEB")
                                        .amount("-3 196.50")
                                        .currency("RUB")
                                        .build(dataCache)
                        ))
                ),
                argumentSet("RBA_CSV_NEW", "rba_account_new.csv", StatementType.RBA_CSV_NEW, new Statement(
                                StatementType.RBA_CSV_NEW, "", List.of(
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2024, 4, 29))
                                        .execution(LocalDate.of(2024, 4, 30))
                                        .description("Какой-то приход")
                                        .amount("4 563,33")
                                        .currency("RUB")
                                        .build(dataCache),
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2023, 4, 21))
                                        .execution(LocalDate.of(2023, 4, 21))
                                        .description("Какой-то расход")
                                        .amount("-7 000,00")
                                        .currency("RUB")
                                        .build(dataCache),
                                new StatementRecord.Builder()
                                        .actual(LocalDate.of(2023, 4, 4))
                                        .execution(LocalDate.of(2023, 4, 4))
                                        .description("Еще приход")
                                        .amount("123 456,78")
                                        .currency("RUB")
                                        .build(dataCache)
                        ))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testParseArguments")
    public void testParse(String fileName, StatementType type, Statement expected) throws IOException {
        try (var inputStream = getClass().getResourceAsStream("/" + fileName)) {
            assertEquals(expected, parser.parse(new RawStatementData(inputStream), dataCache, type));
        }
    }
}
