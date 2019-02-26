/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.ofx.BankTransactionList;
import org.panteleyev.ofx.OFXParser;
import org.panteleyev.ofx.StatementTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

class RBAParser {
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final char CSV_DELIMITER = ';';
    private static final int CSV_CARD_RECORD_SIZE = 10;
    private static final int CSV_ACCOUNT_RECORD_SIZE = 6;
    private static final String CSV_ENCODING = "windows-1251";

    static Statement parseAccountCSV(InputStream inStream) {
        try {
            var records = new ArrayList<StatementRecord>();
            var parser = CSVParser.parse(inStream, Charset.forName(CSV_ENCODING),
                CSVFormat.EXCEL.withDelimiter(CSV_DELIMITER));

            for (var r : parser.getRecords()) {
                if (r.getRecordNumber() == 1L || r.size() < CSV_ACCOUNT_RECORD_SIZE) {
                    continue;
                }

                var date = LocalDate.parse(r.get(0), CSV_DATE_FORMAT);

                records.add(new StatementRecord(
                    date,
                    date,
                    r.get(1),
                    "",
                    "",
                    "",
                    r.get(2),
                    r.get(3).replaceAll(" ", ""),
                    r.get(4),
                    r.get(5).replaceAll(" ", "")
                ));
            }

            return new Statement(Statement.StatementType.RAIFFEISEN_ACCOUNT_CSV, null, records);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Statement parseCardCSV(InputStream inStream) {
        try {
            var records = new ArrayList<StatementRecord>();
            var parser = CSVParser.parse(inStream, Charset.forName(CSV_ENCODING),
                CSVFormat.EXCEL.withDelimiter(CSV_DELIMITER));

            for (var r : parser.getRecords()) {
                if (r.getRecordNumber() == 1L || r.size() < CSV_CARD_RECORD_SIZE) {
                    continue;
                }

                records.add(new StatementRecord(
                    LocalDate.parse(r.get(0), CSV_DATE_FORMAT),
                    LocalDate.parse(r.get(1), CSV_DATE_FORMAT),
                    r.get(2),
                    r.get(3),
                    r.get(4),
                    r.get(5),
                    r.get(6),
                    r.get(7).replaceAll(" ", ""),
                    r.get(8),
                    r.get(9).replaceAll(" ", "")
                ));
            }

            return new Statement(Statement.StatementType.RAIFFEISEN_CREDIT_CARD_CSV, null, records);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Statement parseOfx(InputStream inStream) {
        var records = new ArrayList<StatementRecord>();

        var parser = new OFXParser();
        var ofxStatement = parser.parse(inStream);

        var transactionList = new BankTransactionList();
        var accountNumber = "";

        if (!ofxStatement.getAccountStatements().isEmpty()) {
            var statement = ofxStatement.getAccountStatements().get(0);
            transactionList = statement.getBankTransactionList();
            accountNumber = statement.getAccountInfo().getAccountNumber();
        } else if (!ofxStatement.getCreditCardStatements().isEmpty()) {
            var statement = ofxStatement.getCreditCardStatements().get(0);
            transactionList = ofxStatement.getCreditCardStatements().get(0).getBankTransactionList();
            accountNumber = statement.getAccountInfo().getAccountNumber();
        }

        for (StatementTransaction tr : transactionList.getTransactions()) {
            var builder = new StatementRecord.Builder()
                .amount(tr.getAmount().toString())
                .counterParty(tr.getName())
                .description(tr.getMemo())
                .actual(tr.getDatePosted().toLocalDate())
                .execution(tr.getDateAvailable().toLocalDate());
            records.add(builder.build());
        }

        return new Statement(Statement.StatementType.RAIFFEISEN_OFX, accountNumber, records);
    }
}
