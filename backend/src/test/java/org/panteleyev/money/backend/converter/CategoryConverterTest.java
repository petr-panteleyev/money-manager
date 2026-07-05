// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.panteleyev.money.dto.CategoryType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryConverterTest {

    private final CategoryConverter converter = new CategoryConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID ICON_UUID = UUID.randomUUID();

    private static CategoryEntity createEntity() {
        return createEntity(true);
    }

    private static CategoryEntity createEntity(boolean withIcon) {
        var entity = new CategoryEntity();
        entity.setUuid(CATEGORY_UUID);
        entity.setName("Test Category");
        entity.setComment("Test Category Comment");
        entity.setType(CategoryType.BANKS_AND_CASH);
        if (withIcon) {
            entity.setIcon(createIcon());
        }
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static CategoryFlatDTO createDto() {
        return createDto(true);
    }

    private static CategoryFlatDTO createDto(boolean withIcon) {
        var dto = new CategoryFlatDTO()
                .uuid(CATEGORY_UUID)
                .name("Test Category")
                .comment("Test Category Comment")
                .type(CategoryType.BANKS_AND_CASH)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
        if (withIcon) {
            dto.setIconUuid(ICON_UUID);
        }
        return dto;
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
    void testEntityToFlatDto(CategoryEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getComment(), result.getComment());
        assertEquals(entity.getType(), result.getType());
        if (entity.getIcon() != null) {
            assertEquals(entity.getIcon().getUuid(), result.getIconUuid());
        } else {
            assertNull(result.getIconUuid());
        }
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(CategoryFlatDTO dto) {
        // Given
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, icon);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getComment(), result.getComment());
        assertEquals(dto.getType(), result.getType());
        assertEquals(icon, result.getIcon());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(CategoryEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(CategoryFlatDTO dto) {
        // Given
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, icon);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithoutIcon")
    void testEntityToFlatDtoWithoutIcon(CategoryEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNull(result.getIconUuid());
    }

    @ParameterizedTest
    @MethodSource("provideDtosWithoutIcon")
    void testDtoToEntityWithoutIcon(CategoryFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto, null);

        // Then
        assertNotNull(result);
        assertNull(result.getIcon());
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithDifferentTypes")
    void testDealsWithDifferentTypes(CategoryEntity entity, CategoryFlatDTO dto) {
        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto, createIcon());

        // Then
        assertNotNull(resultDto);
        assertEquals(entity.getUuid(), resultDto.getUuid());
        assertEquals(entity.getType(), resultDto.getType());
        assertNotNull(resultEntity);
        assertEquals(dto.getUuid(), resultEntity.getUuid());
        assertEquals(dto.getType(), resultEntity.getType());
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

    private static Object[] provideEntitysWithoutIcon() {
        var entity = createEntity(false);
        return new Object[]{entity};
    }

    private static Object[] provideDtosWithoutIcon() {
        var dto = createDto(false);
        return new Object[]{dto};
    }

    private static Object[] provideDealsWithDifferentTypes() {
        // Entity with INCOMES type
        var entity1 = new CategoryEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setName("Income Category");
        entity1.setComment("Income Comment");
        entity1.setType(CategoryType.INCOMES);
        entity1.setIcon(createIcon());
        entity1.setCreated(TEST_CREATED);
        entity1.setModified(TEST_MODIFIED);

        var dto1 = new CategoryFlatDTO()
                .uuid(entity1.getUuid())
                .name("Income Category")
                .comment("Income Comment")
                .type(CategoryType.INCOMES)
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with EXPENSES type
        var entity2 = new CategoryEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setName("Expense Category");
        entity2.setComment("Expense Comment");
        entity2.setType(CategoryType.EXPENSES);
        entity2.setIcon(createIcon());
        entity2.setCreated(TEST_CREATED);
        entity2.setModified(TEST_MODIFIED);

        var dto2 = new CategoryFlatDTO()
                .uuid(entity2.getUuid())
                .name("Expense Category")
                .comment("Expense Comment")
                .type(CategoryType.EXPENSES)
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with PORTFOLIO type
        var entity3 = new CategoryEntity();
        entity3.setUuid(UUID.randomUUID());
        entity3.setName("Portfolio Category");
        entity3.setComment("Portfolio Comment");
        entity3.setType(CategoryType.PORTFOLIO);
        entity3.setIcon(createIcon());
        entity3.setCreated(TEST_CREATED);
        entity3.setModified(TEST_MODIFIED);

        var dto3 = new CategoryFlatDTO()
                .uuid(entity3.getUuid())
                .name("Portfolio Category")
                .comment("Portfolio Comment")
                .type(CategoryType.PORTFOLIO)
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2},
                new Object[]{entity3, dto3}
        };
    }
}
