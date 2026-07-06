// Copyright © 2017-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.exchange.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.ExchangeSecurity;

public class ExchangeTypeCell extends TableCell<ExchangeSecurity, ExchangeSecurity> {
    @Override
    public void updateItem(ExchangeSecurity security, boolean empty) {
        super.updateItem(security, empty);

        if (empty || security == null) {
            setText("");
        } else {
            setText(security.typeName());
        }
    }
}
