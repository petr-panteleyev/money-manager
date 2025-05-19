/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "ExchangeSecuritySplit")
@Table(name = "exchange_security_split")
public class ExchangeSecuritySplitEntity {
    private UUID uuid;
    private UUID securityUuid;
    private String splitType;
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

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(nullable = false)
    public UUID getSecurityUuid() {
        return securityUuid;
    }

    public void setSecurityUuid(UUID securityUuid) {
        this.securityUuid = securityUuid;
    }

    @Column(nullable = false)
    public String getSplitType() {
        return splitType;
    }

    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }

    @Column(nullable = false)
    public LocalDate getSplitDate() {
        return splitDate;
    }

    public void setSplitDate(LocalDate splitDate) {
        this.splitDate = splitDate;
    }

    @Column(nullable = false)
    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Column(nullable = false)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(nullable = false)
    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Column(nullable = false)
    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }
}
