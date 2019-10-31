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

package org.panteleyev.ofx;

import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestAccountStatement {
    private static final String FILE_NAME = "src/test/resources/org/panteleyev/ofx/account.ofx";

    private static final String STATEMENT_CURRENCY = "USD";
    private static final String BANK_ID = "ABCDEFGH";
    private static final String ACCOUNT_NUMBER = "12345678901234567890";

    private static final List<StatementTransaction> EXPECTED_TRANSACTIONS = List.of(
        new StatementTransaction(
            TransactionEnum.CREDIT,
            LocalDateTime.of(2018, 8, 10, 12, 0),
            null,
            LocalDateTime.of(2018, 8, 10, 12, 0),
            new BigDecimal("1000.00"),
            "SALARY",
            "SALARY",
            "1234"
        ),
        new StatementTransaction(
            TransactionEnum.DEBIT,
            LocalDateTime.of(2018, 8, 10, 12, 0),
            null,
            LocalDateTime.of(2018, 8, 10, 12, 0),
            new BigDecimal("-901.0000"),
            "Taxes",
            "Property taxes",
            "5678"
        ),
        new StatementTransaction(
            TransactionEnum.DEBIT,
            LocalDateTime.of(2018, 8, 9, 12, 0),
            null,
            LocalDateTime.of(2018, 8, 9, 12, 0),
            new BigDecimal("-32009.6500"),
            "Transfer",
            "Wire transfer",
            "1122"
        )
    );

    @Test
    public void testAccountStatement() throws Exception {
        try (InputStream in = new FileInputStream(new File(FILE_NAME))) {
            var st = new OFXParser().parse(in);

            assertNotNull(st);

            var accountStatement = st.getAccountStatements();
            assertNotNull(accountStatement);
            assertEquals(accountStatement.size(), 1);

            var creditCardStatement = st.getCreditCardStatements();
            assertTrue(creditCardStatement.isEmpty());


            var r1 = accountStatement.get(0);
            var statementList = r1.getAccountStatementList();
            assertEquals(statementList.size(), 1);

            var statement = statementList.get(0);

            assertEquals(statement.getCurrency(), STATEMENT_CURRENCY);
            var accountInfo = statement.getBankAccountFrom();
            assertEquals(accountInfo.getAccountNumber(), ACCOUNT_NUMBER);
            assertEquals(accountInfo.getBankId(), BANK_ID);
            assertEquals(accountInfo.getType(), AccountInfo.Type.SAVINGS);

            var bankTransactionList = statement.getBankTransactionList();

            assertEquals(bankTransactionList.getDateStart(),
                LocalDateTime.of(2018, 8, 10, 12, 0));
            assertEquals(bankTransactionList.getDateEnd(),
                LocalDateTime.of(2018, 7, 18, 12, 0));
            assertEquals(bankTransactionList.getTransactions(), EXPECTED_TRANSACTIONS);
        }
    }

}
