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

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "Card")
@Table(name = "card")
public class CardEntity implements MoneyEntity {
    private UUID uuid;
    private AccountEntity account;
    private String type;
    private String number;
    private LocalDate expiration;
    private String comment;
    private boolean enabled;
    private long created;
    private long modified;

    public CardEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public CardEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uuid", nullable = false)
    public AccountEntity getAccount() {
        return account;
    }

    public CardEntity setAccount(AccountEntity account) {
        this.account = account;
        return this;
    }

    public String getType() {
        return type;
    }

    public CardEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public CardEntity setNumber(String number) {
        this.number = number;
        return this;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public CardEntity setExpiration(LocalDate expiration) {
        this.expiration = expiration;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public CardEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CardEntity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public CardEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public CardEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }
}
