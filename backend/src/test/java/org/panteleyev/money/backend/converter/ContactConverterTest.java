// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.ContactType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContactConverterTest {

    private final ContactConverter converter = new ContactConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ICON_UUID = UUID.randomUUID();

    private static ContactEntity createEntity() {
        return createEntity(true);
    }

    private static ContactEntity createEntity(boolean withIcon) {
        var entity = new ContactEntity();
        entity.setUuid(CONTACT_UUID);
        entity.setName("Test Contact");
        entity.setType(ContactType.PERSONAL);
        entity.setComment("Test Contact Comment");
        entity.setPhone("+7-123-456-78-90");
        entity.setMobile("+7-999-123-45-67");
        entity.setEmail("test@example.com");
        entity.setWeb("https://example.com");
        entity.setStreet("123 Main St");
        entity.setCity("New York");
        entity.setCountry("USA");
        entity.setZip("10001");
        if (withIcon) {
            entity.setIcon(createIcon());
        }
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static ContactFlatDTO createDto() {
        return createDto(true);
    }

    private static ContactFlatDTO createDto(boolean withIcon) {
        var dto = new ContactFlatDTO()
                .uuid(CONTACT_UUID)
                .name("Test Contact")
                .type(ContactType.PERSONAL)
                .comment("Test Contact Comment")
                .phone("+7-123-456-78-90")
                .mobile("+7-999-123-45-67")
                .email("test@example.com")
                .web("https://example.com")
                .street("123 Main St")
                .city("New York")
                .country("USA")
                .zip("10001")
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
    void testEntityToFlatDto(ContactEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getComment(), result.getComment());
        assertEquals(entity.getPhone(), result.getPhone());
        assertEquals(entity.getMobile(), result.getMobile());
        assertEquals(entity.getEmail(), result.getEmail());
        assertEquals(entity.getWeb(), result.getWeb());
        assertEquals(entity.getStreet(), result.getStreet());
        assertEquals(entity.getCity(), result.getCity());
        assertEquals(entity.getCountry(), result.getCountry());
        assertEquals(entity.getZip(), result.getZip());
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
    void testDtoToEntity(ContactFlatDTO dto) {
        // Given
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, icon);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getType(), result.getType());
        assertEquals(dto.getComment(), result.getComment());
        assertEquals(dto.getPhone(), result.getPhone());
        assertEquals(dto.getMobile(), result.getMobile());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getWeb(), result.getWeb());
        assertEquals(dto.getStreet(), result.getStreet());
        assertEquals(dto.getCity(), result.getCity());
        assertEquals(dto.getCountry(), result.getCountry());
        assertEquals(dto.getZip(), result.getZip());
        assertEquals(icon, result.getIcon());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(ContactEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(ContactFlatDTO dto) {
        // Given
        var icon = createIcon();

        // When
        var result = converter.dtoToEntity(dto, icon);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithoutIcon")
    void testEntityToFlatDtoWithoutIcon(ContactEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNull(result.getIconUuid());
    }

    @ParameterizedTest
    @MethodSource("provideDtosWithoutIcon")
    void testDtoToEntityWithoutIcon(ContactFlatDTO dto) {
        // When
        var result = converter.dtoToEntity(dto, null);

        // Then
        assertNotNull(result);
        assertNull(result.getIcon());
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithDifferentTypes")
    void testDealsWithDifferentTypes(ContactEntity entity, ContactFlatDTO dto) {
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
        // Entity with CLIENT type
        var entity1 = new ContactEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setName("Client Contact");
        entity1.setType(ContactType.CLIENT);
        entity1.setComment("Client Comment");
        entity1.setPhone("+7-111-222-33-44");
        entity1.setMobile("+7-999-111-22-33");
        entity1.setEmail("client@example.com");
        entity1.setWeb("https://client.example.com");
        entity1.setStreet("456 Business Ave");
        entity1.setCity("Moscow");
        entity1.setCountry("Russia");
        entity1.setZip("101000");
        entity1.setIcon(createIcon());
        entity1.setCreated(TEST_CREATED);
        entity1.setModified(TEST_MODIFIED);

        var dto1 = new ContactFlatDTO()
                .uuid(entity1.getUuid())
                .name("Client Contact")
                .type(ContactType.CLIENT)
                .comment("Client Comment")
                .phone("+7-111-222-33-44")
                .mobile("+7-999-111-22-33")
                .email("client@example.com")
                .web("https://client.example.com")
                .street("456 Business Ave")
                .city("Moscow")
                .country("Russia")
                .zip("101000")
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with SUPPLIER type
        var entity2 = new ContactEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setName("Supplier Contact");
        entity2.setType(ContactType.SUPPLIER);
        entity2.setComment("Supplier Comment");
        entity2.setPhone("+7-333-444-55-66");
        entity2.setMobile("+7-999-333-44-55");
        entity2.setEmail("supplier@example.com");
        entity2.setWeb("https://supplier.example.com");
        entity2.setStreet("789 Industry Rd");
        entity2.setCity("Saint Petersburg");
        entity2.setCountry("Russia");
        entity2.setZip("190000");
        entity2.setIcon(createIcon());
        entity2.setCreated(TEST_CREATED);
        entity2.setModified(TEST_MODIFIED);

        var dto2 = new ContactFlatDTO()
                .uuid(entity2.getUuid())
                .name("Supplier Contact")
                .type(ContactType.SUPPLIER)
                .comment("Supplier Comment")
                .phone("+7-333-444-55-66")
                .mobile("+7-999-333-44-55")
                .email("supplier@example.com")
                .web("https://supplier.example.com")
                .street("789 Industry Rd")
                .city("Saint Petersburg")
                .country("Russia")
                .zip("190000")
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2}
        };
    }
}
