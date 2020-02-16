package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.mysqlapi.annotations.Column;
import org.panteleyev.mysqlapi.annotations.ForeignKey;
import org.panteleyev.mysqlapi.annotations.PrimaryKey;
import org.panteleyev.mysqlapi.annotations.RecordBuilder;
import org.panteleyev.mysqlapi.annotations.ReferenceOption;
import org.panteleyev.mysqlapi.annotations.Table;
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
