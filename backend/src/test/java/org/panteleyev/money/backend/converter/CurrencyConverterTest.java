// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.dto.CurrencyFlatDTO;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrencyConverterTest {

    private final CurrencyConverter converter = new CurrencyConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID CURRENCY_UUID = UUID.randomUUID();

    private static CurrencyEntity createEntity() {
        var entity = new CurrencyEntity();
        entity.setUuid(CURRENCY_UUID);
        entity.setSymbol("RUB");
        entity.setDescription("Russian Ruble");
        entity.setFormatSymbol("₽");
        entity.setFormatSymbolPosition(1);
        entity.setShowFormatSymbol(true);
        entity.setDef(true);
        entity.setRate(BigDecimal.valueOf(1.0));
        entity.setDirection(1);
        entity.setUseThousandSeparator(true);
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static CurrencyFlatDTO createDto() {
        return new CurrencyFlatDTO()
                .uuid(CURRENCY_UUID)
                .symbol("RUB")
                .description("Russian Ruble")
                .formatSymbol("₽")
                .formatSymbolPosition(1)
                .showFormatSymbol(true)
                .def(true)
                .rate(BigDecimal.valueOf(1.0))
                .direction(1)
                .useThousandSeparator(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(CurrencyEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getSymbol(), result.getSymbol());
        assertEquals(entity.getDescription(), result.getDescription());
        assertEquals(entity.getFormatSymbol(), result.getFormatSymbol());
        assertEquals(entity.getFormatSymbolPosition(), result.getFormatSymbolPosition());
        assertEquals(entity.isShowFormatSymbol(), result.getShowFormatSymbol());
        assertEquals(entity.isDef(), result.getDef());
        assertEquals(entity.getRate(), result.getRate());
        assertEquals(entity.getDirection(), result.getDirection());
        assertEquals(entity.isUseThousandSeparator(), result.getUseThousandSeparator());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(CurrencyFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getSymbol(), result.getSymbol());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getFormatSymbol(), result.getFormatSymbol());
        assertEquals(dto.getFormatSymbolPosition(), result.getFormatSymbolPosition());
        assertEquals(dto.getShowFormatSymbol(), result.isShowFormatSymbol());
        assertEquals(dto.getDef(), result.isDef());
        assertEquals(dto.getRate(), result.getRate());
        assertEquals(dto.getDirection(), result.getDirection());
        assertEquals(dto.getUseThousandSeparator(), result.isUseThousandSeparator());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(CurrencyEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(CurrencyFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithDifferentCurrencies")
    void testDealsWithDifferentCurrencies(CurrencyEntity entity, CurrencyFlatDTO dto) {
        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto);

        // Then
        assertNotNull(resultDto);
        assertEquals(entity.getUuid(), resultDto.getUuid());
        assertEquals(entity.getSymbol(), resultDto.getSymbol());
        assertNotNull(resultEntity);
        assertEquals(dto.getUuid(), resultEntity.getUuid());
        assertEquals(dto.getSymbol(), resultEntity.getSymbol());
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

    private static Object[] provideDealsWithDifferentCurrencies() {
        // Entity with USD
        var entity1 = new CurrencyEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setSymbol("USD");
        entity1.setDescription("US Dollar");
        entity1.setFormatSymbol("$");
        entity1.setFormatSymbolPosition(0);
        entity1.setShowFormatSymbol(true);
        entity1.setDef(false);
        entity1.setRate(BigDecimal.valueOf(95.5));
        entity1.setDirection(1);
        entity1.setUseThousandSeparator(true);
        entity1.setCreated(TEST_CREATED);
        entity1.setModified(TEST_MODIFIED);

        var dto1 = new CurrencyFlatDTO()
                .uuid(entity1.getUuid())
                .symbol("USD")
                .description("US Dollar")
                .formatSymbol("$")
                .formatSymbolPosition(0)
                .showFormatSymbol(true)
                .def(false)
                .rate(BigDecimal.valueOf(95.5))
                .direction(1)
                .useThousandSeparator(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with EUR
        var entity2 = new CurrencyEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setSymbol("EUR");
        entity2.setDescription("Euro");
        entity2.setFormatSymbol("€");
        entity2.setFormatSymbolPosition(0);
        entity2.setShowFormatSymbol(true);
        entity2.setDef(false);
        entity2.setRate(BigDecimal.valueOf(105.3));
        entity2.setDirection(1);
        entity2.setUseThousandSeparator(true);
        entity2.setCreated(TEST_CREATED);
        entity2.setModified(TEST_MODIFIED);

        var dto2 = new CurrencyFlatDTO()
                .uuid(entity2.getUuid())
                .symbol("EUR")
                .description("Euro")
                .formatSymbol("€")
                .formatSymbolPosition(0)
                .showFormatSymbol(true)
                .def(false)
                .rate(BigDecimal.valueOf(105.3))
                .direction(1)
                .useThousandSeparator(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with CNY
        var entity3 = new CurrencyEntity();
        entity3.setUuid(UUID.randomUUID());
        entity3.setSymbol("CNY");
        entity3.setDescription("Chinese Yuan");
        entity3.setFormatSymbol("¥");
        entity3.setFormatSymbolPosition(0);
        entity3.setShowFormatSymbol(true);
        entity3.setDef(false);
        entity3.setRate(BigDecimal.valueOf(13.2));
        entity3.setDirection(1);
        entity3.setUseThousandSeparator(false);
        entity3.setCreated(TEST_CREATED);
        entity3.setModified(TEST_MODIFIED);

        var dto3 = new CurrencyFlatDTO()
                .uuid(entity3.getUuid())
                .symbol("CNY")
                .description("Chinese Yuan")
                .formatSymbol("¥")
                .formatSymbolPosition(0)
                .showFormatSymbol(true)
                .def(false)
                .rate(BigDecimal.valueOf(13.2))
                .direction(1)
                .useThousandSeparator(false)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2},
                new Object[]{entity3, dto3}
        };
    }
}
