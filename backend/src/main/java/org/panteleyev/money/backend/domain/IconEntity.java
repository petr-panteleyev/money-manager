/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "Icon")
@Table(name = "icon")
public class IconEntity implements MoneyEntity {
    private UUID uuid;
    private String name;
    private byte[] bytes;
    private long created;
    private long modified;

    public IconEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public IconEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public IconEntity setName(String name) {
        this.name = name;
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public IconEntity setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public IconEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public IconEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IconEntity that)) return false;
        return created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, created, modified);
    }
}
