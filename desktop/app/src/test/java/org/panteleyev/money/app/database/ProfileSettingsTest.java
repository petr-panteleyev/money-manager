/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.test.BaseTestUtils.randomInt;
import static org.panteleyev.money.test.BaseTestUtils.randomString;

public class ProfileSettingsTest {

    private static final List<ConnectionProfile> PROFILE_LIST = List.of(
            new ConnectionProfile(
                    randomString(),
                    randomString(),
                    randomInt(),
                    randomString(),
                    randomString(),
                    randomString(),
                    randomString()
            ),
            new ConnectionProfile(
                    randomString(),
                    randomString(),
                    randomInt(),
                    randomString(),
                    randomString(),
                    randomString(),
                    randomString()
            )
    );

    public static List<Arguments> dataProvider() {
        return List.of(
                Arguments.of(
                        new ProfileSettings(
                                PROFILE_LIST,
                                randomString(),
                                true
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testImportExport(ProfileSettings settings) throws IOException {
        try (var out = new ByteArrayOutputStream()) {
            settings.save(out);
            try (var in = new ByteArrayInputStream(out.toByteArray())) {
                var actual = ProfileSettings.load(in);
                assertEquals(settings, actual);
            }
        }
    }
}
