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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RbaCsvParser implements Parser {
    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter EXECUTION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final List<String> OLD_FORMAT_HEADERS = List.of(
            "Дата транзакции",
            "Описание",
            "Валюта операции",
            "Сумма в валюте операции",
            "Валюта счета",
            "Сумма в валюте счета"
    );

    private static final List<String> NEW_FORMAT_HEADERS = List.of(
            "Дата операции",
            "Выполнено банком",
            "Номер документа",
            "Сумма в валюте операции (поступления)",
            "Сумма в валюте операции (расходы)",
            "Валюта операции",
            "Сумма в валюте счета (поступления)",
            "Сумма в валюте счета (расходы)",
            "Валюта счета",
            "Детали операции (назначение платежа)",
            "Номер карты"
    );

    private static final CSVFormat FORMAT = CSVFormat.Builder.create(CSVFormat.EXCEL)
            .setHeader()
            .setDelimiter(';')
            .get();

    @Override
    public StatementType detectType(RawStatementData data) {
        // Try new format
        var parser = getParser(new ByteArrayInputStream(data.getBytes()), StandardCharsets.UTF_8);
        if (checkHeaders(parser.getHeaderNames(), NEW_FORMAT_HEADERS)) {
            return StatementType.RBA_CSV_NEW;
        }

        // Try old format
        parser = getParser(new ByteArrayInputStream(data.getBytes()), WINDOWS_1251);
        return checkHeaders(parser.getHeaderNames(), OLD_FORMAT_HEADERS) ? StatementType.RBA_CSV_OLD : null;
    }

    private static boolean checkHeaders(List<String> headers, List<String> expected) {
        if (headers == null || headers.size() != expected.size()) {
            return false;
        }

        for (int index = 0; index < expected.size(); index++) {
            var header = headers.get(index);
            if (index == 0) {
                // Удаляем UTF-8 BOM
                header = header.replace("\uFEFF", "");
            }

            if (!Objects.equals(header, expected.get(index))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Statement parse(RawStatementData data, DataCache cache, StatementType type) {
        try (var inputStream = new ByteArrayInputStream(data.getBytes())) {
            return parseCsv(inputStream, cache, type);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Statement parseCsv(InputStream inputStream, DataCache cache, StatementType type) {
        try {
            var records = new ArrayList<StatementRecord>();

            var charset = type == StatementType.RBA_CSV_OLD ? WINDOWS_1251 : StandardCharsets.UTF_8;
            CSVParser parser = getParser(inputStream, charset);

            for (var r : parser.getRecords()) {
                var newFormat = type == StatementType.RBA_CSV_NEW;

                LocalDate date = LocalDate.parse(r.get(0), DATE_FORMAT);
                LocalDate executionDate;
                if (newFormat) {
                    executionDate = LocalDate.parse(r.get(1), EXECUTION_DATE_FORMAT);
                } else {
                    executionDate = date;
                }

                String amount;

                if (newFormat) {
                    amount = r.get(6);
                    if (amount == null || amount.isBlank()) {
                        amount = "-" + r.get(7);
                    }
                } else {
                    amount = r.get(5);
                }

                var currency = newFormat ? r.get(8) : r.get(4);
                var description = newFormat ? r.get(9) : r.get(1);

                var builder = new StatementRecord.Builder()
                        .actual(date)
                        .execution(executionDate)
                        .currency(currency)
                        .description(description)
                        .amount(amount.replaceAll(" ", ""));

                records.add(builder.build(cache));
            }

            return new Statement(type, "", records);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private CSVParser getParser(InputStream inputStream, Charset charset) {
        try {
            return CSVParser.parse(inputStream, charset, FORMAT);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
