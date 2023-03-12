/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

import org.panteleyev.money.app.BaseCompletionProvider;
import org.panteleyev.money.model.Named;

import java.util.Set;

import static org.panteleyev.money.app.GlobalContext.settings;

public class NamedCompletionProvider<T extends Named> extends BaseCompletionProvider<T> {
    public NamedCompletionProvider(Set<T> set) {
        super(set, () -> settings().getAutoCompleteLength());
    }

    public String getElementString(T element) {
        return element.name();
    }
}
