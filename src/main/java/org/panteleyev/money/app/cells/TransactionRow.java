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

import javafx.scene.control.TableRow;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.Styles.GROUP_CELL;

public class TransactionRow extends TableRow<Transaction> {
    @Override
    public void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(GROUP_CELL);

        if (item != null && !empty) {
            if (item.detailed()) {
                getStyleClass().add(GROUP_CELL);
            }
        }
    }
}
