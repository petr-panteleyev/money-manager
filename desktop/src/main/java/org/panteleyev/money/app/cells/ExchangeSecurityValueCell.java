/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.text.TextAlignment;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import static org.panteleyev.money.app.exchange.Definitions.STOCK_BONDS;

public class ExchangeSecurityValueCell extends TableCell<ExchangeSecurity, ExchangeSecurity> {
    @Override
    protected void updateItem(ExchangeSecurity security, boolean empty) {
        super.updateItem(security, empty);

        if (empty || security == null) {
            setText("");
        } else {
            var text = security.marketValue().toString();
            if (security.group().equals(STOCK_BONDS)) {
                text += "%";
            }
            setText(text);
            setAlignment(Pos.CENTER_RIGHT);
        }
    }
}
