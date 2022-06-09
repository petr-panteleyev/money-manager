/*
 Copyright © 2019-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Contact;

public class ContactNameCell extends TableCell<Contact, Contact> {
    @Override
    protected void updateItem(Contact contact, boolean empty) {
        super.updateItem(contact, empty);

        if (empty || contact == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(contact.name());
            setGraphic(IconManager.getImageView(contact.iconUuid()));
        }
    }
}
