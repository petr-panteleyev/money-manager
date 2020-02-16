package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.statements.StatementRecord;
import java.math.BigDecimal;
import static org.panteleyev.money.Styles.BLACK_TEXT;
import static org.panteleyev.money.Styles.RED_TEXT;

public class StatementSumCell extends TableCell<StatementRecord, StatementRecord> {
    @Override
    public void updateItem(StatementRecord item, boolean empty) {
        super.updateItem(item, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || item == null) {
            setText("");
        } else {
            var amount = item.getAmountDecimal().orElse(BigDecimal.ZERO);

            getStyleClass().removeAll(RED_TEXT, BLACK_TEXT);
            getStyleClass().add(amount.signum() < 0 ? RED_TEXT : BLACK_TEXT);

            setText(amount.toString());
        }
    }
}
