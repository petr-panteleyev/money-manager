/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

import org.panteleyev.money.app.BaseCompletionProvider;

import java.util.Set;

import static org.panteleyev.money.app.GlobalContext.settings;

public class StringCompletionProvider extends BaseCompletionProvider<String> {
    public StringCompletionProvider(Set<String> set) {
        super(set, () -> settings().getAutoCompleteLength());
    }

    public String getElementString(String element) {
        return element;
    }
}
