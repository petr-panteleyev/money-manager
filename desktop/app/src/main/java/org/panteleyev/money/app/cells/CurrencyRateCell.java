/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Currency;

import java.math.RoundingMode;

public class CurrencyRateCell extends TableCell<Currency, Currency> {
    @Override
    public void updateItem(Currency currency, boolean empty) {
        super.updateItem(currency, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || currency == null) {
            setText("");
        } else {
            setText(currency.rate().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
