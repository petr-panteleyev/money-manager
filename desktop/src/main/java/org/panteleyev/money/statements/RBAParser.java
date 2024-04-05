/*
 Copyright © 2018-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.panteleyev.ofx.BankTransactionList;
import org.panteleyev.ofx.OFXParser;
import org.panteleyev.ofx.StatementTransaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class RBAParser implements Parser {
    @Override
    public StatementType detectType(String content) {
        if (content.contains("<?OFX")) {
            return StatementType.OFX;
        } else {
            return StatementType.UNKNOWN;
        }
    }

    public Statement parse(String content) {
        try (var inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return parseOfx(inputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static Statement parseOfx(InputStream inStream) {
        var records = new ArrayList<StatementRecord>();

        var parser = new OFXParser();
        var ofxStatement = parser.parse(inStream);

        var transactionList = new BankTransactionList();
        var accountNumber = "";

        if (!ofxStatement.getAccountStatements().isEmpty()) {
            var statement = ofxStatement.getAccountStatements().getFirst();
            transactionList = statement.getBankTransactionList();
            accountNumber = statement.getAccountInfo().accountNumber();
        } else if (!ofxStatement.getCreditCardStatements().isEmpty()) {
            var statement = ofxStatement.getCreditCardStatements().getFirst();
            transactionList = ofxStatement.getCreditCardStatements().getFirst().getBankTransactionList();
            accountNumber = statement.getAccountInfo().accountNumber();
        }

        for (StatementTransaction tr : transactionList.transactions()) {
            var builder = new StatementRecord.Builder()
                    .amount(tr.amount().toString())
                    .counterParty(tr.name())
                    .description(tr.memo())
                    .actual(tr.datePosted().toLocalDate())
                    .execution(tr.dateAvailable().toLocalDate());
            records.add(builder.build());
        }

        return new Statement(StatementType.OFX, accountNumber, records);
    }
}
