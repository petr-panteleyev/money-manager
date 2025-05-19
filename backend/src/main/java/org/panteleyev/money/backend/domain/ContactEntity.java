/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "Contact")
@Table(name = "contact")
public class ContactEntity implements MoneyEntity {
    private UUID uuid;
    private String name;
    private String type;
    private String comment;
    private String phone;
    private String mobile;
    private String email;
    private String web;
    private String street;
    private String city;
    private String country;
    private String zip;
    private IconEntity icon;
    private long created;
    private long modified;

    public ContactEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public ContactEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public ContactEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ContactEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ContactEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public ContactEntity setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public ContactEntity setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ContactEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getWeb() {
        return web;
    }

    public ContactEntity setWeb(String web) {
        this.web = web;
        return this;
    }

    public String getStreet() {
        return street;
    }

    public ContactEntity setStreet(String street) {
        this.street = street;
        return this;
    }

    public String getCity() {
        return city;
    }

    public ContactEntity setCity(String city) {
        this.city = city;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public ContactEntity setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getZip() {
        return zip;
    }

    public ContactEntity setZip(String zip) {
        this.zip = zip;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_uuid")
    public IconEntity getIcon() {
        return icon;
    }

    public ContactEntity setIcon(IconEntity icon) {
        this.icon = icon;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public ContactEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public ContactEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContactEntity that)) return false;
        return created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(comment, that.comment)
                && Objects.equals(phone, that.phone)
                && Objects.equals(mobile, that.mobile)
                && Objects.equals(email, that.email)
                && Objects.equals(web, that.web)
                && Objects.equals(street, that.street)
                && Objects.equals(city, that.city)
                && Objects.equals(country, that.country)
                && Objects.equals(zip, that.zip)
                && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, type, comment, phone, mobile, email, web, street, city, country, zip, icon,
                created, modified);
    }
}
