/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons;

import javafx.util.StringConverter;

public abstract class ReadOnlyStringConverter<T> extends StringConverter<T> {
    @Override
    public T fromString(String s) {
        throw new UnsupportedOperationException("Read-only");
    }
}
