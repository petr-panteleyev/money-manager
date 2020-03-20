package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.mysqlapi.annotations.Column;
import org.panteleyev.mysqlapi.annotations.ForeignKey;
import org.panteleyev.mysqlapi.annotations.PrimaryKey;
import org.panteleyev.mysqlapi.annotations.ReferenceOption;
import org.panteleyev.mysqlapi.annotations.Table;
import java.util.UUID;

@Table("category")
public record Category(
    @PrimaryKey
    @Column("uuid")
    UUID uuid,
    @Column("name")
    String name,
    @Column("comment")
    String comment,
    @Column("type")
    CategoryType type,
    @Column("icon_uuid")
    @ForeignKey(table = Icon.class, column = "uuid", onDelete = ReferenceOption.SET_NULL)
    UUID iconUuid,
    @Column("created")
    long created,
    @Column("modified")
    long modified

) implements MoneyRecord, Named {

    public static class Builder {
        private String name = "";
        private String comment = "";
        private CategoryType type = null;
        private UUID iconUuid = null;
        private UUID uuid = null;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(Category c) {
            if (c == null) {
                return;
            }

            name = c.name();
            comment = c.comment();
            type = c.type();
            iconUuid = c.iconUuid();
            uuid = c.uuid();
            created = c.created();
            modified = c.modified();
        }

        public UUID getUuid() {
            return uuid;
        }

        public Category build() {
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            return new Category(uuid, name, comment, type, iconUuid, created, modified);
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

        public Builder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public Builder iconUuid(UUID iconUuid) {
            this.iconUuid = iconUuid;
            return this;
        }

        public Builder guid(UUID guid) {
            this.uuid = guid;
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
