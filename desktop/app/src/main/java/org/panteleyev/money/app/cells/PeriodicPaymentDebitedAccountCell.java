/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.PeriodicPayment;

import static org.panteleyev.money.app.GlobalContext.cache;

public class PeriodicPaymentDebitedAccountCell extends TableCell<PeriodicPayment, PeriodicPayment> {
    @Override
    public void updateItem(PeriodicPayment periodicPayment, boolean empty) {
        super.updateItem(periodicPayment, empty);

        setText("");
        setGraphic(null);

        if (empty || periodicPayment == null) {
            return;
        }

        cache().getAccount(periodicPayment.accountDebitedUuid()).ifPresent(account -> {
            setText(account.name());
            setGraphic(IconManager.getAccountImageView(account));
        });
    }
}
