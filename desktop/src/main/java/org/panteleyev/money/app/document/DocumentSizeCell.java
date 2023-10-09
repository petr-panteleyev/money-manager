/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.document;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.MoneyDocument;

class DocumentSizeCell extends TableCell<MoneyDocument, MoneyDocument> {
    @Override
    protected void updateItem(MoneyDocument document, boolean empty) {
        super.updateItem(document, empty);

        setText("");
        setGraphic(null);

        if (!empty && document != null) {
            setText(Integer.toString(document.size()));
        }
    }
}
