/*
 Copyright (C) 2022 Petr Panteleyev

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
import javafx.scene.image.ImageView;
import org.panteleyev.money.app.Images;
import org.panteleyev.money.model.MoneyRecord;

import static org.panteleyev.money.app.GlobalContext.cache;

public class DocumentCountCell<T extends MoneyRecord> extends TableCell<T, T> {
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        setText("");
        setGraphic(null);

        if (item == null || empty) {
            return;
        }

        var count = cache().getDocumentCount(item);
        if (count > 0) {
            setText(Long.toString(count));
            setGraphic(new ImageView(Images.ATTACHMENT));
        }
    }
}
