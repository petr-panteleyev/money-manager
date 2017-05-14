/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
package org.panteleyev.money.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.panteleyev.money.persistence.TransactionGroup;

public class TestTransactionGroup extends BaseTest {
    @Test
    public void testBuilder() throws Exception {
        TransactionGroup original = newTransactionGroup();

        TransactionGroup.Builder builder = new TransactionGroup.Builder(original);
        TransactionGroup newTransactionGroup = builder.build();
        Assert.assertEquals(newTransactionGroup, original);
        Assert.assertEquals(newTransactionGroup.hashCode(), original.hashCode());

        TransactionGroup.Builder emptyBuilder = new TransactionGroup.Builder()
                .id(original.getId())
                .day(original.getDay())
                .month(original.getMonth())
                .year(original.getYear())
                .expanded(original.isExpanded());

        newTransactionGroup = emptyBuilder.build();
        Assert.assertEquals(newTransactionGroup, original);
        Assert.assertEquals(newTransactionGroup.hashCode(), original.hashCode());
    }

    @Test
    public void testEquals() {
        Integer id = RANDOM.nextInt();
        Integer day = RANDOM.nextInt();
        Integer month = RANDOM.nextInt();
        Integer year = RANDOM.nextInt();
        Boolean expanded = RANDOM.nextBoolean();

        TransactionGroup t1 = new TransactionGroup(id, day, month, year, expanded);
        TransactionGroup t2 = new TransactionGroup(id, day, month, year, expanded);

        Assert.assertEquals(t1, t2);
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testBuilderNullId() {
        TransactionGroup.Builder builder = new TransactionGroup.Builder(newTransactionGroup());
        builder.id(0).build();
    }

}
