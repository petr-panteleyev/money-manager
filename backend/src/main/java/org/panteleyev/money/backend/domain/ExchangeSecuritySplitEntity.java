// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.panteleyev.money.dto.ExchangeSecuritySplitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "ExchangeSecuritySplit")
@Table(name = "exchange_security_split")
public class ExchangeSecuritySplitEntity {
    private UUID uuid;
    private UUID securityUuid;
    private ExchangeSecuritySplitType splitType;
    private LocalDate splitDate;
    private BigDecimal rate;
    private String comment;
    private long created;
    private long modified;

    public ExchangeSecuritySplitEntity() {
    }

    @Id
    @Column(nullable = false)
    public UUID getUuid() {
        return uuid;
    }

    public ExchangeSecuritySplitEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    @Column(nullable = false)
    public UUID getSecurityUuid() {
        return securityUuid;
    }

    public ExchangeSecuritySplitEntity setSecurityUuid(UUID securityUuid) {
        this.securityUuid = securityUuid;
        return this;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public ExchangeSecuritySplitType getSplitType() {
        return splitType;
    }

    public ExchangeSecuritySplitEntity setSplitType(ExchangeSecuritySplitType splitType) {
        this.splitType = splitType;
        return this;
    }

    @Column(nullable = false)
    public LocalDate getSplitDate() {
        return splitDate;
    }

    public ExchangeSecuritySplitEntity setSplitDate(LocalDate splitDate) {
        this.splitDate = splitDate;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getRate() {
        return rate;
    }

    public ExchangeSecuritySplitEntity setRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    @Column(nullable = false)
    public String getComment() {
        return comment;
    }

    public ExchangeSecuritySplitEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Column(nullable = false)
    public long getCreated() {
        return created;
    }

    public ExchangeSecuritySplitEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    @Column(nullable = false)
    public long getModified() {
        return modified;
    }

    public ExchangeSecuritySplitEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExchangeSecuritySplitEntity that)) return false;
        return created == that.created && modified == that.modified && Objects.equals(uuid,
                that.uuid) && Objects.equals(securityUuid, that.securityUuid) && Objects.equals(
                splitType, that.splitType) && Objects.equals(splitDate,
                that.splitDate) && Objects.equals(rate, that.rate) && Objects.equals(comment,
                that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, securityUuid, splitType, splitDate, rate, comment, created, modified);
    }
}
