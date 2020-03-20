package org.panteleyev.money.statements;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.io.InputStream;

public final class StatementParser {
    public static Statement parse(Statement.StatementType type, InputStream inStream) {
        return switch (type) {
            case RAIFFEISEN_OFX -> RBAParser.parseOfx(inStream);
            case SBERBANK_HTML -> SberbankParser.parseCreditCardHtml(inStream);
            case YANDEX_MONEY_CSV -> YandexMoneyCsvParser.parseYandexMoneyCsv(inStream);
            case ALFA_BANK_CSV -> AlfaCsvParser.parseAlfaCsvStatement(inStream);
            default -> throw new IllegalArgumentException();
        };
    }
}
