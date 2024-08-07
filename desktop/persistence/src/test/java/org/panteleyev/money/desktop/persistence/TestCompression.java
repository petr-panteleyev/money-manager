/*
 Copyright © 2022-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.panteleyev.money.desktop.persistence.Compression.compress;
import static org.panteleyev.money.desktop.persistence.Compression.decompress;

public class TestCompression {

    private static List<Arguments> testData() {
        return List.of(
                Arguments.of((Object) null),
                Arguments.of((Object) new byte[0]),
                Arguments.of((Object) BaseTestUtils.randomBytes(1000))
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testCompression(byte[] bytes) {
        var compressed = compress(bytes);
        var decompressed = decompress(compressed);
        assertArrayEquals(bytes, decompressed);
    }
}
