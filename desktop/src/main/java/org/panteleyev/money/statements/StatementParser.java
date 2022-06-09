/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import java.io.InputStream;

public final class StatementParser {
    public static Statement parse(Statement.StatementType type, InputStream inStream) {
        return switch (type) {
            case RAIFFEISEN_OFX -> RBAParser.parseOfx(inStream);
            case SBERBANK_HTML -> SberbankParser.parseCreditCardHtml(inStream);
            default -> throw new IllegalArgumentException();
        };
    }
}
