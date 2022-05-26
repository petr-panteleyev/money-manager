/*
 Copyright (C) 2017-2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
                    randomString()
            ),
            new ConnectionProfile(
                    randomString(),
                    randomString(),
                    randomInt(),
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
