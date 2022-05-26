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

import javafx.scene.control.TableRow;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.StatementRecord;

import static org.panteleyev.money.app.Styles.STATEMENT_ALL_CHECKED;
import static org.panteleyev.money.app.Styles.STATEMENT_NOT_CHECKED;
import static org.panteleyev.money.app.Styles.STATEMENT_NOT_FOUND;

public class StatementRow extends TableRow<StatementRecord> {
    @Override
    public void updateItem(StatementRecord item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(STATEMENT_NOT_FOUND, STATEMENT_NOT_CHECKED, STATEMENT_ALL_CHECKED);
        if (item == null || empty) {
            return;
        }

        var transactions = item.getTransactions();

        if (transactions.isEmpty()) {
            getStyleClass().add(STATEMENT_NOT_FOUND);
        } else {
            if (transactions.stream().allMatch(Transaction::checked)) {
                getStyleClass().add(STATEMENT_ALL_CHECKED);
            } else {
                getStyleClass().add(STATEMENT_NOT_CHECKED);
            }
        }
    }
}
