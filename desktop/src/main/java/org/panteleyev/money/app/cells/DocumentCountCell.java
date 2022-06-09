/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
