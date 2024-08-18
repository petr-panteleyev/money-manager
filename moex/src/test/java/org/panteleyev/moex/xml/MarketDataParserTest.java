/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.moex.model.MoexMarketData;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketDataParserTest {
    private final MarketDataParser parser = new MarketDataParser();

    private static List<Arguments> testParseMarketDataArguments() {
        return List.of(
                Arguments.of("response.xml",
                        new MoexMarketData(
                                "SBER",
                                "TQBR",
                                new BigDecimal("281.05"),
                                new BigDecimal("280.42"),
                                new BigDecimal("282.41"),
                                new BigDecimal("281.54"),
                                new BigDecimal("279.72"),
                                null,
                                null,
                                null
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testParseMarketDataArguments")
    public void testParseMarketData(String resource, MoexMarketData expected) throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/MarketDataParserTest/" + resource)) {
            assertEquals(expected, parser.parseMarketData(inputStream).orElseThrow());
        }
    }
}
