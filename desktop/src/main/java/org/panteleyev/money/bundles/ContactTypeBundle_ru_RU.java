/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class ContactTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"PERSONAL", "Личное"},
                {"CLIENT", "Клиент"},
                {"SUPPLIER", "Поставщик"},
                {"EMPLOYEE", "Сотрудник"},
                {"EMPLOYER", "Работодатель"},
                {"SERVICE", "Услуга"},
        };
    }
}
