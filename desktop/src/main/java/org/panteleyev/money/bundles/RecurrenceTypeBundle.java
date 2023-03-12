/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

import static org.panteleyev.money.model.RecurrenceType.MONTHLY;
import static org.panteleyev.money.model.RecurrenceType.YEARLY;

public class RecurrenceTypeBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
                {MONTHLY.name(), "Montly"},
                {YEARLY.name(), "Yearly"}
        };
    }

}
