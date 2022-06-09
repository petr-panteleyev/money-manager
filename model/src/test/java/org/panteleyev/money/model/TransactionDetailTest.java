/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TransactionDetailTest {
    @Test
    public void testInstantiation() {
        var transaction = BaseTestUtils.newTransaction();

        var detail = new TransactionDetail(transaction);

        assertEquals(detail.uuid(), transaction.uuid());
        assertEquals(detail.accountCreditedUuid(), transaction.accountCreditedUuid());
        assertEquals(detail.amount(), transaction.amount());
        assertEquals(detail.comment(), transaction.comment());
        assertEquals(detail.modified(), transaction.modified());
    }
}
