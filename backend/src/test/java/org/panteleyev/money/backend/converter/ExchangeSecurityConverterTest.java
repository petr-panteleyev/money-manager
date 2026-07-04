// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExchangeSecurityConverterTest {

    private final ExchangeSecurityConverter converter = new ExchangeSecurityConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static ExchangeSecurityEntity createEntity() {
        return createEntity(TEST_CREATED, TEST_MODIFIED);
    }

    private static ExchangeSecurityEntity createEntity(long created, long modified) {
        var entity = new ExchangeSecurityEntity();
        entity.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        entity.setSecId("TEST001");
        entity.setName("Test Security");
        entity.setShortName("TS");
        entity.setIsin("RU0000000000");
        entity.setRegNumber("12345");
        entity.setFaceValue(BigDecimal.TEN);
        entity.setIssueDate(LocalDate.of(2024, 1, 1));
        entity.setMatDate(LocalDate.of(2030, 1, 1));
        entity.setDaysToRedemption(180);
        entity.setGroupType("BONDS");
        entity.setGroupName("Government Bonds");
        entity.setType("GOVERNMENT");
        entity.setTypeName("Government Bond");
        entity.setMarketValue(BigDecimal.valueOf(100.50));
        entity.setCouponValue(BigDecimal.valueOf(5.0));
        entity.setCouponPercent(BigDecimal.valueOf(5.5));
        entity.setCouponDate(LocalDate.of(2025, 1, 1));
        entity.setCouponFrequency(2);
        entity.setAccruedInterest(BigDecimal.valueOf(1.25));
        entity.setCouponPeriod(365);
        entity.setCreated(created);
        entity.setModified(modified);
        return entity;
    }

    private static ExchangeSecurityFlatDTO createDto() {
        return createDto(TEST_CREATED, TEST_MODIFIED);
    }

    private static ExchangeSecurityFlatDTO createDto(long created, long modified) {
        var dto = new ExchangeSecurityFlatDTO();
        dto.setUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        dto.setSecId("TEST001");
        dto.setName("Test Security");
        dto.setShortName("TS");
        dto.setIsin("RU0000000000");
        dto.setRegNumber("12345");
        dto.setFaceValue(BigDecimal.TEN);
        dto.setIssueDate(LocalDate.of(2024, 1, 1));
        dto.setMatDate(LocalDate.of(2030, 1, 1));
        dto.setDaysToRedemption(180);
        dto.setGroupType("BONDS");
        dto.setGroupName("Government Bonds");
        dto.setType("GOVERNMENT");
        dto.setTypeName("Government Bond");
        dto.setMarketValue(BigDecimal.valueOf(100.50));
        dto.setCouponValue(BigDecimal.valueOf(5.0));
        dto.setCouponPercent(BigDecimal.valueOf(5.5));
        dto.setCouponDate(LocalDate.of(2025, 1, 1));
        dto.setCouponFrequency(2);
        dto.setAccruedInterest(BigDecimal.valueOf(1.25));
        dto.setCouponPeriod(365);
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(ExchangeSecurityEntity entity) {
        // Given
        var expectedDto = createDto();

        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto.getUuid(), result.getUuid());
        assertEquals(expectedDto.getSecId(), result.getSecId());
        assertEquals(expectedDto.getName(), result.getName());
        assertEquals(expectedDto.getShortName(), result.getShortName());
        assertEquals(expectedDto.getIsin(), result.getIsin());
        assertEquals(expectedDto.getRegNumber(), result.getRegNumber());
        assertEquals(expectedDto.getFaceValue(), result.getFaceValue());
        assertEquals(expectedDto.getIssueDate(), result.getIssueDate());
        assertEquals(expectedDto.getMatDate(), result.getMatDate());
        assertEquals(expectedDto.getDaysToRedemption(), result.getDaysToRedemption());
        assertEquals(expectedDto.getGroupType(), result.getGroupType());
        assertEquals(expectedDto.getGroupName(), result.getGroupName());
        assertEquals(expectedDto.getType(), result.getType());
        assertEquals(expectedDto.getTypeName(), result.getTypeName());
        assertEquals(expectedDto.getMarketValue(), result.getMarketValue());
        assertEquals(expectedDto.getCouponValue(), result.getCouponValue());
        assertEquals(expectedDto.getCouponPercent(), result.getCouponPercent());
        assertEquals(expectedDto.getCouponDate(), result.getCouponDate());
        assertEquals(expectedDto.getCouponFrequency(), result.getCouponFrequency());
        assertEquals(expectedDto.getAccruedInterest(), result.getAccruedInterest());
        assertEquals(expectedDto.getCouponPeriod(), result.getCouponPeriod());
        assertEquals(expectedDto.getCreated(), result.getCreated());
        assertEquals(expectedDto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(ExchangeSecurityFlatDTO dto) {
        // Given
        var expectedEntity = createEntity();

        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(expectedEntity.getUuid(), result.getUuid());
        assertEquals(expectedEntity.getSecId(), result.getSecId());
        assertEquals(expectedEntity.getName(), result.getName());
        assertEquals(expectedEntity.getShortName(), result.getShortName());
        assertEquals(expectedEntity.getIsin(), result.getIsin());
        assertEquals(expectedEntity.getRegNumber(), result.getRegNumber());
        assertEquals(expectedEntity.getFaceValue(), result.getFaceValue());
        assertEquals(expectedEntity.getIssueDate(), result.getIssueDate());
        assertEquals(expectedEntity.getMatDate(), result.getMatDate());
        assertEquals(expectedEntity.getDaysToRedemption(), result.getDaysToRedemption());
        assertEquals(expectedEntity.getGroupType(), result.getGroupType());
        assertEquals(expectedEntity.getGroupName(), result.getGroupName());
        assertEquals(expectedEntity.getType(), result.getType());
        assertEquals(expectedEntity.getTypeName(), result.getTypeName());
        assertEquals(expectedEntity.getMarketValue(), result.getMarketValue());
        assertEquals(expectedEntity.getCouponValue(), result.getCouponValue());
        assertEquals(expectedEntity.getCouponPercent(), result.getCouponPercent());
        assertEquals(expectedEntity.getCouponDate(), result.getCouponDate());
        assertEquals(expectedEntity.getCouponFrequency(), result.getCouponFrequency());
        assertEquals(expectedEntity.getAccruedInterest(), result.getAccruedInterest());
        assertEquals(expectedEntity.getCouponPeriod(), result.getCouponPeriod());
        assertEquals(expectedEntity.getCreated(), result.getCreated());
        assertEquals(expectedEntity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(ExchangeSecurityEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(ExchangeSecurityFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNull(result);
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
}
