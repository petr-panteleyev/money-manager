/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordSerializerTest {
    private static List<Arguments> testFromStringArguments() {
        return List.of(
                Arguments.of(String.class, "100", "100"),
                Arguments.of(Integer.class, "100", 100),
                Arguments.of(Long.class, "100", 100L),
                Arguments.of(Boolean.class, "true", true),
                Arguments.of(BigDecimal.class, "123.12", new BigDecimal("123.12")),
                Arguments.of(UUID.class, "33db0377-4072-4d9d-9107-609bf0c5e321",
                        UUID.fromString("33db0377-4072-4d9d-9107-609bf0c5e321")),
                Arguments.of(LocalDate.class, "2023-12-24", LocalDate.of(2023, 12, 24)),
                Arguments.of(LocalDateTime.class, "2023-12-24T12:23:34",
                        LocalDateTime.of(2023, 12, 24, 12, 23, 34))
        );
    }

    @SuppressWarnings("rawtypes")
    @ParameterizedTest
    @MethodSource("testFromStringArguments")
    public void testFromString(Class type, String value, Object expected) {
        assertEquals(expected, RecordSerializer.fromString(type, value));
    }

    private static List<Arguments> testFromNullArguments() {
        return List.of(
                Arguments.of(Integer.class, null),
                Arguments.of(Integer.TYPE, 0),
                Arguments.of(Long.class, null),
                Arguments.of(Long.TYPE, 0),
                Arguments.of(Boolean.class, null),
                Arguments.of(Boolean.TYPE, false),
                Arguments.of(String.class, null)
        );
    }

    @SuppressWarnings("rawtypes")
    @ParameterizedTest
    @MethodSource("testFromNullArguments")
    public void testFromNull(Class type, Object expected) {
        assertEquals(expected, RecordSerializer.fromNull(type));
    }
}
