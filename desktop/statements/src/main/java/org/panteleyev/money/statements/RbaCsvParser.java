/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.panteleyev.money.desktop.commons.DataCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class RbaCsvParser implements Parser {
    private static final char DELIMITER = ';';
    private static final String ENCODING = "windows-1251";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int RECORD_SIZE = 6;

    @Override
    public StatementType detectType(RawStatementData data) {
        try (var inputStream = new ByteArrayInputStream(data.getBytes())) {
            var parser = getParser(inputStream);
            var headers = parser.getHeaderNames();
            if (headers == null || headers.isEmpty()) {
                return null;
            }
            return StatementType.RBA_CSV;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Statement parse(RawStatementData data, DataCache cache) {
        try (var inputStream = new ByteArrayInputStream(data.getBytes())) {
            return parseCsv(inputStream, cache);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    Statement parseCsv(InputStream inputStream, DataCache cache) {
        try {
            var records = new ArrayList<StatementRecord>();
            CSVParser parser = getParser(inputStream);

            for (var r : parser.getRecords()) {
                if (r.getRecordNumber() == 1L || r.size() < RECORD_SIZE) {
                    continue;
                }

                var date = LocalDate.parse(r.get(0), DATE_FORMAT);

                var builder = new StatementRecord.Builder()
                        .actual(date)
                        .execution(date)
                        .description(r.get(1))
                        .amount(r.get(5).replaceAll(" ", ""));

                records.add(builder.build(cache));
            }

            return new Statement(StatementType.RBA_CSV, "", records);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private CSVParser getParser(InputStream inputStream) {
        try {
            return CSVParser.parse(inputStream, Charset.forName(ENCODING),
                    CSVFormat.EXCEL.withDelimiter(DELIMITER));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
