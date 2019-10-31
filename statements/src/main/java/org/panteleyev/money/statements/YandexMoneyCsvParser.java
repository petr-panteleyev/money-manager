/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.statements;

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
