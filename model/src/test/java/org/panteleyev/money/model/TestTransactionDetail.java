/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTransactionDetail {
    @Test
    public void testInstantiation() {
        var transaction = BaseTestUtils.newTransaction();

        var detail = new TransactionDetail(transaction);

        assertEquals(transaction.uuid(), detail.uuid());
        assertEquals(transaction.accountCreditedUuid(), detail.accountCreditedUuid() );
        assertEquals(transaction.amount(), detail.amount());
        assertEquals(transaction.comment(), detail.comment());
        assertEquals(transaction.modified(), detail.modified());
    }
}
