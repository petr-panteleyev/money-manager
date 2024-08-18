/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.moex.model.MoexSecurity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecurityParserTest {
    private final SecurityParser parser = new SecurityParser();

    private static List<Arguments> testParseSecurityArguments() {
        return List.of(
                Arguments.of("share.xml",
                        new MoexSecurity(
                                "SBER",
                                "stock",
                                "shares",
                                "TQBR",
                                "Сбербанк России ПАО ао",
                                "Сбербанк",
                                "RU0009029540",
                                "10301481B",
                                new BigDecimal("3"),
                                LocalDate.of(2007, 7, 20),
                                null,
                                null,
                                "common_share",
                                "Акция обыкновенная",
                                "stock_shares",
                                "Акции",
                                null,
                                null,
                                null,
                                null
                        )
                ),
                Arguments.of("bond.xml", new MoexSecurity(
                                "RU000A105SD9",
                                "stock",
                                "bonds",
                                "TQCB",
                                "Сбербанк ПАО 001Р-SBER42",
                                "Сбер Sb42R",
                                "RU000A105SD9",
                                "4B02-582-01481-B-001P",
                                new BigDecimal("1000"),
                                LocalDate.of(2023, 2, 3),
                                LocalDate.of(2025, 3, 2),
                                201,
                                "exchange_bond",
                                "Биржевая облигация",
                                "stock_bonds",
                                "Облигации",
                                new BigDecimal("43.88"),
                                new BigDecimal("8.8"),
                                LocalDate.of(2024, 9, 1),
                                2
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testParseSecurityArguments")
    public void testParseSecurity(String resource, MoexSecurity expected) throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/SecurityParserTest/" + resource)) {
            assertEquals(expected, parser.parseSecurity(inputStream).orElse(null));
        }
    }
}
