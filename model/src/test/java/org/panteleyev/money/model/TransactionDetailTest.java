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

        assertEquals(detail.uuid(), transaction.uuid());
        assertEquals(detail.accountCreditedUuid(), transaction.accountCreditedUuid());
        assertEquals(detail.amount(), transaction.amount());
        assertEquals(detail.comment(), transaction.comment());
        assertEquals(detail.modified(), transaction.modified());
    }
}
