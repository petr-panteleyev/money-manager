/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import java.util.UUID;

public record Contact(
    UUID uuid,
    String name,
    ContactType type,
    String phone,
    String mobile,
    String email,
    String web,
    String comment,
    String street,
    String city,
    String country,
    String zip,
    UUID iconUuid,
    long created,
    long modified
) implements MoneyRecord, Named, Comparable<Contact> {

    public Contact {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Contact name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalStateException("Contact type cannot be null");
        }
        if (created == 0) {
            created = System.currentTimeMillis();
        }
        if (modified == 0) {
            modified = System.currentTimeMillis();
        }

        phone = MoneyRecord.normalize(phone);
        mobile = MoneyRecord.normalize(mobile);
        email = MoneyRecord.normalize(email);
        web = MoneyRecord.normalize(web);
        comment = MoneyRecord.normalize(comment);
        street = MoneyRecord.normalize(street);
        city = MoneyRecord.normalize(city);
        country = MoneyRecord.normalize(country);
        zip = MoneyRecord.normalize(zip);

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

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
            return new Contact(uuid, name, type, phone, mobile, email, web, comment, street, city, country, zip,
                iconUuid, created, modified);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(ContactType type) {
            this.type = type;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder web(String web) {
            this.web = web;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip;
            return this;
        }

        public Builder iconUuid(UUID iconUuid) {
            this.iconUuid = iconUuid;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
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
