// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "ExchangeSecurity")
@Table(name = "exchange_security")
public class ExchangeSecurityEntity implements MoneyEntity {
    private UUID uuid;
    private String secId;
    private String name;
    private String shortName;
    private String isin;
    private String regNumber;
    private BigDecimal faceValue;
    private LocalDate issueDate;
    private LocalDate matDate;
    private Integer daysToRedemption;
    private String groupType;
    private String groupName;
    private String type;
    private String typeName;
    private BigDecimal marketValue;
    // Bond specific
    private BigDecimal couponValue;
    private BigDecimal couponPercent;
    private LocalDate couponDate;
    private Integer couponFrequency;
    private BigDecimal accruedInterest;
    private Integer couponPeriod;
    long created;
    long modified;

    public ExchangeSecurityEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public ExchangeSecurityEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    @Column(nullable = false)
    public String getSecId() {
        return secId;
    }

    public ExchangeSecurityEntity setSecId(String secId) {
        this.secId = secId;
        return this;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public ExchangeSecurityEntity setName(String name) {
        this.name = name;
        return this;
    }

    @Column(nullable = false)
    public String getShortName() {
        return shortName;
    }

    public ExchangeSecurityEntity setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    @Column(nullable = false)
    public String getIsin() {
        return isin;
    }

    public ExchangeSecurityEntity setIsin(String isin) {
        this.isin = isin;
        return this;
    }

    @Column(nullable = false)
    public String getRegNumber() {
        return regNumber;
    }

    public ExchangeSecurityEntity setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        return this;
    }

    @Column(name = "face_value", nullable = false)
    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public ExchangeSecurityEntity setFaceValue(BigDecimal faceValue) {
        this.faceValue = faceValue;
        return this;
    }

    @Column(nullable = false)
    public LocalDate getIssueDate() {
        return issueDate;
    }

    public ExchangeSecurityEntity setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public LocalDate getMatDate() {
        return matDate;
    }

    public ExchangeSecurityEntity setMatDate(LocalDate matDate) {
        this.matDate = matDate;
        return this;
    }

    public Integer getDaysToRedemption() {
        return daysToRedemption;
    }

    public ExchangeSecurityEntity setDaysToRedemption(Integer daysToRedemption) {
        this.daysToRedemption = daysToRedemption;
        return this;
    }

    @Column(nullable = false)
    public String getGroupType() {
        return groupType;
    }

    public ExchangeSecurityEntity setGroupType(String groupType) {
        this.groupType = groupType;
        return this;
    }

    @Column(nullable = false)
    public String getGroupName() {
        return groupName;
    }

    public ExchangeSecurityEntity setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    @Column(nullable = false)
    public String getType() {
        return type;
    }

    public ExchangeSecurityEntity setType(String type) {
        this.type = type;
        return this;
    }

    @Column(nullable = false)
    public String getTypeName() {
        return typeName;
    }

    public ExchangeSecurityEntity setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public ExchangeSecurityEntity setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
        return this;
    }

    public BigDecimal getCouponValue() {
        return couponValue;
    }

    public ExchangeSecurityEntity setCouponValue(BigDecimal couponValue) {
        this.couponValue = couponValue;
        return this;
    }

    public BigDecimal getCouponPercent() {
        return couponPercent;
    }

    public ExchangeSecurityEntity setCouponPercent(BigDecimal couponPercent) {
        this.couponPercent = couponPercent;
        return this;
    }

    public LocalDate getCouponDate() {
        return couponDate;
    }

    public ExchangeSecurityEntity setCouponDate(LocalDate couponDate) {
        this.couponDate = couponDate;
        return this;
    }

    public Integer getCouponFrequency() {
        return couponFrequency;
    }

    public ExchangeSecurityEntity setCouponFrequency(Integer couponFrequency) {
        this.couponFrequency = couponFrequency;
        return this;
    }

    public BigDecimal getAccruedInterest() {
        return accruedInterest;
    }

    public ExchangeSecurityEntity setAccruedInterest(BigDecimal accruedInterest) {
        this.accruedInterest = accruedInterest;
        return this;
    }

    public Integer getCouponPeriod() {
        return couponPeriod;
    }

    public ExchangeSecurityEntity setCouponPeriod(Integer couponPeriod) {
        this.couponPeriod = couponPeriod;
        return this;
    }

    @Column(nullable = false)
    public long getCreated() {
        return created;
    }

    public ExchangeSecurityEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    @Column(nullable = false)
    public long getModified() {
        return modified;
    }

    public ExchangeSecurityEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExchangeSecurityEntity that)) return false;
        return created == that.created && modified == that.modified && Objects.equals(uuid,
                that.uuid) && Objects.equals(secId, that.secId) && Objects.equals(name,
                that.name) && Objects.equals(shortName, that.shortName) && Objects.equals(isin,
                that.isin) && Objects.equals(regNumber, that.regNumber) && Objects.equals(faceValue,
                that.faceValue) && Objects.equals(issueDate, that.issueDate) && Objects.equals(matDate,
                that.matDate) && Objects.equals(daysToRedemption,
                that.daysToRedemption) && Objects.equals(groupType, that.groupType) && Objects.equals(groupName,
                that.groupName) && Objects.equals(type, that.type) && Objects.equals(typeName,
                that.typeName) && Objects.equals(marketValue, that.marketValue) && Objects.equals(
                couponValue, that.couponValue) && Objects.equals(couponPercent,
                that.couponPercent) && Objects.equals(couponDate, that.couponDate) && Objects.equals(
                couponFrequency, that.couponFrequency) && Objects.equals(accruedInterest,
                that.accruedInterest) && Objects.equals(couponPeriod, that.couponPeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, secId, name, shortName, isin, regNumber, faceValue, issueDate, matDate,
                daysToRedemption,
                groupType, groupName, type, typeName, marketValue, couponValue, couponPercent, couponDate, couponFrequency,
                accruedInterest, couponPeriod, created, modified);
    }
}
