/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

import static org.panteleyev.money.model.RecurrenceType.MONTHLY;
import static org.panteleyev.money.model.RecurrenceType.YEARLY;

public class RecurrenceTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
                {MONTHLY.name(), "Раз в месяц"},
                {YEARLY.name(), "Раз в год"}
        };
    }

}
