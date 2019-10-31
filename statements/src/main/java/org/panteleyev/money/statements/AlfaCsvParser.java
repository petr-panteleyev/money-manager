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
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AlfaCsvParser {
    private final static Logger LOGGER = Logger.getLogger(SberbankParser.class.getName());

    private static final char CSV_DELIMITER = ';';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    private static final Pattern DESCRIPTION_PATTERN =
        Pattern.compile("(.*)(\\d{2}\\.\\d{2}\\.\\d{2})\\s(\\d{2}\\.\\d{2}\\.\\d{2}).*");

    static Statement parseAlfaCsvStatement(InputStream inputStream) {
        try {
            var parser = CSVParser.parse(inputStream, Charset.forName("Windows-1251"),
                CSVFormat.DEFAULT.withDelimiter(CSV_DELIMITER)
            );

            var csvRecords = parser.getRecords();
            if (csvRecords.size() < 2) {
                LOGGER.warning("Transactions not found in statement");
                return new Statement(Statement.StatementType.ALFA_BANK_CSV, "", Collections.emptyList());
            }

            if (csvRecords.get(0).size() != 9) {
                LOGGER.log(Level.SEVERE, "Unsupported statement format");
                return new Statement(Statement.StatementType.ALFA_BANK_CSV, "", Collections.emptyList());
            }

            var records = new ArrayList<StatementRecord>();
            var accountNumber = "";

            for (var r : csvRecords) {
                if (r.getRecordNumber() == 1) {
                    continue;
                }

                if (r.get(4).equalsIgnoreCase("HOLD")) {
                    continue;
                }

                if (accountNumber.isEmpty()) {
                    accountNumber = r.get(1);
                }

                records.add(parseAccountStatementRecord(r));
            }

            return new Statement(Statement.StatementType.ALFA_BANK_CSV, accountNumber, records);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static StatementRecord parseAccountStatementRecord(CSVRecord r) {
        var currency = r.get(2);
        var actualDate = LocalDate.parse(r.get(3), DATE_FORMATTER);
        var executionDate = actualDate;
        var description = r.get(5).trim();
        var credit = new BigDecimal(r.get(6).replaceAll(",", "."));
        var debit = new BigDecimal(r.get(7).replaceAll(",", "."));

        BigDecimal sum;

        if (debit.compareTo(BigDecimal.ZERO) == 0) {
            sum = credit;
        } else {
            sum = debit.negate();

            Matcher matcher = DESCRIPTION_PATTERN.matcher(description);
            if (matcher.matches() && matcher.groupCount() == 3) {
                description = matcher.group(1);
                executionDate = LocalDate.parse(matcher.group(2), DATE_FORMATTER);
                actualDate = LocalDate.parse(matcher.group(3), DATE_FORMATTER);
            }
        }

        return new StatementRecord(
            actualDate, executionDate, description, "", "", "", currency, sum.toString(),
            currency, sum.toString()
        );
    }
}
