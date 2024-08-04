/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.currency;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Currency;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;

public class CurrencyDialogTest {
    private final static Currency CURRENCY = new Currency.Builder()
            .uuid(UUID.randomUUID())
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(randomBoolean())
            .def(randomBoolean())
            .rate(randomBigDecimal())
            .direction(1)
            .useThousandSeparator(randomBoolean())
            .created(System.currentTimeMillis())
            .modified(System.currentTimeMillis())
            .build();

    @BeforeAll
    public static void init() {
        new JFXPanel();
    }

    private void setupDialog(CurrencyDialog dialog) {
        dialog.getNameEdit().setText(CURRENCY.symbol());
        dialog.getDescrEdit().setText(CURRENCY.description());
        dialog.getRateEdit().setText(CURRENCY.rate().toString());
        dialog.getThousandSeparatorCheck().setSelected(CURRENCY.useThousandSeparator());
        dialog.getDefaultCheck().setSelected(CURRENCY.def());
    }

    private void setupDialogUpdate(CurrencyDialog dialog) {
    }

    @Test
    public void testNewCurrency() throws Exception {
        var queue = new ArrayBlockingQueue<Currency>(1);

        Platform.runLater(() -> {
            var dialog = new CurrencyDialog(null, null, null);
            setupDialog(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var currency = queue.take();

        assertNotNull(currency.uuid());
        assertCurrency(currency);
        assertEquals(currency.created(), currency.modified());
    }

    @Test
    public void testExistingCurrency() throws Exception {
        var queue = new ArrayBlockingQueue<Currency>(1);

        Platform.runLater(() -> {
            var dialog = new CurrencyDialog(null, null, CURRENCY);
            setupDialogUpdate(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var currency = queue.take();

        assertEquals(CURRENCY.uuid(), currency.uuid());
        assertCurrency(currency);
        assertTrue(currency.modified() > CURRENCY.modified());
        assertTrue(currency.modified() > currency.created());
    }

    private static void assertCurrency(Currency currency) {
        assertEquals(CURRENCY.symbol(), currency.symbol());
        assertEquals(CURRENCY.description(), currency.description());
        assertEquals(CURRENCY.rate(), currency.rate());
        assertEquals(CURRENCY.useThousandSeparator(), currency.useThousandSeparator());
        assertEquals(CURRENCY.def(), currency.def());
    }
}
