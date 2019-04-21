/*
 * Copyright (c) 2018, 2019, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.ofx.BankTransactionList;
import org.panteleyev.ofx.OFXParser;
import org.panteleyev.ofx.StatementTransaction;
import java.io.InputStream;
import java.util.ArrayList;

class RBAParser {
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
