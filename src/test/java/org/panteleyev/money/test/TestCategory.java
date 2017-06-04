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

import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.UUID;

public class TestCategory extends BaseTest {
    @Test
    public void testBuilder() throws Exception {
        Category original = newCategory();

        Category.Builder builder = new Category.Builder(original);
        Category newCategory = builder.build();
        Assert.assertEquals(newCategory, original);
        Assert.assertEquals(newCategory.hashCode(), original.hashCode());

        Category.Builder emptyBuilder = new Category.Builder()
                .id(original.getId())
                .name(original.getName())
                .comment(original.getComment())
                .type(original.getType())
                .expanded(original.isExpanded());

        Assert.assertEquals(emptyBuilder.id(), original.getId());

        newCategory = emptyBuilder.build();
        Assert.assertEquals(newCategory, original);
        Assert.assertEquals(newCategory.hashCode(), original.hashCode());
    }

    @Test
    public void testEquals() {
        Integer id = RANDOM.nextInt();
        String name = UUID.randomUUID().toString();
        String comment = UUID.randomUUID().toString();
        CategoryType type = randomCategoryType();
        Boolean expanded = RANDOM.nextBoolean();

        Category c1 = new Category(id, name, comment, type.getId(), expanded);
        Category c2 = new Category(id, name, comment, type.getId(), expanded);

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testBuilderNullId() {
        Category.Builder builder = new Category.Builder(newCategory());
        builder.id(0).build();
    }
}
