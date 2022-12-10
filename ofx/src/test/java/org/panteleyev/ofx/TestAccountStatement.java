/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        try (var in = new FileInputStream(new File(FILE_NAME))) {
            var st = new OFXParser().parse(in);

            assertNotNull(st);

            var accountStatement = st.getAccountStatements();
            assertNotNull(accountStatement);
            assertEquals(1, accountStatement.size());

            var creditCardStatement = st.getCreditCardStatements();
            assertTrue(creditCardStatement.isEmpty());


            var r1 = accountStatement.get(0);
            var statementList = r1.accountStatementList();
            assertEquals(1, statementList.size());

            var statement = statementList.get(0);

            assertEquals(STATEMENT_CURRENCY, statement.currency());
            var accountInfo = statement.bankAccountFrom();
            assertEquals(ACCOUNT_NUMBER, accountInfo.accountNumber());
            assertEquals(BANK_ID, accountInfo.bankId());
            assertEquals(AccountInfo.Type.SAVINGS, accountInfo.type());

            var bankTransactionList = statement.bankTransactionList();

            assertEquals(LocalDateTime.of(2018, 8, 10, 12, 0),
                    bankTransactionList.dateStart());
            assertEquals(LocalDateTime.of(2018, 7, 18, 12, 0),
                    bankTransactionList.dateEnd());
            assertEquals(EXPECTED_TRANSACTIONS, bankTransactionList.transactions());
        }
    }
}
