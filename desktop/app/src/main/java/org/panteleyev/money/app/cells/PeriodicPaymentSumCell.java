/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.PeriodicPayment;

import java.math.RoundingMode;

public class PeriodicPaymentSumCell extends TableCell<PeriodicPayment, PeriodicPayment> {
    @Override
    public void updateItem(PeriodicPayment periodicPayment, boolean empty) {
        super.updateItem(periodicPayment, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || periodicPayment == null) {
            setText("");
        } else {
            setText(periodicPayment.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
