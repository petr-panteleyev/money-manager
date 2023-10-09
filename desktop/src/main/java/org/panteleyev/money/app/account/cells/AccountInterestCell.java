/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccountInterestCell extends TableCell<Account, BigDecimal> {
    @Override
    public void updateItem(BigDecimal interest, boolean empty) {
        super.updateItem(interest, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || interest == null || interest.compareTo(BigDecimal.ZERO) == 0) {
            setText("");
        } else {
            setText(interest.setScale(2, RoundingMode.HALF_UP) + "%");
        }
    }
}
