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

@Table("contact")
public final class Contact implements MoneyRecord, Named, Comparable<Contact> {
    @PrimaryKey
    @Column("uuid")
    private final UUID guid;

    @Column("name")
    private final String name;

    @Column("type_id")
    private final int typeId;

    @Column("phone")
    private final String phone;

    @Column("mobile")
    private final String mobile;

    @Column("email")
    private final String email;

    @Column("web")
    private final String web;

    @Column("comment")
    private final String comment;

    @Column("street")
    private final String street;

    @Column("city")
    private final String city;

    @Column("country")
    private final String country;

    @Column("zip")
    private final String zip;

    @Column("icon_uuid")
    @ForeignKey(table = Icon.class, column = "uuid", onDelete = ReferenceOption.SET_NULL)
    private final UUID iconUuid;

    @Column("created")
    private final long created;

    @Column("modified")
    private final long modified;

    private final ContactType type;

    @RecordBuilder
    public Contact(@Column("name") String name,
                   @Column("type_id") int typeId,
                   @Column("phone") String phone,
                   @Column("mobile") String mobile,
                   @Column("email") String email,
                   @Column("web") String web,
                   @Column("comment") String comment,
                   @Column("street") String street,
                   @Column("city") String city,
                   @Column("country") String country,
                   @Column("zip") String zip,
                   @Column("icon_uuid") UUID iconUuid,
                   @Column("uuid") UUID guid,
                   @Column("created") long created,
                   @Column("modified") long modified)
    {
        this.name = name;
        this.typeId = typeId;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
        this.web = web;
        this.comment = comment;
        this.street = street;
        this.city = city;
        this.country = country;
        this.zip = zip;
        this.iconUuid = iconUuid;
        this.guid = guid;
        this.created = created;
        this.modified = modified;
        this.type = ContactType.get(this.typeId);
    }

    public final ContactType getType() {
        return type;
    }

    @Override
    public int compareTo(Contact other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public String getName() {
        return name;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getPhone() {
        return phone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getWeb() {
        return web;
    }

    public String getComment() {
        return comment;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
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
        return Objects.hash(name, typeId, phone, mobile, email, web, comment, street, city, country, zip, iconUuid,
            guid, created, modified);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Contact)) {
            return false;
        }

        Contact that = (Contact) other;
        return Objects.equals(name, that.name)
            && typeId == that.typeId
            && Objects.equals(phone, that.phone)
            && Objects.equals(mobile, that.mobile)
            && Objects.equals(email, that.email)
            && Objects.equals(web, that.web)
            && Objects.equals(comment, that.comment)
            && Objects.equals(street, that.street)
            && Objects.equals(city, that.city)
            && Objects.equals(country, that.country)
            && Objects.equals(zip, that.zip)
            && Objects.equals(iconUuid, that.iconUuid)
            && Objects.equals(guid, that.guid)
            && created == that.created
            && modified == that.modified;
    }

    public static final class Builder {
        private String name = "";
        private int typeId = ContactType.PERSONAL.getId();
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
        private UUID guid = null;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(Contact c) {
            if (c == null) {
                return;
            }

            name = c.getName();
            typeId = c.getTypeId();
            phone = c.getPhone();
            mobile = c.getMobile();
            email = c.getEmail();
            web = c.getWeb();
            comment = c.getComment();
            street = c.getStreet();
            city = c.getCity();
            country = c.getCountry();
            zip = c.getZip();
            iconUuid = c.getIconUuid();
            guid = c.getUuid();
            created = c.getCreated();
            modified = c.getModified();
        }

        public UUID getUuid() {
            return guid;
        }

        public Contact build() {
            if (name.isBlank()) {
                throw new IllegalStateException("Name must not be empty");
            }

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

            return new Contact(name, typeId, phone, mobile, email, web, comment, street, city, country, zip, iconUuid,
                guid, created, modified);
        }

        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Contact name must not be empty");
            }
            this.name = name;
            return this;
        }

        public Builder typeId(int typeId) {
            this.typeId = typeId;
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
