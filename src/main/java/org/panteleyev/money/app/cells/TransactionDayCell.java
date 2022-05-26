/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

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

public class TransactionDayCell extends TableCell<Transaction, Transaction> {
    private final boolean fullDate;

    public TransactionDayCell(boolean fullDate) {
        this.fullDate = fullDate;
    }

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        if (empty || transaction == null) {
            setText("");
        } else {
            if (fullDate) {
                setText(String.format("%02d.%02d.%04d",
                        transaction.day(), transaction.month(), transaction.year()));
            } else {
                setText(Integer.toString(transaction.day()));
            }
        }
    }
}
