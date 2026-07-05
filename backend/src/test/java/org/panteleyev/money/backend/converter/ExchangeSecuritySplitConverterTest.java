// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.ExchangeSecuritySplitEntity;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExchangeSecuritySplitConverterTest {

    private final ExchangeSecuritySplitConverter converter = new ExchangeSecuritySplitConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static ExchangeSecuritySplitEntity createEntity() {
        return createEntity(TEST_CREATED, TEST_MODIFIED);
    }

    private static ExchangeSecuritySplitEntity createEntity(long created, long modified) {
        var entity = new ExchangeSecuritySplitEntity();
        entity.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        entity.setSecurityUuid(UUID.fromString("456e4567-e89b-12d3-a456-426614174000"));
        entity.setSplitType(ExchangeSecuritySplitType.SPLIT);
        entity.setSplitDate(LocalDate.of(2024, 6, 15));
        entity.setRate(BigDecimal.valueOf(2.0));
        entity.setComment("Stock split 2:1");
        entity.setCreated(created);
        entity.setModified(modified);
        return entity;
    }

    private static ExchangeSecuritySplitFlatDTO createDto() {
        return createDto(TEST_CREATED, TEST_MODIFIED);
    }

    private static ExchangeSecuritySplitFlatDTO createDto(long created, long modified) {
        var dto = new ExchangeSecuritySplitFlatDTO();
        dto.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        dto.setSecurityUuid(UUID.fromString("456e4567-e89b-12d3-a456-426614174000"));
        dto.setSplitType(ExchangeSecuritySplitType.SPLIT);
        dto.setSplitDate(LocalDate.of(2024, 6, 15));
        dto.setRate(BigDecimal.valueOf(2.0));
        dto.setComment("Stock split 2:1");
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(ExchangeSecuritySplitEntity entity) {
        // Given
        var expectedDto = createDto();

        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto.getUuid(), result.getUuid());
        assertEquals(expectedDto.getSecurityUuid(), result.getSecurityUuid());
        assertEquals(expectedDto.getSplitType(), result.getSplitType());
        assertEquals(expectedDto.getSplitDate(), result.getSplitDate());
        assertEquals(expectedDto.getRate(), result.getRate());
        assertEquals(expectedDto.getComment(), result.getComment());
        assertEquals(expectedDto.getCreated(), result.getCreated());
        assertEquals(expectedDto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(ExchangeSecuritySplitFlatDTO dto) {
        // Given
        var expectedEntity = createEntity();

        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(expectedEntity.getUuid(), result.getUuid());
        assertEquals(expectedEntity.getSecurityUuid(), result.getSecurityUuid());
        assertEquals(expectedEntity.getSplitType(), result.getSplitType());
        assertEquals(expectedEntity.getSplitDate(), result.getSplitDate());
        assertEquals(expectedEntity.getRate(), result.getRate());
        assertEquals(expectedEntity.getComment(), result.getComment());
        assertEquals(expectedEntity.getCreated(), result.getCreated());
        assertEquals(expectedEntity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(ExchangeSecuritySplitEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(ExchangeSecuritySplitFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideDifferentSplitTypes")
    void testDifferentSplitTypes(ExchangeSecuritySplitType splitType) {
        // Given
        var entity = createEntity();
        entity.setSplitType(splitType);
        var dto = createDto();
        dto.setSplitType(splitType);

        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto);

        // Then
        assertNotNull(resultDto);
        assertEquals(splitType, resultDto.getSplitType());
        assertNotNull(resultEntity);
        assertEquals(splitType, resultEntity.getSplitType());
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

    private static Object[] provideDifferentSplitTypes() {
        return new Object[]{
                ExchangeSecuritySplitType.SPLIT,
                ExchangeSecuritySplitType.REVERSE_SPLIT
        };
    }
}
