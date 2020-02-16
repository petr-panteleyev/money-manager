package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.testng.annotations.Test;
import static org.panteleyev.money.test.BaseTestUtils.newTransaction;
import static org.testng.Assert.assertEquals;

public class TransactionDetailTest {
    @Test
    public void testInstantiation() {
        var transaction = newTransaction();

        var detail = new TransactionDetail(transaction);

        assertEquals(detail.getUuid(), transaction.getUuid());
        assertEquals(detail.getAccountCreditedUuid(), transaction.getAccountCreditedUuid());
        assertEquals(detail.getAmount(), transaction.getAmount());
        assertEquals(detail.getComment(), transaction.getComment());
        assertEquals(detail.getModified(), transaction.getModified());
    }
}
