package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CurrencyDialogTest extends BaseTest {
    private final static Currency CURRENCY = new Currency.Builder()
        .guid(UUID.randomUUID())
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

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private void setupDialog(CurrencyDialog dialog) {
        dialog.getNameEdit().setText(CURRENCY.getSymbol());
        dialog.getDescrEdit().setText(CURRENCY.getDescription());
        dialog.getRateEdit().setText(CURRENCY.getRate().toString());
        dialog.getThousandSeparatorCheck().setSelected(CURRENCY.getUseThousandSeparator());
        dialog.getDefaultCheck().setSelected(CURRENCY.getDef());
    }

    private void setupDialogUpdate(CurrencyDialog dialog) {
    }

    @Test
    public void testNewCurrency() throws Exception {
        var queue = new ArrayBlockingQueue<Currency>(1);

        Platform.runLater(() -> {
            var dialog = new CurrencyDialog(null, null);
            setupDialog(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var currency = queue.take();

        assertNotNull(currency.getUuid());
        assertCurrency(currency);
        assertEquals(currency.getCreated(), currency.getModified());
    }

    @Test
    public void testExistingCurrency() throws Exception {
        var queue = new ArrayBlockingQueue<Currency>(1);

        Platform.runLater(() -> {
            var dialog = new CurrencyDialog(null, CURRENCY);
            setupDialogUpdate(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var currency = queue.take();

        assertEquals(currency.getUuid(), CURRENCY.getUuid());
        assertCurrency(currency);
        assertTrue(currency.getModified() > CURRENCY.getModified());
        assertTrue(currency.getModified() > currency.getCreated());
    }

    private static void assertCurrency(Currency currency) {
        assertEquals(currency.getSymbol(), CURRENCY.getSymbol());
        assertEquals(currency.getDescription(), CURRENCY.getDescription());
        assertEquals(currency.getRate(), CURRENCY.getRate());
        assertEquals(currency.getUseThousandSeparator(), CURRENCY.getUseThousandSeparator());
        assertEquals(currency.getDef(), CURRENCY.getDef());
    }
}
