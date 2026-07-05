// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.domain.InvestmentDealEntity;
import org.panteleyev.money.dto.CategoryType;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.dto.InvestmentDealType;
import org.panteleyev.money.dto.InvestmentMarketType;
import org.panteleyev.money.dto.InvestmentOperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvestmentDealConverterTest {

    private final InvestmentDealConverter converter = new InvestmentDealConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID ACCOUNT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID SECURITY_UUID = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    private static final UUID CURRENCY_UUID = UUID.fromString("789e4567-e89b-12d3-a456-426614174000");

    private static InvestmentDealEntity createEntity() {
        return createEntity(TEST_CREATED, TEST_MODIFIED, true, true);
    }

    private static InvestmentDealEntity createEntity(long created, long modified) {
        return createEntity(created, modified, true, true);
    }

    private static InvestmentDealEntity createEntity(long created, long modified,
                                                     boolean withSecurity, boolean withCurrency) {
        var entity = new InvestmentDealEntity();
        entity.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        entity.setAccount(createAccount());
        if (withSecurity) {
            entity.setSecurity(createSecurity());
        }
        if (withCurrency) {
            entity.setCurrency(createCurrency());
        }
        entity.setDealNumber("DEAL001");
        entity.setDealDate(LocalDateTime.of(2024, 6, 15, 10, 30, 0));
        entity.setAccountingDate(LocalDateTime.of(2024, 6, 16, 14, 0, 0));
        entity.setMarketType(InvestmentMarketType.STOCK_MARKET);
        entity.setOperationType(InvestmentOperationType.PURCHASE);
        entity.setSecurityAmount(100);
        entity.setPrice(BigDecimal.valueOf(50.25));
        entity.setAci(BigDecimal.valueOf(1.50));
        entity.setDealVolume(BigDecimal.valueOf(5025.00));
        entity.setRate(BigDecimal.valueOf(1.0));
        entity.setExchangeFee(BigDecimal.valueOf(10.00));
        entity.setBrokerFee(BigDecimal.valueOf(25.00));
        entity.setAmount(BigDecimal.valueOf(5061.50));
        entity.setDealType(InvestmentDealType.NORMAL);
        entity.setCreated(created);
        entity.setModified(modified);
        return entity;
    }

    private static InvestmentDealFlatDTO createDto() {
        return createDto(TEST_CREATED, TEST_MODIFIED, true, true);
    }

    private static InvestmentDealFlatDTO createDto(long created, long modified) {
        return createDto(created, modified, true, true);
    }

    private static InvestmentDealFlatDTO createDto(long created, long modified,
                                                   boolean withSecurity, boolean withCurrency) {
        var dto = new InvestmentDealFlatDTO();
        dto.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        dto.setAccountUuid(ACCOUNT_UUID);
        if (withSecurity) {
            dto.setSecurityUuid(SECURITY_UUID);
        }
        if (withCurrency) {
            dto.setCurrencyUuid(CURRENCY_UUID);
        }
        dto.setDealNumber("DEAL001");
        dto.setDealDate(LocalDateTime.of(2024, 6, 15, 10, 30, 0));
        dto.setAccountingDate(LocalDateTime.of(2024, 6, 16, 14, 0, 0));
        dto.setMarketType(InvestmentMarketType.STOCK_MARKET);
        dto.setOperationType(InvestmentOperationType.PURCHASE);
        dto.setSecurityAmount(100);
        dto.setPrice(BigDecimal.valueOf(50.25));
        dto.setAci(BigDecimal.valueOf(1.50));
        dto.setDealVolume(BigDecimal.valueOf(5025.00));
        dto.setRate(BigDecimal.valueOf(1.0));
        dto.setExchangeFee(BigDecimal.valueOf(10.00));
        dto.setBrokerFee(BigDecimal.valueOf(25.00));
        dto.setAmount(BigDecimal.valueOf(5061.50));
        dto.setDealType(InvestmentDealType.NORMAL);
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    private static AccountEntity createAccount() {
        var account = new AccountEntity();
        account.setUuid(ACCOUNT_UUID);
        account.setName("Test Account");
        account.setType(CategoryType.EXPENSES);
        account.setCreated(TEST_CREATED);
        account.setModified(TEST_MODIFIED);
        return account;
    }

    private static ExchangeSecurityEntity createSecurity() {
        var security = new ExchangeSecurityEntity();
        security.setUuid(SECURITY_UUID);
        security.setSecId("TEST001");
        security.setName("Test Security");
        security.setShortName("TS");
        security.setIsin("RU0000000000");
        security.setRegNumber("12345");
        security.setFaceValue(BigDecimal.TEN);
        security.setGroupType("BONDS");
        security.setGroupName("Government Bonds");
        security.setType("GOVERNMENT");
        security.setTypeName("Government Bond");
        security.setMarketValue(BigDecimal.valueOf(100.50));
        security.setCreated(TEST_CREATED);
        security.setModified(TEST_MODIFIED);
        return security;
    }

    private static CurrencyEntity createCurrency() {
        var currency = new CurrencyEntity();
        currency.setUuid(CURRENCY_UUID);
        currency.setSymbol("RUB");
        currency.setDescription("Russian Ruble");
        currency.setFormatSymbol("₽");
        currency.setFormatSymbolPosition(1);
        currency.setShowFormatSymbol(true);
        currency.setDef(false);
        currency.setRate(BigDecimal.ONE);
        currency.setDirection(1);
        currency.setUseThousandSeparator(true);
        currency.setCreated(TEST_CREATED);
        currency.setModified(TEST_MODIFIED);
        return currency;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(InvestmentDealEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getAccount().getUuid(), result.getAccountUuid());
        if (entity.getSecurity() != null) {
            assertEquals(entity.getSecurity().getUuid(), result.getSecurityUuid());
        } else {
            assertNull(result.getSecurityUuid());
        }
        if (entity.getCurrency() != null) {
            assertEquals(entity.getCurrency().getUuid(), result.getCurrencyUuid());
        } else {
            assertNull(result.getCurrencyUuid());
        }
        assertEquals(entity.getDealNumber(), result.getDealNumber());
        assertEquals(entity.getDealDate(), result.getDealDate());
        assertEquals(entity.getAccountingDate(), result.getAccountingDate());
        assertEquals(entity.getMarketType(), result.getMarketType());
        assertEquals(entity.getOperationType(), result.getOperationType());
        assertEquals(entity.getSecurityAmount(), result.getSecurityAmount());
        assertEquals(entity.getPrice(), result.getPrice());
        assertEquals(entity.getAci(), result.getAci());
        assertEquals(entity.getDealVolume(), result.getDealVolume());
        assertEquals(entity.getRate(), result.getRate());
        assertEquals(entity.getExchangeFee(), result.getExchangeFee());
        assertEquals(entity.getBrokerFee(), result.getBrokerFee());
        assertEquals(entity.getAmount(), result.getAmount());
        assertEquals(entity.getDealType(), result.getDealType());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(InvestmentDealFlatDTO dto) {
        // Given
        var account = createAccount();
        var security = createSecurity();
        var currency = createCurrency();

        // When
        var result = converter.dtoToEntity(dto, account, security, currency);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(account, result.getAccount());
        assertEquals(security, result.getSecurity());
        assertEquals(currency, result.getCurrency());
        assertEquals(dto.getDealNumber(), result.getDealNumber());
        assertEquals(dto.getDealDate(), result.getDealDate());
        assertEquals(dto.getAccountingDate(), result.getAccountingDate());
        assertEquals(dto.getMarketType(), result.getMarketType());
        assertEquals(dto.getOperationType(), result.getOperationType());
        assertEquals(dto.getSecurityAmount(), result.getSecurityAmount());
        assertEquals(dto.getPrice(), result.getPrice());
        assertEquals(dto.getAci(), result.getAci());
        assertEquals(dto.getDealVolume(), result.getDealVolume());
        assertEquals(dto.getRate(), result.getRate());
        assertEquals(dto.getExchangeFee(), result.getExchangeFee());
        assertEquals(dto.getBrokerFee(), result.getBrokerFee());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getDealType(), result.getDealType());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(InvestmentDealEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(InvestmentDealFlatDTO dto) {
        // Given
        var account = createAccount();
        var security = createSecurity();
        var currency = createCurrency();

        // When
        var result = converter.dtoToEntity(dto, account, security, currency);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithNullableDependencies")
    void testDealsWithNullableDependencies(InvestmentDealEntity entity, InvestmentDealFlatDTO dto) {
        // Given
        var account = createAccount();
        var security = createSecurity();
        var currency = createCurrency();

        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto, account, security, currency);

        // Then
        assertNotNull(resultDto);
        assertEquals(entity.getUuid(), resultDto.getUuid());
        assertEquals(entity.getAccount().getUuid(), resultDto.getAccountUuid());
        assertEquals(entity.getDealNumber(), resultDto.getDealNumber());
        assertNotNull(resultEntity);
        assertEquals(dto.getUuid(), resultEntity.getUuid());
        assertEquals(dto.getAccountUuid(), resultEntity.getAccount().getUuid());
        assertEquals(dto.getDealNumber(), resultEntity.getDealNumber());
    }

    private static Object[] provideEntitiesForConversion() {
        return new Object[]{createEntity()};
    }

    private static Object[] provideDtosForConversion() {
        return new Object[]{createDto()};
    }

    private static Object[] provideNullValues() {
        return new Object[]{null};
    }

    private static Object[] provideDealsWithNullableDependencies() {
        // Entity without security and currency
        var entity1 = createEntity(TEST_CREATED, TEST_MODIFIED, false, false);
        var dto1 = createDto(TEST_CREATED, TEST_MODIFIED, false, false);

        // Entity without security only
        var entity2 = createEntity(TEST_CREATED, TEST_MODIFIED, false, true);
        var dto2 = createDto(TEST_CREATED, TEST_MODIFIED, false, true);

        // Entity without currency only
        var entity3 = createEntity(TEST_CREATED, TEST_MODIFIED, true, false);
        var dto3 = createDto(TEST_CREATED, TEST_MODIFIED, true, false);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2},
                new Object[]{entity3, dto3}
        };
    }
}
