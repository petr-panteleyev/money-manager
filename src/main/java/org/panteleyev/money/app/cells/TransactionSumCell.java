/*
 Copyright (c) 2017-2022, Petr Panteleyev

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

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import java.math.RoundingMode;

public class TransactionSumCell extends TableCell<Transaction, Transaction> {
    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT, Styles.TRANSFER);

        if (empty || transaction == null) {
            setText("");
        } else {
            if (transaction.accountCreditedType() == CategoryType.EXPENSES) {
                getStyleClass().add(Styles.DEBIT);
            } else if (transaction.accountCreditedType() == transaction.accountDebitedType()) {
                getStyleClass().add(Styles.TRANSFER);
            } else {
                getStyleClass().add(Styles.CREDIT);
            }

            setText(transaction.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
