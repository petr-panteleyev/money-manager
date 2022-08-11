/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.panteleyev.money.test.BaseTestUtils.randomInt;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

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

    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][]{
                {
                        new ProfileSettings(
                                PROFILE_LIST,
                                randomString(),
                                true
                        )
                }
        };
    }

    @Test(dataProvider = "dataProvider")
    public void testImportExport(ProfileSettings settings) throws IOException {
        try (var out = new ByteArrayOutputStream()) {
            settings.save(out);
            try (var in = new ByteArrayInputStream(out.toByteArray())) {
                var actual = ProfileSettings.load(in);
                assertEquals(actual, settings);
            }
        }
    }
}
