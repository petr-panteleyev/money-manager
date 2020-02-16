package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
            setText(interest.setScale(2, RoundingMode.HALF_UP).toString() + "%");
        }
    }
}
