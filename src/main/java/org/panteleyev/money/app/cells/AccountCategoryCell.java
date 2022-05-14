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

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;
import static org.panteleyev.money.app.GlobalContext.cache;

public class AccountCategoryCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account item, boolean empty) {
        super.updateItem(item, empty);

        setText("");
        setGraphic(null);

        if (empty || item == null) {
            return;
        }

        cache().getCategory(item.categoryUuid()).ifPresent(category -> {
            setText(category.name());
            setGraphic(IconManager.getImageView(category.iconUuid()));
        });
    }
}
