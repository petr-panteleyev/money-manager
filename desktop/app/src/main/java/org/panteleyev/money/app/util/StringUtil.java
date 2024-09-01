/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

public final class StringUtil {
    public static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        var c = str.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private StringUtil() {
    }
}
