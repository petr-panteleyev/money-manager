/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.model;

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.ForeignKey;
import org.panteleyev.persistence.annotations.PrimaryKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.ReferenceOption;
import org.panteleyev.persistence.annotations.Table;
import java.util.Objects;
import java.util.UUID;

@Table("category")
public final class Category implements MoneyRecord, Named {
    @PrimaryKey
    @Column("uuid")
    private final UUID guid;

    @Column("name")
    private final String name;

    @Column("comment")
    private final String comment;

    @Column("cat_type_id")
    private final int catTypeId;

    @Column("icon_uuid")
    @ForeignKey(table = Icon.class, column = "uuid", onDelete = ReferenceOption.SET_NULL)
    private final UUID iconUuid;

    @Column("created")
    private final long created;

    @Column("modified")
    private final long modified;

    private final CategoryType type;

    @RecordBuilder
    public Category(@Column("name") String name,
                    @Column("comment") String comment,
                    @Column("cat_type_id") int catTypeId,
                    @Column("icon_uuid") UUID iconUuid,
                    @Column("uuid") UUID guid,
                    @Column("created") long created,
                    @Column("modified") long modified)
    {
        this.name = name;
        this.comment = comment;
        this.catTypeId = catTypeId;
        this.iconUuid = iconUuid;
        this.guid = guid;
        this.created = created;
        this.modified = modified;

        this.type = CategoryType.get(this.catTypeId);
    }

    public CategoryType getType() {
        return type;
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

    public UUID getIconUuid() {
        return iconUuid;
    }

    @Override
    public UUID getUuid() {
        return guid;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, catTypeId, iconUuid, guid, created, modified);
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

        return Objects.equals(this.name, that.name)
            && Objects.equals(this.comment, that.comment)
            && this.catTypeId == that.catTypeId
            && Objects.equals(this.iconUuid, that.iconUuid)
            && Objects.equals(this.guid, that.guid)
            && this.created == that.created
            && this.modified == that.modified;
    }

    public static class Builder {
        private String name = "";
        private String comment = "";
        private int catTypeId = 0;
        private UUID iconUuid = null;
        private UUID guid = null;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(Category c) {
            if (c == null) {
                return;
            }

            name = c.getName();
            comment = c.getComment();
            catTypeId = c.getCatTypeId();
            iconUuid = c.getIconUuid();
            guid = c.getUuid();
            created = c.getCreated();
            modified = c.getModified();
        }

        public UUID getUuid() {
            return guid;
        }

        public Category build() {
            if (guid == null) {
                guid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            return new Category(name, comment, catTypeId, iconUuid, guid, created, modified);
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

        public Builder iconUuid(UUID iconUuid) {
            this.iconUuid = iconUuid;
            return this;
        }

        public Builder guid(UUID guid) {
            this.guid = guid;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
