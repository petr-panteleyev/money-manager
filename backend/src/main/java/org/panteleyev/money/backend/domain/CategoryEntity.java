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

@Entity(name = "Category")
@Table(name = "category")
public class CategoryEntity implements MoneyEntity {
    private UUID uuid;
    private String name;
    private String comment;
    private String type;
    private IconEntity icon;
    private long created;
    private long modified;

    public CategoryEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public CategoryEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public CategoryEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public CategoryEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getType() {
        return type;
    }

    public CategoryEntity setType(String type) {
        this.type = type;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_uuid")
    public IconEntity getIcon() {
        return icon;
    }

    public CategoryEntity setIcon(IconEntity icon) {
        this.icon = icon;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public CategoryEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public CategoryEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CategoryEntity that)) return false;
        return created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(comment, that.comment)
                && type == that.type
                && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, comment, type, icon, created, modified);
    }
}
