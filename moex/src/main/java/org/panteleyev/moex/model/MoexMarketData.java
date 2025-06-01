/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

import java.math.BigDecimal;

public record MoexMarketData(
        String secId,
        String boardId,
        BigDecimal open,
        BigDecimal low,
        BigDecimal high,
        BigDecimal last,
        BigDecimal marketPrice,
        BigDecimal marketPriceToday,
        BigDecimal accruedInterest,
        Integer couponPeriod
) {
    public static class Builder {
        private String secId;
        private String boardId;
        private BigDecimal open;
        private BigDecimal low;
        private BigDecimal high;
        private BigDecimal last;
        private BigDecimal marketPrice;
        private BigDecimal marketPriceToday;
        private BigDecimal accruedInterest;
        private Integer couponPeriod;

        public MoexMarketData build() {
            return new MoexMarketData(
                    secId,
                    boardId,
                    open,
                    low,
                    high,
                    last,
                    marketPrice,
                    marketPriceToday,
                    accruedInterest,
                    couponPeriod
            );
        }

        public Builder secId(String secId) {
            this.secId = secId;
            return this;
        }

        public String getSecId() {
            return secId;
        }

        public Builder boardId(String boardId) {
            this.boardId = boardId;
            return this;
        }

        public Builder open(BigDecimal open) {
            this.open = open;
            return this;
        }

        public Builder low(BigDecimal low) {
            this.low = low;
            return this;
        }

        public Builder high(BigDecimal high) {
            this.high = high;
            return this;
        }

        public Builder last(BigDecimal last) {
            this.last = last;
            return this;
        }

        public Builder marketPrice(BigDecimal marketPrice) {
            this.marketPrice = marketPrice;
            return this;
        }

        public Builder marketPriceToday(BigDecimal marketPriceToday) {
            this.marketPriceToday = marketPriceToday;
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
    }
}
