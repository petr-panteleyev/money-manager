// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountConverterTest {

    private final AccountConverter converter = new AccountConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID SECURITY_UUID = UUID.randomUUID();
    private static final UUID ICON_UUID = UUID.randomUUID();

    private static AccountEntity createEntity() {
        return createEntity(TEST_CREATED, TEST_MODIFIED, true, true, true);
    }

    private static AccountEntity createEntity(long created, long modified,
            boolean withCurrency, boolean withSecurity, boolean withIcon)
    {
        var entity = new AccountEntity();
        entity.setUuid(ACCOUNT_UUID);
        entity.setName("Test Account");
        entity.setComment("Test Comment");
        entity.setAccountNumber("40817810123456789012");
        entity.setOpeningBalance(BigDecimal.valueOf(10000.00));
        entity.setAccountLimit(BigDecimal.valueOf(50000.00));
        entity.setCurrencyRate(BigDecimal.valueOf(1.0));
        entity.setType(CategoryType.BANKS_AND_CASH);
        entity.setCategory(createCategory());
        if (withCurrency) {
            entity.setCurrency(createCurrency());
        }
        if (withSecurity) {
            entity.setSecurity(createSecurity());
        }
        entity.setEnabled(true);
        entity.setInterest(BigDecimal.valueOf(5.5));
        entity.setClosingDate(LocalDate.of(2030, 12, 31));
        if (withIcon) {
            entity.setIcon(createIcon());
        }
        entity.setTotal(BigDecimal.valueOf(15000.00));
        entity.setTotalWaiting(BigDecimal.valueOf(500.00));
        entity.setCreated(created);
        entity.setModified(modified);
        return entity;
    }

    private static AccountFlatDTO createDto() {
        return createDto(TEST_CREATED, TEST_MODIFIED, true, true, true);
    }

    private static AccountFlatDTO createDto(long created, long modified,
            boolean withCurrency, boolean withSecurity, boolean withIcon)
    {
        var dto = new AccountFlatDTO()
                .uuid(ACCOUNT_UUID)
                .name("Test Account")
                .comment("Test Comment")
                .accountNumber("40817810123456789012")
                .openingBalance(BigDecimal.valueOf(10000.00))
                .accountLimit(BigDecimal.valueOf(50000.00))
                .currencyRate(BigDecimal.valueOf(1.0))
                .type(CategoryType.BANKS_AND_CASH)
                .categoryUuid(CATEGORY_UUID)
                .enabled(true)
                .interest(BigDecimal.valueOf(5.5))
                .total(BigDecimal.valueOf(15000.00))
                .totalWaiting(BigDecimal.valueOf(500.00))
                .created(created)
                .modified(modified);
        if (withCurrency) {
            dto.setCurrencyUuid(CURRENCY_UUID);
        }
        if (withSecurity) {
            dto.setSecurityUuid(SECURITY_UUID);
        }
        dto.setClosingDate(LocalDate.of(2030, 12, 31));
        if (withIcon) {
            dto.setIconUuid(ICON_UUID);
        }
        return dto;
    }

    private static CategoryEntity createCategory() {
        var category = new CategoryEntity();
        category.setUuid(CATEGORY_UUID);
        category.setName("Test Category");
        category.setType(CategoryType.BANKS_AND_CASH);
        category.setCreated(TEST_CREATED);
        category.setModified(TEST_MODIFIED);
        return category;
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

    private static IconEntity createIcon() {
        var icon = new IconEntity();
        icon.setUuid(ICON_UUID);
        icon.setName("Test Icon");
        icon.setBytes(new byte[]{1, 2, 3, 4, 5});
        icon.setCreated(TEST_CREATED);
        icon.setModified(TEST_MODIFIED);
        return icon;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(AccountEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getComment(), result.getComment());
        assertEquals(entity.getAccountNumber(), result.getAccountNumber());
        assertEquals(entity.getOpeningBalance(), result.getOpeningBalance());
        assertEquals(entity.getAccountLimit(), result.getAccountLimit());
        assertEquals(entity.getCurrencyRate(), result.getCurrencyRate());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getCategory().getUuid(), result.getCategoryUuid());
        if (entity.getCurrency() != null) {
            assertEquals(entity.getCurrency().getUuid(), result.getCurrencyUuid());
        } else {
            assertNull(result.getCurrencyUuid());
        }
        if (entity.getSecurity() != null) {
            assertEquals(entity.getSecurity().getUuid(), result.getSecurityUuid());
        } else {
            assertNull(result.getSecurityUuid());
        }
        assertEquals(entity.isEnabled(), result.getEnabled());
        assertEquals(entity.getInterest(), result.getInterest());
        assertEquals(entity.getClosingDate(), result.getClosingDate());
        if (entity.getIcon() != null) {
            assertEquals(entity.getIcon().getUuid(), result.getIconUuid());
        } else {
            assertNull(result.getIconUuid());
        }
        assertEquals(entity.getTotal(), result.getTotal());
        assertEquals(entity.getTotalWaiting(), result.getTotalWaiting());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(AccountFlatDTO dto) {
        // Given
        var category = createCategory();
        var currency = createCurrency();
        var security = createSecurity();
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, category, currency, security, icon);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getComment(), result.getComment());
        assertEquals(dto.getAccountNumber(), result.getAccountNumber());
        assertEquals(dto.getOpeningBalance(), result.getOpeningBalance());
        assertEquals(dto.getAccountLimit(), result.getAccountLimit());
        assertEquals(dto.getCurrencyRate(), result.getCurrencyRate());
        // Note: Converter uses category.getType() not dto.getType()
        assertEquals(category.getType(), result.getType());
        assertEquals(category, result.getCategory());
        assertEquals(currency, result.getCurrency());
        assertEquals(security, result.getSecurity());
        assertEquals(dto.getEnabled(), result.isEnabled());
        assertEquals(dto.getInterest(), result.getInterest());
        assertEquals(dto.getClosingDate(), result.getClosingDate());
        assertEquals(icon, result.getIcon());
        assertEquals(dto.getTotal(), result.getTotal());
        assertEquals(dto.getTotalWaiting(), result.getTotalWaiting());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(AccountEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(AccountFlatDTO dto) {
        // Given
        var category = createCategory();
        var currency = createCurrency();
        var security = createSecurity();
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, category, currency, security, icon);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithNullableDependencies")
    void testDealsWithNullableDependencies(AccountEntity entity, AccountFlatDTO dto) {
        // Given
        var category = createCategory();
        var currency = createCurrency();

        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto, category, currency, null, null);

        // Then
        assertNotNull(resultDto);
        assertEquals(entity.getUuid(), resultDto.getUuid());
        assertEquals(entity.getName(), resultDto.getName());
        assertEquals(entity.getAccountNumber(), resultDto.getAccountNumber());
        assertNotNull(resultEntity);
        assertEquals(dto.getUuid(), resultEntity.getUuid());
        assertEquals(dto.getName(), resultEntity.getName());
        assertEquals(dto.getAccountNumber(), resultEntity.getAccountNumber());
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
        // Entity without currency, security and icon
        var entity1 = createEntity(TEST_CREATED, TEST_MODIFIED, false, false, false);
        var dto1 = createDto(TEST_CREATED, TEST_MODIFIED, false, false, false);

        // Entity without currency and icon
        var entity2 = createEntity(TEST_CREATED, TEST_MODIFIED, false, true, false);
        var dto2 = createDto(TEST_CREATED, TEST_MODIFIED, false, true, false);

        // Entity without security and icon
        var entity3 = createEntity(TEST_CREATED, TEST_MODIFIED, true, false, false);
        var dto3 = createDto(TEST_CREATED, TEST_MODIFIED, true, false, false);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2},
                new Object[]{entity3, dto3}
        };
    }
}
