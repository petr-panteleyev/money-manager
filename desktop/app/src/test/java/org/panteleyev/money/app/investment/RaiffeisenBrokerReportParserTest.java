/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentDealType;
import org.panteleyev.money.model.investment.InvestmentMarketType;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.app.GlobalContext.cache;

public class RaiffeisenBrokerReportParserTest {
    private static final String REPORT = "/org/panteleyev/money/app/investment/Test_Raiffeisen_Broker_Report.xlsx";

    private static final List<InvestmentDeal> EXPECTED = List.of(
            new InvestmentDeal.Builder()
                    .dealNumber("57924757")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 16, 25))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(10)
                    .price(new BigDecimal("280.02"))
                    .aci(new BigDecimal("0.00"))
                    .dealVolume(new BigDecimal("2800.2"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.84"))
                    .brokerFee(new BigDecimal("8.40"))
                    .amount(new BigDecimal("2790.96"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("13253446")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 20, 49))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.PURCHASE)
                    .securityAmount(32)
                    .price(new BigDecimal("88.55"))
                    .aci(new BigDecimal("55.36"))
                    .dealVolume(new BigDecimal("28391.36"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("2.83"))
                    .brokerFee(new BigDecimal("0"))
                    .amount(new BigDecimal("28394.19"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("68743287")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 15, 51))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(1)
                    .price(new BigDecimal("577.6"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("577.6"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.17"))
                    .brokerFee(new BigDecimal("1.73"))
                    .amount(new BigDecimal("575.7"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("12127213")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 17, 5))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(20)
                    .price(new BigDecimal("76.55"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("1531"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.46"))
                    .brokerFee(new BigDecimal("4.59"))
                    .amount(new BigDecimal("1525.95"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("28856197")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 16, 49))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(70)
                    .price(new BigDecimal("274.86"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("19240.20"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("5.77"))
                    .brokerFee(new BigDecimal("57.72"))
                    .amount(new BigDecimal("19176.71"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("52536606")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 15, 33))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(1)
                    .price(new BigDecimal("1645"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("1645"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.49"))
                    .brokerFee(new BigDecimal("4.94"))
                    .amount(new BigDecimal("1639.57"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("17432879")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 15, 6))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(1)
                    .price(new BigDecimal("2982.40"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("2982.40"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.89"))
                    .brokerFee(new BigDecimal("8.95"))
                    .amount(new BigDecimal("2972.56"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build()
    );

    @Test
    @DisplayName("should parse Raiffeisen broker report")
    public void testParse() {
        var accountName = "1234567890";

        cache().add(new Account.Builder()
                .name(accountName)
                .type(CategoryType.PORTFOLIO)
                .categoryUuid(UUID.randomUUID())
                .build());

        try (var inputStream = getClass().getResourceAsStream(REPORT)) {
            var actual = new RaiffeisenBrokerReportParser().parse(accountName, inputStream);
            assertEquals(7, actual.size());

            for (int i = 0; i < EXPECTED.size(); i++) {
                compareItem(EXPECTED.get(i), actual.get(i));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void compareItem(InvestmentDeal expected, InvestmentDeal actual) {
        assertEquals(expected.dealNumber(), actual.dealNumber());
        assertEquals(expected.dealDate(), actual.dealDate());
        assertEquals(expected.accountingDate(), actual.accountingDate());
        assertEquals(expected.marketType(), actual.marketType());
        assertEquals(expected.operationType(), actual.operationType());
        assertEquals(expected.securityAmount(), actual.securityAmount());
        assertEquals(expected.price(), actual.price());
        assertEquals(expected.aci(), actual.aci());
        assertEquals(expected.dealVolume(), actual.dealVolume());
        assertEquals(expected.rate(), actual.rate());
        assertEquals(expected.exchangeFee(), actual.exchangeFee());
        assertEquals(expected.brokerFee(), actual.brokerFee());
        assertEquals(expected.amount(), actual.amount());
        assertEquals(expected.dealType(), actual.dealType());
    }
}
