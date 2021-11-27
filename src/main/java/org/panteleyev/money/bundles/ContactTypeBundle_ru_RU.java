/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class ContactTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            {"PERSONAL", "Личное"},
            {"CLIENT", "Клиент"},
            {"SUPPLIER", "Поставщик"},
            {"EMPLOYEE", "Сотрудник"},
            {"EMPLOYER", "Работодатель"},
            {"SERVICE", "Услуга"},
        };
    }
}
