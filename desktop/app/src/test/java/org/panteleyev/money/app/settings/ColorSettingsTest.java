/*
 Copyright © 2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.app.settings.ColorName.CREDIT;
import static org.panteleyev.money.app.settings.ColorName.DEBIT;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_CHECKED;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_MISSING;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_UNCHECKED;
import static org.panteleyev.money.app.settings.ColorName.TRANSFER;

public class ColorSettingsTest {
    private static final Map<ColorName, Color> COLORS = Map.of(
            DEBIT, Color.CYAN,
            CREDIT, Color.BLUEVIOLET,
            TRANSFER, Color.WHITE,
            STATEMENT_CHECKED, Color.GREEN,
            STATEMENT_UNCHECKED, Color.YELLOW,
            STATEMENT_MISSING, Color.MAGENTA
    );

    @Test
    @DisplayName("should save and load color settings")
    public void testSaveLoad() throws Exception {
        var settings = new ColorSettings();
        for (var entry : COLORS.entrySet()) {
            settings.setColor(entry.getKey(), entry.getValue());
        }

        try (var out = new ByteArrayOutputStream()) {
            settings.save(out);

            try (var in = new ByteArrayInputStream(out.toByteArray())) {
                var loaded = new ColorSettings();
                loaded.load(in);

                for (var entry: COLORS.entrySet()) {
                    assertEquals(entry.getValue(), loaded.getColor(entry.getKey()));
                }
            }
        }
    }
}
