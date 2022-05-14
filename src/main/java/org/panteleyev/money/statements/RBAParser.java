/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
            accountNumber = statement.getAccountInfo().accountNumber();
        } else if (!ofxStatement.getCreditCardStatements().isEmpty()) {
            var statement = ofxStatement.getCreditCardStatements().get(0);
            transactionList = ofxStatement.getCreditCardStatements().get(0).getBankTransactionList();
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

        return new Statement(Statement.StatementType.RAIFFEISEN_OFX, accountNumber, records);
    }
}
