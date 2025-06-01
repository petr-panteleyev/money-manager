/*
 Copyright © 2023-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MoexSecurity(
        String secId,
        String engine,
        String market,
        String primaryBoard,
        String name,
        String shortName,
        String isin,
        String regNumber,
        BigDecimal faceValue,
        LocalDate issueDate,
        LocalDate matDate,
        Integer daysToRedemption,
        String type,
        String typeName,
        String group,
        String groupName,
        // Bond specific
        BigDecimal couponValue,
        BigDecimal couponPercent,
        LocalDate couponDate,
        Integer couponFrequency
) {
    public static class Builder {
        private String secId = "";
        private String engine = "";
        private String market = "";
        private String primaryBoard = "";
        private String name = "";
        private String shortName = "";
        private String isin = "";
        private String regNumber = "";
        private BigDecimal faceValue = BigDecimal.ZERO;
        private LocalDate issueDate = null;
        private LocalDate matDate = null;
        private Integer daysToRedemption = null;
        private String type = "";
        private String typeName = "";
        private String group = "";
        private String groupName = "";
        private BigDecimal couponValue = null;
        private BigDecimal couponPercent = null;
        private LocalDate couponDate = null;
        private Integer couponFrequency = null;


        public MoexSecurity build() {
            return new MoexSecurity(
                    secId,
                    engine,
                    market,
                    primaryBoard,
                    name,
                    shortName,
                    isin,
                    regNumber,
                    faceValue,
                    issueDate,
                    matDate,
                    daysToRedemption,
                    type,
                    typeName,
                    group,
                    groupName,
                    couponValue,
                    couponPercent,
                    couponDate,
                    couponFrequency
            );
        }

        public Builder secId(String secId) {
            this.secId = secId;
            return this;
        }

        public String secId() {
            return secId;
        }

        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        public Builder market(String market) {
            this.market = market;
            return this;
        }

        public Builder primaryBoard(String primaryBoard) {
            this.primaryBoard = primaryBoard;
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

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder typeName(String typeName) {
            this.typeName = typeName;
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
    }
}
