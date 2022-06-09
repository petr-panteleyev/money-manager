/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.MoneyDocument;

import java.util.UUID;

import static org.panteleyev.money.app.GlobalContext.cache;

public class DocumentContactNameCell extends TableCell<MoneyDocument, Object> {
    @Override
    protected void updateItem(Object data, boolean empty) {
        super.updateItem(data, empty);

        setText("");
        setGraphic(null);

        if (!empty && data instanceof UUID uuid) {
            cache().getContact(uuid).ifPresent(contact -> {
                setText(contact.name());
                setGraphic(IconManager.getImageView(contact.iconUuid()));
            });
        }
    }
}
