/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.exchange;

import org.panteleyev.money.model.MoneyRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record ExchangeSecurity(
        UUID uuid,
        String secId,
        String name,
        String shortName,
        String isin,
        String regNumber,
        BigDecimal faceValue,
        LocalDate issueDate,
        LocalDate matDate,
        Integer daysToRedemption,
        String group,
        String groupName,
        String type,
        String typeName,
        BigDecimal marketValue,
        // Bond specific
        BigDecimal couponValue,
        BigDecimal couponPercent,
        LocalDate couponDate,
        Integer couponFrequency,
        BigDecimal accruedInterest,
        Integer couponPeriod,
        long created,
        long modified
) implements MoneyRecord {
    public ExchangeSecurity {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        var now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public Optional<LocalDate> getMatDate() {
        return Optional.ofNullable(matDate);
    }

    public Optional<BigDecimal> getCouponValue() {
        return Optional.ofNullable(couponValue);
    }

    public Optional<BigDecimal> getCouponPercent() {
        return Optional.ofNullable(couponPercent);
    }

    public Optional<LocalDate> getCouponDate() {
        return Optional.ofNullable(couponDate);
    }

    public Optional<BigDecimal> getAccruedInterest() {
        return Optional.ofNullable(accruedInterest);
    }

    public Optional<Integer> getCouponPeriod() {
        return Optional.ofNullable(couponPeriod);
    }

    public Optional<Integer> getDaysToRedemption() {
        return Optional.ofNullable(daysToRedemption);
    }

    public static class Builder {
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
        private String group;
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
        private long created;
        private long modified;

        public Builder() {
        }

        public Builder(ExchangeSecurity security) {
            if (security == null) {
                return;
            }

            this.uuid = security.uuid();
            this.secId = security.secId();
            this.name = security.name();
            this.shortName = security.shortName();
            this.isin = security.isin();
            this.regNumber = security.regNumber();
            this.faceValue = security.faceValue();
            this.issueDate = security.issueDate();
            this.matDate = security.matDate();
            this.daysToRedemption = security.daysToRedemption();
            this.group = security.group();
            this.groupName = security.groupName();
            this.type = security.type();
            this.typeName = security.typeName();
            this.couponValue = security.couponValue();
            this.couponPercent = security.couponPercent();
            this.couponDate = security.couponDate();
            this.couponFrequency = security.couponFrequency();
            this.accruedInterest = security.accruedInterest();
            this.couponPeriod = security.couponPeriod();
            this.created = security.created();
            this.modified = security.modified();
        }

        public ExchangeSecurity build() {
            return new ExchangeSecurity(
                    uuid,
                    secId,
                    name,
                    shortName,
                    isin,
                    regNumber,
                    faceValue,
                    issueDate,
                    matDate,
                    daysToRedemption,
                    group,
                    groupName,
                    type,
                    typeName,
                    marketValue,
                    couponValue,
                    couponPercent,
                    couponDate,
                    couponFrequency,
                    accruedInterest,
                    couponPeriod,
                    created,
                    modified
            );
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder secId(String secId) {
            this.secId = secId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder isin(String isin) {
            this.isin = isin;
            return this;
        }

        public Builder regNumber(String regNumber) {
            this.regNumber = regNumber;
            return this;
        }

        public Builder faceValue(BigDecimal faceValue) {
            this.faceValue = faceValue;
            return this;
        }

        public Builder issueDate(LocalDate issueDate) {
            this.issueDate = issueDate;
            return this;
        }

        public Builder matDate(LocalDate matDate) {
            this.matDate = matDate;
            return this;
        }

        public Builder daysToRedemption(Integer daysToRedemption) {
            this.daysToRedemption = daysToRedemption;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public Builder marketValue(BigDecimal marketValue) {
            this.marketValue = marketValue;
            return this;
        }

        public Builder couponValue(BigDecimal couponValue) {
            this.couponValue = couponValue;
            return this;
        }

        public Builder couponPercent(BigDecimal couponPercent) {
            this.couponPercent = couponPercent;
            return this;
        }

        public Builder couponDate(LocalDate couponDate) {
            this.couponDate = couponDate;
            return this;
        }

        public Builder couponFrequency(Integer couponFrequency) {
            this.couponFrequency = couponFrequency;
            return this;
        }

        public Builder accruedInterest(BigDecimal accruedInterest) {
            this.accruedInterest = accruedInterest;
            return this;
        }

        public Builder couponPeriod(Integer couponPeriod) {
            this.couponPeriod = couponPeriod;
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
