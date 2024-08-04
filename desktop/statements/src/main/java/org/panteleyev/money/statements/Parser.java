/*
 Copyright © 2022-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.panteleyev.money.desktop.commons.DataCache;

interface Parser {
    StatementType detectType(String content);

    Statement parse(String content, DataCache cache);
}
