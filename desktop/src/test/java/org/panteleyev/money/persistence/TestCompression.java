/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.panteleyev.money.persistence.Compression.compress;
import static org.panteleyev.money.persistence.Compression.decompress;
import static org.panteleyev.money.test.BaseTestUtils.randomBytes;

public class TestCompression {

    private static List<Arguments> testData() {
        return List.of(
                Arguments.of((Object) null),
                Arguments.of((Object) new byte[0]),
                Arguments.of((Object) randomBytes(1000))
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
