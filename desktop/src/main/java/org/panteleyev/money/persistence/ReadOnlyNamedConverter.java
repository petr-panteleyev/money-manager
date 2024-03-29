/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

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
