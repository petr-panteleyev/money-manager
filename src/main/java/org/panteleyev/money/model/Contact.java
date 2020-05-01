package org.panteleyev.money.model;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.mysqlapi.annotations.Column;
import org.panteleyev.mysqlapi.annotations.ForeignKey;
import org.panteleyev.mysqlapi.annotations.PrimaryKey;
import org.panteleyev.mysqlapi.annotations.ReferenceOption;
import org.panteleyev.mysqlapi.annotations.Table;
import java.util.UUID;

@Table("contact")
public record Contact(
    @PrimaryKey
    @Column("uuid")
    UUID uuid,
    @Column("name")
    String name,
    @Column("type")
    ContactType type,
    @Column("phone")
    String phone,
    @Column("mobile")
    String mobile,
    @Column("email")
    String email,
    @Column("web")
    String web,
    @Column("comment")
    String comment,
    @Column("street")
    String street,
    @Column("city")
    String city,
    @Column("country")
    String country,
    @Column("zip")
    String zip,
    @Column("icon_uuid")
    @ForeignKey(table = Icon.class, column = "uuid", onDelete = ReferenceOption.SET_NULL)
    UUID iconUuid,
    @Column("created")
    long created,
    @Column("modified")
    long modified

) implements MoneyRecord, Named, Comparable<Contact> {

    @Override
    public int compareTo(Contact other) {
        return name.compareToIgnoreCase(other.name);
    }

    public static final class Builder {
        private String name = "";
        private ContactType type = ContactType.PERSONAL;
        private String phone = "";
        private String mobile = "";
        private String email = "";
        private String web = "";
        private String comment = "";
        private String street = "";
        private String city = "";
        private String country = "";
        private String zip = "";
        private UUID iconUuid = null;
        private UUID uuid = null;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(Contact c) {
            if (c == null) {
                return;
            }

            name = c.name();
            type = c.type();
            phone = c.phone();
            mobile = c.mobile();
            email = c.email();
            web = c.web();
            comment = c.comment();
            street = c.street();
            city = c.city();
            country = c.country();
            zip = c.zip();
            iconUuid = c.iconUuid();
            uuid = c.uuid();
            created = c.created();
            modified = c.modified();
        }

        public UUID getUuid() {
            return uuid;
        }

        public Contact build() {
            if (name.isBlank()) {
                throw new IllegalStateException("Name must not be empty");
            }

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

            return new Contact(uuid, name, type, phone, mobile, email, web, comment, street, city, country, zip,
                iconUuid, created, modified);
        }

        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Contact name must not be empty");
            }
            this.name = name;
            return this;
        }

        public Builder type(ContactType type) {
            this.type = type;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone == null ? "" : phone;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile == null ? "" : mobile;
            return this;
        }

        public Builder email(String email) {
            this.email = email == null ? "" : email;
            return this;
        }

        public Builder web(String web) {
            this.web = web == null ? "" : web;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment == null ? "" : comment;
            return this;
        }

        public Builder street(String street) {
            this.street = street == null ? "" : street;
            return this;
        }

        public Builder city(String city) {
            this.city = city == null ? "" : city;
            return this;
        }

        public Builder country(String country) {
            this.country = country == null ? "" : country;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip == null ? "" : zip;
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
