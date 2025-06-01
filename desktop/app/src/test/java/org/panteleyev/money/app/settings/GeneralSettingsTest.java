/*
 Copyright © 2024-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.ACCOUNT_CLOSING_DAY_DELTA;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.AUTO_COMPLETE_LENGTH;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.LAST_EXPORT_DIR;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.LAST_REPORT_DIR;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.LAST_STATEMENT_DIR;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.SHOW_DEACTIVATED_ACCOUNTS;
import static org.panteleyev.money.app.settings.GeneralSettings.Setting.SHOW_DEACTIVATED_CARDS;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.test.BaseTestUtils.randomString;

public class GeneralSettingsTest {
    private static final Map<GeneralSettings.Setting, Object> SETTINGS = Map.of(
            AUTO_COMPLETE_LENGTH, 13,
            ACCOUNT_CLOSING_DAY_DELTA, 14,
            SHOW_DEACTIVATED_ACCOUNTS, randomBoolean(),
            SHOW_DEACTIVATED_CARDS, randomBoolean(),
            LAST_STATEMENT_DIR, randomString(),
            LAST_EXPORT_DIR, randomString(),
            LAST_REPORT_DIR, randomString()
    );

    @Test
    @DisplayName("should save and load general settings")
    public void testSaveLoad() throws Exception {
        var settings = new GeneralSettings();
        for (var entry : SETTINGS.entrySet()) {
            settings.put(entry.getKey(), entry.getValue());
        }

        try (var out = new ByteArrayOutputStream()) {
            settings.save(out);

            try (var in = new ByteArrayInputStream(out.toByteArray())) {
                var loaded = new GeneralSettings();
                loaded.load(in);

                for (var entry : SETTINGS.entrySet()) {
                    assertEquals(entry.getValue(), loaded.get(entry.getKey()));
                }
            }
        }
    }
}
