/*
 Copyright (C) 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionCheckCell extends TableCell<Transaction, Transaction> {
    private static final String CHECK_SYMBOL = "\u2714";

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setGraphic(null);

        if (empty || transaction == null || !transaction.checked()) {
            setText("");
        } else {
            setText(CHECK_SYMBOL);
        }
    }
}
