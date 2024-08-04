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

public class SberbankBrokerReportParserTest {
    private static final String REPORT = "/org/panteleyev/money/app/investment/Test_Sberbank_Broker_Report.xlsx";

    private static final List<InvestmentDeal> EXPECTED = List.of(
            new InvestmentDeal.Builder()
                    .dealNumber("37326529")
                    .dealDate(LocalDateTime.of(2024, 4, 1, 10, 21, 31))
                    .accountingDate(LocalDateTime.of(2024, 4, 2, 9, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.PURCHASE)
                    .securityAmount(43)
                    .price(new BigDecimal("10.278"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("441.95"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.14"))
                    .brokerFee(new BigDecimal("0"))
                    .amount(new BigDecimal("442.09"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("84809054")
                    .dealDate(LocalDateTime.of(2024, 2, 28, 18, 36, 6))
                    .accountingDate(LocalDateTime.of(2024, 2, 29, 9, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.PURCHASE)
                    .securityAmount(1)
                    .price(new BigDecimal("90.547"))
                    .aci(new BigDecimal("4.91"))
                    .dealVolume(new BigDecimal("905.47"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.07"))
                    .brokerFee(new BigDecimal("0.54"))
                    .amount(new BigDecimal("910.99"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("99865492")
                    .dealDate(LocalDateTime.of(2024, 2, 28, 18, 35, 28))
                    .accountingDate(LocalDateTime.of(2024, 2, 29, 9, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.PURCHASE)
                    .securityAmount(90)
                    .price(new BigDecimal("161.19"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("14507.1"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("4.35"))
                    .brokerFee(new BigDecimal("8.71"))
                    .amount(new BigDecimal("14520.16"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("34904326")
                    .dealDate(LocalDateTime.of(2024, 1, 30, 10, 3, 42))
                    .accountingDate(LocalDateTime.of(2024, 1, 31, 9, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.SELL)
                    .securityAmount(6)
                    .price(new BigDecimal("2968"))
                    .aci(new BigDecimal("0"))
                    .dealVolume(new BigDecimal("17808"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("5.34"))
                    .brokerFee(new BigDecimal("10.69"))
                    .amount(new BigDecimal("17791.97"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build(),
            new InvestmentDeal.Builder()
                    .dealNumber("39955156")
                    .dealDate(LocalDateTime.of(2024, 1, 26, 14, 44, 27))
                    .accountingDate(LocalDateTime.of(2024, 1, 29, 9, 0, 0))
                    .marketType(InvestmentMarketType.STOCK_MARKET)
                    .operationType(InvestmentOperationType.PURCHASE)
                    .securityAmount(1)
                    .price(new BigDecimal("95.25"))
                    .aci(new BigDecimal("35.68"))
                    .dealVolume(new BigDecimal("952.5"))
                    .rate(BigDecimal.ONE)
                    .exchangeFee(new BigDecimal("0.09"))
                    .brokerFee(new BigDecimal("0.57"))
                    .amount(new BigDecimal("988.84"))
                    .dealType(InvestmentDealType.NORMAL)
                    .build()
    );

    @Test
    @DisplayName("should parse Sberbank broker report")
    public void testParse() {
        cache().add(new Account.Builder()
                .name("W88451772")
                .type(CategoryType.PORTFOLIO)
                .categoryUuid(UUID.randomUUID())
                .build());

        try (var inputStream = getClass().getResourceAsStream(REPORT)) {
            var actual = new SberbankBrokerReportParser().parse(inputStream);
            assertEquals(5, actual.size());

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
