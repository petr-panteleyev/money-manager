// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.dto.CardFlatDTO;
import org.panteleyev.money.dto.CardType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardConverterTest {

    private final CardConverter converter = new CardConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID CARD_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();

    private static CardEntity createEntity() {
        var entity = new CardEntity();
        entity.setUuid(CARD_UUID);
        entity.setAccount(createAccount());
        entity.setType(CardType.VISA);
        entity.setNumber("4276870012345678");
        entity.setExpiration(LocalDate.of(2028, 12, 31));
        entity.setComment("Test Card Comment");
        entity.setEnabled(true);
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static CardFlatDTO createDto() {
        return new CardFlatDTO()
                .uuid(CARD_UUID)
                .accountUuid(ACCOUNT_UUID)
                .type(CardType.VISA)
                .number("4276870012345678")
                .expiration(LocalDate.of(2028, 12, 31))
                .comment("Test Card Comment")
                .enabled(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    private static AccountEntity createAccount() {
        var account = new AccountEntity();
        account.setUuid(ACCOUNT_UUID);
        account.setName("Test Account");
        account.setComment("Test Account Comment");
        account.setAccountNumber("40817810123456789012");
        account.setEnabled(true);
        account.setCreated(TEST_CREATED);
        account.setModified(TEST_MODIFIED);
        return account;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(CardEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getAccount().getUuid(), result.getAccountUuid());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getNumber(), result.getNumber());
        assertEquals(entity.getExpiration(), result.getExpiration());
        assertEquals(entity.getComment(), result.getComment());
        assertEquals(entity.isEnabled(), result.getEnabled());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(CardFlatDTO dto) {
        // Given
        var account = createAccount();

        // When
        var result = converter.dtoToEntity(dto, account);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(account, result.getAccount());
        assertEquals(dto.getType(), result.getType());
        assertEquals(dto.getNumber(), result.getNumber());
        assertEquals(dto.getExpiration(), result.getExpiration());
        assertEquals(dto.getComment(), result.getComment());
        assertEquals(dto.getEnabled(), result.isEnabled());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(CardEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(CardFlatDTO dto) {
        // Given
        var account = createAccount();

        // When
        var result = converter.dtoToEntity(dto, account);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithNullAccount")
    void testEntityToFlatDtoWithNullAccount(CardEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNull(result.getAccountUuid());
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithDifferentCardTypes")
    void testDealsWithDifferentCardTypes(CardEntity entity, CardFlatDTO dto) {
        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto, createAccount());

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

    private static Object[] provideEntitysWithNullAccount() {
        var entity = new CardEntity();
        entity.setUuid(CARD_UUID);
        entity.setType(CardType.MASTERCARD);
        entity.setNumber("5500000012345678");
        entity.setExpiration(LocalDate.of(2027, 6, 30));
        entity.setComment("Card without account");
        entity.setEnabled(false);
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return new Object[]{entity};
    }

    private static Object[] provideDealsWithDifferentCardTypes() {
        // Entity with MIR type
        var entity1 = new CardEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setAccount(createAccount());
        entity1.setType(CardType.MIR);
        entity1.setNumber("2200000012345678");
        entity1.setExpiration(LocalDate.of(2029, 3, 31));
        entity1.setComment("MIR Card");
        entity1.setEnabled(true);
        entity1.setCreated(TEST_CREATED);
        entity1.setModified(TEST_MODIFIED);

        var dto1 = new CardFlatDTO()
                .uuid(entity1.getUuid())
                .accountUuid(ACCOUNT_UUID)
                .type(CardType.MIR)
                .number("2200000012345678")
                .expiration(LocalDate.of(2029, 3, 31))
                .comment("MIR Card")
                .enabled(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        // Entity with AMEX type
        var entity2 = new CardEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setAccount(createAccount());
        entity2.setType(CardType.AMEX);
        entity2.setNumber("3700000012345678");
        entity2.setExpiration(LocalDate.of(2030, 8, 31));
        entity2.setComment("AMEX Card");
        entity2.setEnabled(true);
        entity2.setCreated(TEST_CREATED);
        entity2.setModified(TEST_MODIFIED);

        var dto2 = new CardFlatDTO()
                .uuid(entity2.getUuid())
                .accountUuid(ACCOUNT_UUID)
                .type(CardType.AMEX)
                .number("3700000012345678")
                .expiration(LocalDate.of(2030, 8, 31))
                .comment("AMEX Card")
                .enabled(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2}
        };
    }
}
