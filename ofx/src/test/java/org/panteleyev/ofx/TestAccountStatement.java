package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
