/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.TransactionDetail;
import java.math.RoundingMode;

public class TransactionDetailSumCell extends TableCell<TransactionDetail, TransactionDetail> {
    @Override
    public void updateItem(TransactionDetail item, boolean empty) {
        super.updateItem(item, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || item == null) {
            setText("");
        } else {
            setText(item.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
