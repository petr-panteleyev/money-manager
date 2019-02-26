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

import java.util.Objects;
import java.util.UUID;

public final class Category implements MoneyRecord, Named {
    private final int id;
    private final String name;
    private final String comment;
    private final int catTypeId;
    private final String guid;
    private final long modified;

    private final CategoryType type;

    private Category(int id, String name, String comment, int catTypeId, String guid, long modified) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.catTypeId = catTypeId;
        this.guid = guid;
        this.modified = modified;

        this.type = CategoryType.get(this.catTypeId);
    }

    public Category copy(String newName, String newComment, int newCatTypeId) {
        return new Builder(this)
            .name(newName)
            .comment(newComment)
            .catTypeId(newCatTypeId)
            .modified(System.currentTimeMillis())
            .build();
    }

    public CategoryType getType() {
        return type;
    }

    public Category copy(int newId) {
        return new Builder(this).id(newId).build();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public int getCatTypeId() {
        return catTypeId;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, comment, catTypeId, guid, modified);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Category)) {
            return false;
        }

        Category that = (Category) other;

        return this.id == that.id
            && Objects.equals(this.name, that.name)
            && Objects.equals(this.comment, that.comment)
            && this.catTypeId == that.catTypeId
            && Objects.equals(this.guid, that.guid)
            && this.modified == that.modified;
    }

    public static class Builder {
        private int id = 0;
        private String name = "";
        private String comment = "";
        private int catTypeId = 0;
        private String guid = null;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(int id) {
            this.id = id;
        }

        public Builder(Category c) {
            if (c == null) {
                return;
            }

            id = c.getId();
            name = c.getName();
            comment = c.getComment();
            catTypeId = c.getCatTypeId();
            guid = c.getGuid();
            modified = c.getModified();
        }

        public Category build() {
            if (guid == null) {
                guid = UUID.randomUUID().toString();
            }

            if (modified == 0) {
                modified = System.currentTimeMillis();
            }

            return new Category(id, name, comment, catTypeId, guid, modified);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Category name must not be empty");
            }
            this.name = name;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment == null ? "" : comment;
            return this;
        }

        public Builder catTypeId(int catTypeId) {
            this.catTypeId = catTypeId;
            return this;
        }

        public Builder guid(String guid) {
            this.guid = guid;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
