/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.panteleyev.money.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

public class TestCategory extends BaseTest {
    @Test
    public void testEquals() {
        var id = randomId();
        var name = UUID.randomUUID().toString();
        var comment = UUID.randomUUID().toString();
        var type = randomCategoryType();
        var uuid = UUID.randomUUID().toString();
        var modified = System.currentTimeMillis();

        var c1 = new Category.Builder(id)
            .name(name)
            .comment(comment)
            .catTypeId(type.getId())
            .guid(uuid)
            .modified(modified)
            .build();
        var c2 = new Category.Builder(id)
            .name(name)
            .comment(comment)
            .catTypeId(type.getId())
            .guid(uuid)
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testBuilder() {
        var original = new Category.Builder(randomId())
            .name(randomString())
            .comment(randomString())
            .catTypeId(randomCategoryType().getId())
            .guid(randomString())
            .modified(System.currentTimeMillis())
            .build();

        var copy = new Category.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Category.Builder()
            .id(original.getId())
            .name(original.getName())
            .comment(original.getComment())
            .catTypeId(original.getCatTypeId())
            .guid(original.getGuid())
            .modified(original.getModified())
            .build();
        assertEquals(manualCopy, original);
    }
}
