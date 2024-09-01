/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

    private static List<Arguments> testDecapitalizeArguments() {
        return List.of(
            Arguments.of(null, null),
            Arguments.of("", ""),
            Arguments.of(" ", " "),
            Arguments.of("Word", "word"),
            Arguments.of("UpperCaseProper", "upperCaseProper"),
            Arguments.of("lowerCaseProper", "lowerCaseProper"),
            Arguments.of("Паи ПИФов", "паи ПИФов")
        );
    }


    @ParameterizedTest
    @MethodSource("testDecapitalizeArguments")
    public void testDecapitalize(String given, String expected) {
        var actual = StringUtil.decapitalize(given);
        assertEquals(expected, actual);
    }
}
