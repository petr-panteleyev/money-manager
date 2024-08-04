/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons;

import javafx.util.StringConverter;
import org.panteleyev.money.model.Named;

public class ReadOnlyNamedConverter<T extends Named> extends StringConverter<T> {
    @Override
    public String toString(T object) {
        return object == null ? null : object.name();
    }

    @Override
    public T fromString(String string) {
        throw new UnsupportedOperationException("Read-only");
    }
}
