package org.panteleyev.money.persistence;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
