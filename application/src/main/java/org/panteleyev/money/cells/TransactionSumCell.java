package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import java.math.RoundingMode;
import static org.panteleyev.money.Styles.BLACK_TEXT;
import static org.panteleyev.money.Styles.BLUE_TEXT;
import static org.panteleyev.money.Styles.RED_TEXT;

public class TransactionSumCell extends TableCell<Transaction, Transaction> {
    @Override
    public void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || item == null) {
            setText("");
        } else {
            getStyleClass().removeAll(RED_TEXT, BLUE_TEXT, BLACK_TEXT);

            var s = BLACK_TEXT;
            if (item.accountCreditedType() != item.accountDebitedType()) {
                if (item.accountDebitedType() == CategoryType.INCOMES) {
                    s = BLUE_TEXT;
                } else {
                    s = RED_TEXT;
                }
            }
            getStyleClass().add(s);

            setText(item.getSignedAmount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
