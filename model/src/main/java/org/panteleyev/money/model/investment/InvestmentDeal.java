/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.investment;

import org.panteleyev.money.model.MoneyRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentDeal(
        UUID uuid
        , UUID accountUuid
        , UUID securityUuid
        , UUID currencyUuid
        , String dealNumber
        , LocalDateTime dealDate
        , LocalDateTime accountingDate
        , InvestmentMarketType marketType
        , InvestmentOperationType operationType
        , int securityAmount
        , BigDecimal price
        , BigDecimal aci
        , BigDecimal dealVolume
        , BigDecimal rate
        , BigDecimal exchangeFee
        , BigDecimal brokerFee
        , BigDecimal amount
        , InvestmentDealType dealType
        , long created
        , long modified
) implements MoneyRecord {
    public InvestmentDeal {
        var now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }

        price = MoneyRecord.normalize(price, BigDecimal.ZERO);
        aci = MoneyRecord.normalize(aci, BigDecimal.ZERO);
        dealVolume = MoneyRecord.normalize(dealVolume, BigDecimal.ZERO);
        rate = MoneyRecord.normalize(rate, BigDecimal.ONE);
        exchangeFee = MoneyRecord.normalize(exchangeFee, BigDecimal.ONE);
        brokerFee = MoneyRecord.normalize(brokerFee, BigDecimal.ONE);
        amount = MoneyRecord.normalize(amount, BigDecimal.ONE);
    }

    public static class Builder {
        private UUID uuid = UUID.randomUUID();
        private UUID accountUuid;
        private UUID securityUuid;
        private UUID currencyUuid;
        private String dealNumber;
        private LocalDateTime dealDate;
        private LocalDateTime accountingDate;
        private InvestmentMarketType marketType;
        private InvestmentOperationType operationType;
        private int securityAmount;
        private BigDecimal price;
        private BigDecimal aci;
        private BigDecimal dealVolume;
        private BigDecimal rate;
        private BigDecimal exchangeFee;
        private BigDecimal brokerFee;
        private BigDecimal amount;
        private InvestmentDealType dealType;
        private long created = 0;
        private long modified = 0;

        public InvestmentDeal build() {
            return new InvestmentDeal(
                    uuid,
                    accountUuid,
                    securityUuid,
                    currencyUuid,
                    dealNumber,
                    dealDate,
                    accountingDate,
                    marketType,
                    operationType,
                    securityAmount,
                    price,
                    aci,
                    dealVolume,
                    rate,
                    exchangeFee,
                    brokerFee,
                    amount,
                    dealType,
                    created,
                    modified
            );
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder accountUuid(UUID accountUuid) {
            this.accountUuid = accountUuid;
            return this;
        }

        public Builder securityUuid(UUID securityUuid) {
            this.securityUuid = securityUuid;
            return this;
        }

        public Builder currencyUuid(UUID currencyUuid) {
            this.currencyUuid = currencyUuid;
            return this;
        }

        public Builder dealNumber(String dealNumber) {
            this.dealNumber = dealNumber;
            return this;
        }

        public Builder dealDate(LocalDateTime dealDate) {
            this.dealDate = dealDate;
            return this;
        }

        public Builder accountingDate(LocalDateTime accountingDate) {
            this.accountingDate = accountingDate;
            return this;
        }

        public Builder marketType(InvestmentMarketType marketType) {
            this.marketType = marketType;
            return this;
        }

        public Builder operationType(InvestmentOperationType operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder securityAmount(int securityAmount) {
            this.securityAmount = securityAmount;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder aci(BigDecimal aci) {
            this.aci = aci;
            return this;
        }

        public Builder dealVolume(BigDecimal dealVolume) {
            this.dealVolume = dealVolume;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder exchangeFee(BigDecimal exchangeFee) {
            this.exchangeFee = exchangeFee;
            return this;
        }

        public Builder brokerFee(BigDecimal brokerFee) {
            this.brokerFee = brokerFee;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder dealType(InvestmentDealType dealType) {
            this.dealType = dealType;
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
