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
import static org.testng.Assert.fail;

public class TestCreditCardStatement {
    private static final String FILE_NAME = "src/test/resources/org/panteleyev/ofx/credit.ofx";
    private static final String FILE_NAME_NO_PENDING = "src/test/resources/org/panteleyev/ofx/credit_no_pending.ofx";
    private static final String FILE_NAME_NO_BANK = "src/test/resources/org/panteleyev/ofx/credit_no_bank.ofx";

    private static final String STATEMENT_CURRENCY = "EUR";
    private static final String ACCOUNT_NUMBER = "12345678901234567890";

    private static final List<StatementTransaction> EXPECTED_TRANSACTIONS = List.of(
        new StatementTransaction(
            TransactionEnum.DEBIT,
            LocalDateTime.of(2018, 12, 6, 12, 0),
            null,
            LocalDateTime.of(2018, 12, 8, 12, 0),
            new BigDecimal("-1084.000"),
            "COMPUTER STORE",
            "COMPUTER STORE MEMO",
            "6163136512"
        ),
        new StatementTransaction(
            TransactionEnum.DEBIT,
            LocalDateTime.of(2018, 12, 6, 12, 0),
            null,
            LocalDateTime.of(2018, 12, 8, 12, 0),
            new BigDecimal("-7260.000"),
            "DRUG STORE",
            "DRUG STORE MEMO",
            "6163136522"
        )
    );

    private static final List<PendingTransaction> EXPECTED_PENDING_TRANSACTIONS = List.of(
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-349.120"),
            "GROCERY STORE",
            "GROCERY STORE MEMO",
            LocalDateTime.of(2018, 12, 7, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-257.000"),
            "TAXI",
            "TAXI",
            LocalDateTime.of(2018, 12, 7, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-97.500"),
            "HOME DEPOT",
            "HOME DEPOT",
            LocalDateTime.of(2018, 12, 7, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-220.000"),
            "CANTINA",
            "CANTINA",
            LocalDateTime.of(2018, 12, 7, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-235.000"),
            "STARBUCKS",
            "STARBUCKS",
            LocalDateTime.of(2018, 12, 7, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-182.070"),
            "ANOTHER STORE",
            "ANOTHER STORE",
            LocalDateTime.of(2018, 12, 6, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-220.000"),
            "CANTINA",
            "CANTINA",
            LocalDateTime.of(2018, 12, 6, 12, 0),
            null
        ),
        new PendingTransaction(
            TransactionEnum.HOLD,
            new BigDecimal("-179.100"),
            "BAKERY",
            "BAKERY",
            LocalDateTime.of(2018, 12, 6, 12, 0),
            null
        )
    );

    @Test
    public void testCreditCardStatementFull() throws Exception {
        try (InputStream in = new FileInputStream(new File(FILE_NAME))) {
            var st = new OFXParser().parse(in);

            assertNotNull(st);

            var accountStatement = st.getAccountStatements();
            assertTrue(accountStatement.isEmpty());

            var creditCardStatement = st.getCreditCardStatements();
            assertNotNull(creditCardStatement);
            assertEquals(creditCardStatement.size(), 1);

            var r1 = creditCardStatement.get(0);
            var statementList = r1.getCreditCardStatementList();
            assertEquals(statementList.size(), 1);

            var statement = statementList.get(0);

            assertEquals(statement.getCurrency(), STATEMENT_CURRENCY);
            var accountInfo = statement.getCreditCardAccountFrom();
            assertEquals(accountInfo.getAccountNumber(), ACCOUNT_NUMBER);

            var bankTransactionList = statement.getBankTransactionList();
            if (!bankTransactionList.isEmpty()) {
                assertEquals(bankTransactionList.getDateStart(),
                    LocalDateTime.of(2018, 12, 6, 12, 0));
                assertEquals(bankTransactionList.getDateEnd(),
                    LocalDateTime.of(2018, 12, 8, 12, 0));
                assertEquals(bankTransactionList.getTransactions(), EXPECTED_TRANSACTIONS);
            } else {
                fail("No bank transaction list present");
            }

            var pendingTransactionList = statement.getPendingTransactionList();
            if (!pendingTransactionList.isEmpty()) {
                assertEquals(pendingTransactionList.getDateAsOf(),
                    LocalDateTime.of(2018, 12, 8, 12, 0));
                assertEquals(pendingTransactionList.getTransactions(), EXPECTED_PENDING_TRANSACTIONS);
            } else {
                fail("No pending transaction list present");
            }
        }
    }

    @Test
    public void testCreditCardStatementNoPending() throws Exception {
        try (InputStream in = new FileInputStream(new File(FILE_NAME_NO_PENDING))) {
            var st = new OFXParser().parse(in);

            assertNotNull(st);

            var accountStatement = st.getAccountStatements();
            assertTrue(accountStatement.isEmpty());

            var creditCardStatement = st.getCreditCardStatements();
            assertNotNull(creditCardStatement);
            assertEquals(creditCardStatement.size(), 1);

            var r1 = creditCardStatement.get(0);
            var statementList = r1.getCreditCardStatementList();
            assertEquals(statementList.size(), 1);

            var statement = statementList.get(0);

            assertEquals(statement.getCurrency(), STATEMENT_CURRENCY);
            var accountInfo = statement.getCreditCardAccountFrom();
            assertEquals(accountInfo.getAccountNumber(), ACCOUNT_NUMBER);

            var bankTransactionList = statement.getBankTransactionList();
            if (!bankTransactionList.isEmpty())  {
                assertEquals(bankTransactionList.getDateStart(),
                    LocalDateTime.of(2018, 12, 6, 12, 0));
                assertEquals(bankTransactionList.getDateEnd(),
                    LocalDateTime.of(2018, 12, 8, 12, 0));
                assertEquals(bankTransactionList.getTransactions(), EXPECTED_TRANSACTIONS);
            } else {
                fail("No bank transaction list present");
            }

            assertTrue(statement.getPendingTransactionList().isEmpty());
        }
    }

    @Test
    public void testCreditCardStatementNoBank() throws Exception {
        try (InputStream in = new FileInputStream(new File(FILE_NAME_NO_BANK))) {
            var st = new OFXParser().parse(in);

            assertNotNull(st);

            var accountStatement = st.getAccountStatements();
            assertTrue(accountStatement.isEmpty());

            var creditCardStatement = st.getCreditCardStatements();
            assertNotNull(creditCardStatement);
            assertEquals(creditCardStatement.size(), 1);

            var r1 = creditCardStatement.get(0);
            var statementList = r1.getCreditCardStatementList();
            assertEquals(statementList.size(), 1);

            var statement = statementList.get(0);

            assertEquals(statement.getCurrency(), STATEMENT_CURRENCY);
            var accountInfo = statement.getCreditCardAccountFrom();
            assertEquals(accountInfo.getAccountNumber(), ACCOUNT_NUMBER);

            assertTrue(statement.getBankTransactionList().isEmpty());

            var pendingTransactionList = statement.getPendingTransactionList();
            if (!pendingTransactionList.isEmpty()) {
                assertEquals(pendingTransactionList.getDateAsOf(),
                    LocalDateTime.of(2018, 12, 8, 12, 0));
                assertEquals(pendingTransactionList.getTransactions(), EXPECTED_PENDING_TRANSACTIONS);
            } else {
                fail("No pending transaction list present");
            }
        }
    }
}
