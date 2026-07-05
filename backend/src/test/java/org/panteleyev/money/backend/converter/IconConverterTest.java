// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.IconFlatDTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IconConverterTest {

    private final IconConverter converter = new IconConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID ICON_UUID = UUID.randomUUID();

    private static IconEntity createEntity() {
        var entity = new IconEntity();
        entity.setUuid(ICON_UUID);
        entity.setName("Test Icon");
        entity.setBytes(new byte[]{1, 2, 3, 4, 5});
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static IconFlatDTO createDto() {
        return new IconFlatDTO()
                .uuid(ICON_UUID)
                .name("Test Icon")
                .bytes(new byte[]{1, 2, 3, 4, 5})
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(IconEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getName(), result.getName());
        assertArrayEquals(entity.getBytes(), result.getBytes());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(IconFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getName(), result.getName());
        assertArrayEquals(dto.getBytes(), result.getBytes());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(IconEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(IconFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithEmptyBytes")
    void testEntityToFlatDtoWithEmptyBytes(IconEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBytes());
        assertEquals(0, result.getBytes().length);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithNullBytes")
    void testEntityToFlatDtoWithNullBytes(IconEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNull(result.getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithDifferentIcons")
    void testEntityToFlatDtoWithDifferentIcons(IconEntity entity, IconFlatDTO dto) {
        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto);

        // Then
        assertNotNull(resultDto);
        assertEquals(entity.getUuid(), resultDto.getUuid());
        assertEquals(entity.getName(), resultDto.getName());
        assertArrayEquals(entity.getBytes(), resultDto.getBytes());
        assertNotNull(resultEntity);
        assertEquals(dto.getUuid(), resultEntity.getUuid());
        assertEquals(dto.getName(), resultEntity.getName());
        assertArrayEquals(dto.getBytes(), resultEntity.getBytes());
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

    private static Object[] provideEntitysWithEmptyBytes() {
        var entity = new IconEntity();
        entity.setUuid(UUID.randomUUID());
        entity.setName("Empty Bytes Icon");
        entity.setBytes(new byte[]{});
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return new Object[]{entity};
    }

    private static Object[] provideEntitysWithNullBytes() {
        var entity = new IconEntity();
        entity.setUuid(UUID.randomUUID());
        entity.setName("Null Bytes Icon");
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return new Object[]{entity};
    }

    private static Object[] provideEntitysWithDifferentIcons() {
        // Entity with different name and bytes
        var entity1 = new IconEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setName("Different Icon");
        entity1.setBytes(new byte[]{10, 20, 30, 40, 50});
        entity1.setCreated(TEST_CREATED);
        entity1.setModified(TEST_MODIFIED);

        var dto1 = new IconFlatDTO()
                .uuid(entity1.getUuid())
                .name("Different Icon")
                .bytes(new byte[]{10, 20, 30, 40, 50})
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with larger bytes array
        var entity2 = new IconEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setName("Large Icon");
        var largeBytes = new byte[1000];
        for (int i = 0; i < largeBytes.length; i++) {
            largeBytes[i] = (byte) (i % 256);
        }
        entity2.setBytes(largeBytes);
        entity2.setCreated(TEST_CREATED);
        entity2.setModified(TEST_MODIFIED);

        var dto2 = new IconFlatDTO()
                .uuid(entity2.getUuid())
                .name("Large Icon")
                .bytes(largeBytes)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2}
        };
    }
}
