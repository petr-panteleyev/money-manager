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
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import java.math.RoundingMode;

public class TransactionAccountRequestSumCell extends TableCell<Transaction, Transaction> {
    private final Account account;

    public TransactionAccountRequestSumCell(Account account) {
        this.account = account;
    }

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (empty || transaction == null) {
            setText("");
        } else {
            getStyleClass().add(
                transaction.accountDebitedUuid().equals(account.uuid()) ?
                    Styles.DEBIT : Styles.CREDIT
            );

            setText(transaction.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
