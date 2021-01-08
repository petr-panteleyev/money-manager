/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.statements.StatementRecord;
import java.math.BigDecimal;

public class StatementSumCell extends TableCell<StatementRecord, StatementRecord> {
    @Override
    public void updateItem(StatementRecord item, boolean empty) {
        super.updateItem(item, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (empty || item == null) {
            setText("");
        } else {
            var amount = item.getAmountDecimal().orElse(BigDecimal.ZERO);
            getStyleClass().add(amount.signum() < 0 ? Styles.DEBIT : Styles.CREDIT);
            setText(amount.toString());
        }
    }
}
