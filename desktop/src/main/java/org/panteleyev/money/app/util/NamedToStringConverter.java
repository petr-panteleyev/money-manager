/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

import javafx.util.StringConverter;
import org.panteleyev.money.model.Named;

public class NamedToStringConverter<T extends Named> extends StringConverter<T> {
    public String toString(T obj) {
        return obj.name();
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}
