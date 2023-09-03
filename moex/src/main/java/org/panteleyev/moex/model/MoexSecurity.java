/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.panteleyev.moex.xml.ParserUtil.parseDate;
import static org.panteleyev.moex.xml.ParserUtil.parseNumber;
import static org.panteleyev.moex.xml.ParserUtil.parseInt;

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
        private BigDecimal couponValue = BigDecimal.ZERO;
        private BigDecimal couponPercent = BigDecimal.ZERO;
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

        public Builder faceValue(String faceValue) {
            this.faceValue = new BigDecimal(faceValue);
            return this;
        }

        public Builder issueDate(String issueDate) {
            this.issueDate = parseDate(issueDate);
            return this;
        }

        public Builder matDate(String matDate) {
            this.matDate = parseDate(matDate);
            return this;
        }

        public Builder daysToRedemption(String daysToRedemption) {
            try {
                this.daysToRedemption = Integer.parseInt(daysToRedemption);
            } catch (Exception ex) {
                this.daysToRedemption = null;
            }
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

        public Builder couponValue(String couponValue) {
            this.couponValue = parseNumber(couponValue);
            return this;
        }

        public Builder couponPercent(String couponPercent) {
            this.couponPercent = parseNumber(couponPercent);
            return this;
        }

        public Builder couponDate(String couponDate) {
            this.couponDate = parseDate(couponDate);
            return this;
        }

        public Builder couponFrequency(String couponFrequency) {
            this.couponFrequency = parseInt(couponFrequency);
            return this;
        }
    }
}
