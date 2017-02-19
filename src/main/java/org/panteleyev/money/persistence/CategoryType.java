/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence;

import java.util.Arrays;
import java.util.ResourceBundle;

public enum CategoryType implements Named {
    BANKS_AND_CASH(1),
    INCOMES(2),
    EXPENSES(3),
    DEBTS(4),
    PORTFOLIO(5),
    ASSETS(6),
    STARTUP(7);

    private static final String BUNDLE = "org.panteleyev.money.persistence.CategoryType";

    private final Integer id;
    private final String name;
    private final String comment;

    CategoryType(int id) {
        ResourceBundle b = ResourceBundle.getBundle(BUNDLE);

        this.id = id;
        this.name = b.getString("name" + id);
        this.comment = b.getString("comment" + id);
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public static CategoryType get(int id) {
        return Arrays.stream(values())
                .filter(v -> v.getId() == id)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
