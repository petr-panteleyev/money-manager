/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence.model;

import org.panteleyev.money.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.panteleyev.money.persistence.PersistenceTestUtils.randomCategoryType;

public class TestAccount extends BaseTest {
    @Test
    public void testEquals() {
        int id = randomId();
        String name = UUID.randomUUID().toString();
        String comment = UUID.randomUUID().toString();
        BigDecimal opening = randomBigDecimal();
        BigDecimal limit = randomBigDecimal();
        BigDecimal rate = randomBigDecimal();
        CategoryType type = randomCategoryType();
        int categoryId = randomId();
        int currencyId = randomId();
        boolean enabled = RANDOM.nextBoolean();
        String uuid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Account a1 = new Account(id, name, comment, opening, limit, rate, type.getId(), categoryId, currencyId,
                enabled, uuid, modified);
        Account a2 = new Account(id, name, comment, opening, limit, rate, type.getId(), categoryId, currencyId,
                enabled, uuid, modified);

        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());
    }
}
