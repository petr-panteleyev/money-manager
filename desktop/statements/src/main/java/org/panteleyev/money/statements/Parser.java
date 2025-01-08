/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.panteleyev.money.desktop.commons.DataCache;

interface Parser {
    StatementType detectType(RawStatementData data);

    Statement parse(RawStatementData data, DataCache cache);
}
