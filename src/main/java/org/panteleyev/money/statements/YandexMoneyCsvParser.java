package org.panteleyev.money.statements;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class YandexMoneyCsvParser {
    private static final char CSV_DELIMITER = ';';

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static Statement parseYandexMoneyCsv(InputStream inputStream) {
        try {
            var records = new ArrayList<StatementRecord>();

            var parser = CSVParser.parse(inputStream, StandardCharsets.UTF_8,
                CSVFormat.EXCEL.withDelimiter(CSV_DELIMITER));

            var accountNumber = "";

            for (var r : parser.getRecords()) {
                if (r.getRecordNumber() == 1) {
                    // parse account number
                    var nIndex = r.get(0).indexOf("№");
                    if (nIndex > 0) {
                        accountNumber = r.get(0).substring(nIndex + 1);
                    }
                }

                if (r.getRecordNumber() < 6) {
                    continue;
                }

                if (r.get(0).equals("+/-")) {
                    continue;
                }

                if (r.size() < 6) {
                    continue;
                }

                var sign = r.get(0);
                var date = LocalDate.parse(r.get(1).substring(0, 10), DATE_FORMATTER);
                var sum = new BigDecimal(r.get(2).replaceAll(",", "."));
                if (sign.equals("-")) {
                    sum = sum.negate();
                }
                var currency = r.get(3);
                var description = r.get(5);

                records.add(new StatementRecord(
                    date, date, description, "", "", "", currency, sum.toString(),
                    currency, sum.toString()
                ));
            }

            return new Statement(Statement.StatementType.YANDEX_MONEY_CSV, accountNumber, records);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
