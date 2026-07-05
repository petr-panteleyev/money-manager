// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.TransactionEntity;
import org.panteleyev.money.dto.CategoryType;
import org.panteleyev.money.dto.TransactionFlatDTO;
import org.panteleyev.money.dto.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionConverterTest {

    private final TransactionConverter converter = new TransactionConverter();

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_DEBITED_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_CREDITED_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID PARENT_UUID = UUID.randomUUID();
    private static final UUID CARD_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_DEBITED_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_CREDITED_UUID = UUID.randomUUID();

    private static TransactionEntity createEntity() {
        return createEntity(true, true, true, true);
    }

    private static TransactionEntity createEntity(boolean withContact, boolean withParent, boolean withCard,
            boolean withDetailed) {
        var entity = new TransactionEntity();
        entity.setUuid(TRANSACTION_UUID);
        entity.setAmount(BigDecimal.valueOf(1000.00));
        entity.setCreditAmount(BigDecimal.valueOf(500.00));
        entity.setTransactionDate(LocalDate.of(2026, 7, 5));
        entity.setType(TransactionType.CARD_PAYMENT);
        entity.setComment("Test Transaction Comment");
        entity.setChecked(true);
        entity.setAccountDebitedType(CategoryType.BANKS_AND_CASH);
        entity.setAccountCreditedType(CategoryType.BANKS_AND_CASH);
        entity.setAccountDebited(createAccountDebited());
        entity.setAccountCredited(createAccountCredited());
        entity.setAccountDebitedCategory(createCategoryDebited());
        entity.setAccountCreditedCategory(createCategoryCredited());
        if (withContact) {
            entity.setContact(createContact());
        }
        entity.setInvoiceNumber("INV-2026-001");
        if (withParent) {
            entity.setParent(createParent());
        }
        entity.setDetailed(withDetailed);
        entity.setStatementDate(LocalDate.of(2026, 7, 1));
        if (withCard) {
            entity.setCard(createCard());
        }
        entity.setLocation("Moscow, Russia");
        entity.setCreated(TEST_CREATED);
        entity.setModified(TEST_MODIFIED);
        return entity;
    }

    private static TransactionFlatDTO createDto() {
        return createDto(true, true, true, true);
    }

    private static TransactionFlatDTO createDto(boolean withContact, boolean withParent, boolean withCard,
            boolean withDetailed) {
        var dto = new TransactionFlatDTO()
                .uuid(TRANSACTION_UUID)
                .amount(BigDecimal.valueOf(1000.00))
                .creditAmount(BigDecimal.valueOf(500.00))
                .transactionDate(LocalDate.of(2026, 7, 5))
                .type(TransactionType.CARD_PAYMENT)
                .comment("Test Transaction Comment")
                .checked(true)
                .accountDebitedUuid(ACCOUNT_DEBITED_UUID)
                .accountCreditedUuid(ACCOUNT_CREDITED_UUID)
                .accountDebitedType(CategoryType.BANKS_AND_CASH)
                .accountCreditedType(CategoryType.BANKS_AND_CASH)
                .accountDebitedCategoryUuid(CATEGORY_DEBITED_UUID)
                .accountCreditedCategoryUuid(CATEGORY_CREDITED_UUID)
                .invoiceNumber("INV-2026-001")
                .detailed(withDetailed)
                .statementDate(LocalDate.of(2026, 7, 1))
                .location("Moscow, Russia")
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
        if (withContact) {
            dto.setContactUuid(CONTACT_UUID);
        }
        if (withParent) {
            dto.setParentUuid(PARENT_UUID);
        }
        if (withCard) {
            dto.setCardUuid(CARD_UUID);
        }
        return dto;
    }

    private static AccountEntity createAccountDebited() {
        var account = new AccountEntity();
        account.setUuid(ACCOUNT_DEBITED_UUID);
        account.setName("Debited Account");
        account.setComment("Debited Account Comment");
        account.setAccountNumber("40817810123456789012");
        account.setType(CategoryType.BANKS_AND_CASH);
        account.setCategory(createCategoryDebited());
        account.setEnabled(true);
        account.setCreated(TEST_CREATED);
        account.setModified(TEST_MODIFIED);
        return account;
    }

    private static AccountEntity createAccountCredited() {
        var account = new AccountEntity();
        account.setUuid(ACCOUNT_CREDITED_UUID);
        account.setName("Credited Account");
        account.setComment("Credited Account Comment");
        account.setAccountNumber("40817810987654321098");
        account.setType(CategoryType.BANKS_AND_CASH);
        account.setCategory(createCategoryCredited());
        account.setEnabled(true);
        account.setCreated(TEST_CREATED);
        account.setModified(TEST_MODIFIED);
        return account;
    }

    private static CategoryEntity createCategoryDebited() {
        var category = new CategoryEntity();
        category.setUuid(CATEGORY_DEBITED_UUID);
        category.setName("Debited Category");
        category.setType(CategoryType.BANKS_AND_CASH);
        category.setCreated(TEST_CREATED);
        category.setModified(TEST_MODIFIED);
        return category;
    }

    private static CategoryEntity createCategoryCredited() {
        var category = new CategoryEntity();
        category.setUuid(CATEGORY_CREDITED_UUID);
        category.setName("Credited Category");
        category.setType(CategoryType.BANKS_AND_CASH);
        category.setCreated(TEST_CREATED);
        category.setModified(TEST_MODIFIED);
        return category;
    }

    private static ContactEntity createContact() {
        var contact = new ContactEntity();
        contact.setUuid(CONTACT_UUID);
        contact.setName("Test Contact");
        contact.setType(org.panteleyev.money.dto.ContactType.PERSONAL);
        contact.setPhone("+7-123-456-78-90");
        contact.setMobile("+7-999-123-45-67");
        contact.setEmail("test@example.com");
        contact.setWeb("https://example.com");
        contact.setStreet("123 Main St");
        contact.setCity("New York");
        contact.setCountry("USA");
        contact.setZip("10001");
        contact.setCreated(TEST_CREATED);
        contact.setModified(TEST_MODIFIED);
        return contact;
    }

    private static TransactionEntity createParent() {
        var parent = new TransactionEntity();
        parent.setUuid(PARENT_UUID);
        parent.setAmount(BigDecimal.valueOf(2000.00));
        parent.setCreditAmount(BigDecimal.valueOf(1000.00));
        parent.setTransactionDate(LocalDate.of(2026, 7, 1));
        parent.setType(TransactionType.TRANSFER);
        parent.setComment("Parent Transaction");
        parent.setChecked(true);
        parent.setAccountDebitedType(CategoryType.BANKS_AND_CASH);
        parent.setAccountCreditedType(CategoryType.BANKS_AND_CASH);
        parent.setAccountDebited(createAccountDebited());
        parent.setAccountCredited(createAccountCredited());
        parent.setAccountDebitedCategory(createCategoryDebited());
        parent.setAccountCreditedCategory(createCategoryCredited());
        parent.setInvoiceNumber("INV-2026-000");
        parent.setDetailed(false);
        parent.setStatementDate(LocalDate.of(2026, 7, 1));
        parent.setLocation("Moscow, Russia");
        parent.setCreated(TEST_CREATED);
        parent.setModified(TEST_MODIFIED);
        return parent;
    }

    private static CardEntity createCard() {
        var card = new CardEntity();
        card.setUuid(CARD_UUID);
        card.setAccount(createAccountDebited());
        card.setType(org.panteleyev.money.dto.CardType.VISA);
        card.setNumber("4276870012345678");
        card.setExpiration(LocalDate.of(2028, 12, 31));
        card.setComment("Test Card");
        card.setEnabled(true);
        card.setCreated(TEST_CREATED);
        card.setModified(TEST_MODIFIED);
        return card;
    }

    @ParameterizedTest
    @MethodSource("provideEntitiesForConversion")
    void testEntityToFlatDto(TransactionEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getUuid(), result.getUuid());
        assertEquals(entity.getAmount(), result.getAmount());
        assertEquals(entity.getCreditAmount(), result.getCreditAmount());
        assertEquals(entity.getTransactionDate(), result.getTransactionDate());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getComment(), result.getComment());
        assertEquals(entity.isChecked(), result.getChecked());
        assertEquals(entity.getAccountDebited().getUuid(), result.getAccountDebitedUuid());
        assertEquals(entity.getAccountCredited().getUuid(), result.getAccountCreditedUuid());
        assertEquals(entity.getAccountDebitedCategory().getType(), result.getAccountDebitedType());
        assertEquals(entity.getAccountCreditedCategory().getType(), result.getAccountCreditedType());
        assertEquals(entity.getAccountDebitedCategory().getUuid(), result.getAccountDebitedCategoryUuid());
        assertEquals(entity.getAccountCreditedCategory().getUuid(), result.getAccountCreditedCategoryUuid());
        if (entity.getContact() != null) {
            assertEquals(entity.getContact().getUuid(), result.getContactUuid());
        } else {
            assertNull(result.getContactUuid());
        }
        assertEquals(entity.getInvoiceNumber(), result.getInvoiceNumber());
        if (entity.getParent() != null) {
            assertEquals(entity.getParent().getUuid(), result.getParentUuid());
        } else {
            assertNull(result.getParentUuid());
        }
        assertEquals(entity.isDetailed(), result.getDetailed());
        assertEquals(entity.getStatementDate(), result.getStatementDate());
        if (entity.getCard() != null) {
            assertEquals(entity.getCard().getUuid(), result.getCardUuid());
        } else {
            assertNull(result.getCardUuid());
        }
        assertEquals(entity.getLocation(), result.getLocation());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideDtosForConversion")
    void testDtoToEntity(TransactionFlatDTO dto) {
        // Given
        var accountDebited = createAccountDebited();
        var accountCredited = createAccountCredited();
        var contact = createContact();
        var parent = createParent();
        var card = createCard();

        // When
        var result = converter.dtoToEntity(dto, accountDebited, accountCredited, contact, parent, card);

        // Then
        assertNotNull(result);
        assertEquals(dto.getUuid(), result.getUuid());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getCreditAmount(), result.getCreditAmount());
        assertEquals(dto.getTransactionDate(), result.getTransactionDate());
        assertEquals(dto.getType(), result.getType());
        assertEquals(dto.getComment(), result.getComment());
        assertEquals(dto.getChecked(), result.isChecked());
        assertEquals(accountDebited, result.getAccountDebited());
        assertEquals(accountCredited, result.getAccountCredited());
        assertEquals(accountDebited.getCategory(), result.getAccountDebitedCategory());
        assertEquals(accountCredited.getCategory(), result.getAccountCreditedCategory());
        assertEquals(contact, result.getContact());
        assertEquals(parent, result.getParent());
        assertEquals(dto.getInvoiceNumber(), result.getInvoiceNumber());
        assertEquals(dto.getDetailed(), result.isDetailed());
        assertEquals(dto.getStatementDate(), result.getStatementDate());
        assertEquals(card, result.getCard());
        assertEquals(dto.getLocation(), result.getLocation());
        assertEquals(dto.getCreated(), result.getCreated());
        assertEquals(dto.getModified(), result.getModified());
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testEntityToFlatDtoWithNull(TransactionEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideNullValues")
    void testDtoToEntityWithNull(TransactionFlatDTO dto) {
        // Given
        var accountDebited = createAccountDebited();
        var accountCredited = createAccountCredited();
        var contact = createContact();
        var parent = createParent();
        var card = createCard();

        // When
        var result = converter.dtoToEntity(dto, accountDebited, accountCredited, contact, parent, card);

        // Then
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEntitysWithoutOptionalDependencies")
    void testEntityToFlatDtoWithoutOptionalDependencies(TransactionEntity entity) {
        // When
        var result = converter.entityToFlatDto(entity);

        // Then
        assertNotNull(result);
        assertNull(result.getContactUuid());
        assertNull(result.getParentUuid());
        assertNull(result.getCardUuid());
    }

    @ParameterizedTest
    @MethodSource("provideDtosWithOptionalDependencies")
    void testDtoToEntityWithOptionalDependencies(TransactionFlatDTO dto) {
        // Given
        var accountDebited = createAccountDebited();
        var accountCredited = createAccountCredited();

        // When
        var result = converter.dtoToEntity(dto, accountDebited, accountCredited, null, null, null);

        // Then
        assertNotNull(result);
        assertNull(result.getContact());
        assertNull(result.getParent());
        assertNull(result.getCard());
    }

    @ParameterizedTest
    @MethodSource("provideDealsWithDifferentTransactionTypes")
    void testDealsWithDifferentTransactionTypes(TransactionEntity entity, TransactionFlatDTO dto) {
        // When
        var resultDto = converter.entityToFlatDto(entity);
        var resultEntity = converter.dtoToEntity(dto, createAccountDebited(), createAccountCredited(), null, null, null);

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

    private static Object[] provideEntitysWithoutOptionalDependencies() {
        var entity = createEntity(false, false, false, false);
        return new Object[]{entity};
    }

    private static Object[] provideDtosWithOptionalDependencies() {
        var dto = createDto(false, false, false, false);
        return new Object[]{dto};
    }

    private static Object[] provideDealsWithDifferentTransactionTypes() {
        // Entity with WITHDRAWAL type
        var entity1 = createEntity();
        entity1.setUuid(UUID.randomUUID());
        entity1.setType(TransactionType.WITHDRAWAL);
        entity1.setAmount(BigDecimal.valueOf(5000.00));
        entity1.setCreditAmount(BigDecimal.valueOf(5000.00));
        entity1.setTransactionDate(LocalDate.of(2026, 7, 6));
        entity1.setComment("ATM Withdrawal");
        entity1.setInvoiceNumber("ATM-2026-001");
        entity1.setDetailed(true);
        entity1.setLocation("ATM Moscow");

        var dto1 = createDto();
        dto1.setUuid(entity1.getUuid());
        dto1.setType(TransactionType.WITHDRAWAL);
        dto1.setAmount(BigDecimal.valueOf(5000.00));
        dto1.setCreditAmount(BigDecimal.valueOf(5000.00));
        dto1.setTransactionDate(LocalDate.of(2026, 7, 6));
        dto1.setComment("ATM Withdrawal");
        dto1.setInvoiceNumber("ATM-2026-001");
        dto1.setDetailed(true);
        dto1.setLocation("ATM Moscow");

        // Entity with INCOME type
        var entity2 = createEntity();
        entity2.setUuid(UUID.randomUUID());
        entity2.setType(TransactionType.INCOME);
        entity2.setAmount(BigDecimal.valueOf(15000.00));
        entity2.setCreditAmount(BigDecimal.valueOf(0.00));
        entity2.setTransactionDate(LocalDate.of(2026, 7, 7));
        entity2.setComment("Salary Income");
        entity2.setInvoiceNumber("SAL-2026-001");
        entity2.setDetailed(true);
        entity2.setLocation("Office");

        var dto2 = createDto();
        dto2.setUuid(entity2.getUuid());
        dto2.setType(TransactionType.INCOME);
        dto2.setAmount(BigDecimal.valueOf(15000.00));
        dto2.setCreditAmount(BigDecimal.valueOf(0.00));
        dto2.setTransactionDate(LocalDate.of(2026, 7, 7));
        dto2.setComment("Salary Income");
        dto2.setInvoiceNumber("SAL-2026-001");
        dto2.setDetailed(true);
        dto2.setLocation("Office");

        return new Object[]{
                new Object[]{entity1, dto1},
                new Object[]{entity2, dto2}
        };
    }
}
