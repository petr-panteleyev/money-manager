/*
 Copyright © 2017-2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.util.StringConverter;

public abstract class ToStringConverter<T> extends StringConverter<T> {
    @Override
    public T fromString(String string) {
        return null;
    }
}
