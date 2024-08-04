/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons;

import org.panteleyev.money.model.MoneyDocument;

public interface DocumentProvider {
    byte[] getDocumentBytes(MoneyDocument document);
}
