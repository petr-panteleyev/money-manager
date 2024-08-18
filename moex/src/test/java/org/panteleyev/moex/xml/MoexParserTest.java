/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.moex.model.MoexEngine;
import org.panteleyev.moex.model.MoexMarket;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoexParserTest {
    private final MoexParser parser = new MoexParser();

    private static List<Arguments> testGetEnginesArguments() {
        return List.of(
                Arguments.of("engines.xml",
                        List.of(
                                new MoexEngine(1, "stock", "Фондовый рынок и рынок депозитов"),
                                new MoexEngine(2, "state", "Рынок ГЦБ (размещение)"),
                                new MoexEngine(3, "currency", "Валютный рынок"),
                                new MoexEngine(4, "futures", "Срочный рынок"),
                                new MoexEngine(5, "commodity", "Товарный рынок"),
                                new MoexEngine(6, "interventions", "Товарные интервенции"),
                                new MoexEngine(7, "offboard", "ОТС-система"),
                                new MoexEngine(9, "agro", "Агро"),
                                new MoexEngine(1012, "otc", "ОТС с ЦК"),
                                new MoexEngine(1282, "quotes", "Квоты"),
                                new MoexEngine(1326, "money", "Денежный рынок")
                        )
                ),
                Arguments.of("engines_empty.xml", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("testGetEnginesArguments")
    public void testGetEngines(String resource, List<MoexEngine> expected) throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/MoexParserTest/" + resource)) {
            assertEquals(expected, parser.getEngines(inputStream));
        }
    }

    private static List<Arguments> testGetMarketsArguments() {
        var engine = new MoexEngine(1, "stock", "");

        return List.of(
                Arguments.of("markets.xml",
                        engine,
                        List.of(
                                new MoexMarket(5, engine, "index", "Индексы фондового рынка"),
                                new MoexMarket(1, engine, "shares", "Рынок акций"),
                                new MoexMarket(2, engine, "bonds", "Рынок облигаций"),
                                new MoexMarket(4, engine, "ndm", "Режим переговорных сделок"),
                                new MoexMarket(29, engine, "otc", "ОТС"),
                                new MoexMarket(27, engine, "ccp", "РЕПО с ЦК"),
                                new MoexMarket(1015, engine, "nonresndm", "Режим переговорных сделок (нерезиденты)"),
                                new MoexMarket(1017, engine, "nonresrepo", "Рынок РЕПО (нерезиденты)"),
                                new MoexMarket(1019, engine, "nonresccp", "Рынок РЕПО с ЦК (нерезиденты)"),
                                new MoexMarket(25, engine, "classica", "Classica")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetMarketsArguments")
    public void testGetMarkets(String resource, MoexEngine engine, List<MoexMarket> expected) throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/MoexParserTest/" + resource)) {
            assertEquals(expected, parser.getMarkets(inputStream, engine));
        }
    }
}
