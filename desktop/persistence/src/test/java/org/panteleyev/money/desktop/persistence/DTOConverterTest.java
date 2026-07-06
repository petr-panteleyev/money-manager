// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.desktop.persistence;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.CardFlatDTO;
import org.panteleyev.money.dto.CardType;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.panteleyev.money.dto.CategoryType;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.ContactType;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitType;
import org.panteleyev.money.dto.IconFlatDTO;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.dto.InvestmentDealType;
import org.panteleyev.money.dto.InvestmentMarketType;
import org.panteleyev.money.dto.InvestmentOperationType;
import org.panteleyev.money.dto.TransactionFlatDTO;
import org.panteleyev.money.dto.TransactionType;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.ExchangeSecurity;
import org.panteleyev.money.model.ExchangeSecuritySplit;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.InvestmentDeal;
import org.panteleyev.money.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DTOConverterTest {

    private static final long TEST_CREATED = System.currentTimeMillis();
    private static final long TEST_MODIFIED = System.currentTimeMillis() + 1000;

    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID SECURITY_UUID = UUID.randomUUID();
    private static final UUID ICON_UUID = UUID.randomUUID();

    private static Account createAccount() {
        return new Account.Builder()
                .uuid(ACCOUNT_UUID)
                .name("Test Account")
                .comment("Test Account Comment")
                .accountNumber("1234567890")
                .openingBalance(BigDecimal.TEN)
                .accountLimit(BigDecimal.ONE)
                .currencyRate(BigDecimal.valueOf(1.5))
                .type(CategoryType.BANKS_AND_CASH)
                .categoryUuid(CATEGORY_UUID)
                .currencyUuid(CURRENCY_UUID)
                .securityUuid(SECURITY_UUID)
                .enabled(true)
                .interest(BigDecimal.valueOf(2.5))
                .closingDate(LocalDate.now())
                .iconUuid(null)
                .total(BigDecimal.valueOf(100.5))
                .totalWaiting(BigDecimal.valueOf(10.25))
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED)
                .build();
    }

    private static AccountFlatDTO createDto() {
        return new AccountFlatDTO()
                .uuid(ACCOUNT_UUID)
                .name("Test Account")
                .comment("Test Account Comment")
                .accountNumber("1234567890")
                .openingBalance(BigDecimal.TEN)
                .accountLimit(BigDecimal.ONE)
                .currencyRate(BigDecimal.valueOf(1.5))
                .type(CategoryType.BANKS_AND_CASH)
                .categoryUuid(CATEGORY_UUID)
                .currencyUuid(CURRENCY_UUID)
                .securityUuid(SECURITY_UUID)
                .enabled(true)
                .interest(BigDecimal.valueOf(2.5))
                .closingDate(LocalDate.now())
                .iconUuid(null)
                .total(BigDecimal.valueOf(100.5))
                .totalWaiting(BigDecimal.valueOf(10.25))
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testAccountAccountFromDTO() {
        // Given
        var account = createAccount();
        var dto = createDto();

        // When
        var result = DTOConverter.accountFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(account.uuid(), result.uuid());
        assertEquals(account.name(), result.name());
        assertEquals(account.comment(), result.comment());
        assertEquals(account.accountNumber(), result.accountNumber());
        assertEquals(account.openingBalance(), result.openingBalance());
        assertEquals(account.accountLimit(), result.accountLimit());
        assertEquals(account.currencyRate(), result.currencyRate());
        assertEquals(account.type(), result.type());
        assertEquals(account.categoryUuid(), result.categoryUuid());
        assertEquals(account.currencyUuid(), result.currencyUuid());
        assertEquals(account.securityUuid(), result.securityUuid());
        assertEquals(account.enabled(), result.enabled());
        assertEquals(account.interest(), result.interest());
        assertEquals(account.closingDate(), result.closingDate());
        assertEquals(account.iconUuid(), result.iconUuid());
        assertEquals(account.total(), result.total());
        assertEquals(account.totalWaiting(), result.totalWaiting());
        assertEquals(account.created(), result.created());
        assertEquals(account.modified(), result.modified());
    }

    @Test
    void testAccountAccountToDTO() {
        // Given
        var account = createAccount();

        // When
        var result = DTOConverter.accountToDTO(account);

        // Then
        assertNotNull(result);
        assertEquals(account.uuid(), result.getUuid());
        assertEquals(account.name(), result.getName());
        assertEquals(account.comment(), result.getComment());
        assertEquals(account.accountNumber(), result.getAccountNumber());
        assertEquals(account.openingBalance(), result.getOpeningBalance());
        assertEquals(account.accountLimit(), result.getAccountLimit());
        assertEquals(account.currencyRate(), result.getCurrencyRate());
        assertEquals(account.type().toString(), result.getType().toString());
        assertEquals(account.categoryUuid(), result.getCategoryUuid());
        assertEquals(account.currencyUuid(), result.getCurrencyUuid());
        assertEquals(account.securityUuid(), result.getSecurityUuid());
        assertEquals(account.enabled(), result.getEnabled());
        assertEquals(account.interest(), result.getInterest());
        assertEquals(account.closingDate(), result.getClosingDate());
        assertEquals(account.iconUuid(), result.getIconUuid());
        assertEquals(account.total(), result.getTotal());
        assertEquals(account.totalWaiting(), result.getTotalWaiting());
        assertEquals(account.created(), result.getCreated());
        assertEquals(account.modified(), result.getModified());
    }

    @Test
    void testAccountAccountFromDTOWithNull() {
        // When
        var result = DTOConverter.accountFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testAccountAccountToDTOWithNull() {
        // When
        var result = DTOConverter.accountToDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testIconAccountFromDTO() {
        // Given
        var icon = createIcon();
        var dto = createIconDto();

        // When
        var result = DTOConverter.iconFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(icon.uuid(), result.uuid());
        assertEquals(icon.name(), result.name());
        assertArrayEquals(icon.bytes(), result.bytes());
        assertEquals(icon.created(), result.created());
        assertEquals(icon.modified(), result.modified());
    }

    @Test
    void testIconAccountToDTO() {
        // Given
        var icon = createIcon();

        // When
        var result = DTOConverter.iconToDTO(icon);

        // Then
        assertNotNull(result);
        assertEquals(icon.uuid(), result.getUuid());
        assertEquals(icon.name(), result.getName());
        assertArrayEquals(icon.bytes(), result.getBytes());
        assertEquals(icon.created(), result.getCreated());
        assertEquals(icon.modified(), result.getModified());
    }

    @Test
    void testIconAccountFromDTOWithNull() {
        // When
        var result = DTOConverter.iconFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testIconAccountToDTOWithNull() {
        // When
        var result = DTOConverter.iconToDTO(null);

        // Then
        assertNull(result);
    }

    private static Icon createIcon() {
        var bytes = new byte[]{1, 2, 3, 4, 5};
        return new Icon(ICON_UUID, "Test Icon", bytes, TEST_CREATED, TEST_MODIFIED);
    }

    private static IconFlatDTO createIconDto() {
        var bytes = new byte[]{1, 2, 3, 4, 5};
        return new IconFlatDTO()
                .uuid(ICON_UUID)
                .name("Test Icon")
                .bytes(bytes)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    private static final UUID CATEGORY_FLAT_UUID = UUID.randomUUID();

    private static Category createCategory() {
        return new Category(
                CATEGORY_FLAT_UUID,
                "Test Category",
                "Test Category Comment",
                CategoryType.BANKS_AND_CASH,
                ICON_UUID,
                TEST_CREATED,
                TEST_MODIFIED
        );
    }

    private static CategoryFlatDTO createCategoryDto() {
        return new CategoryFlatDTO()
                .uuid(CATEGORY_FLAT_UUID)
                .name("Test Category")
                .comment("Test Category Comment")
                .type(CategoryType.BANKS_AND_CASH)
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testCategoryFromDTO() {
        // Given
        var category = createCategory();
        var dto = createCategoryDto();

        // When
        var result = DTOConverter.categoryFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(category.uuid(), result.uuid());
        assertEquals(category.name(), result.name());
        assertEquals(category.comment(), result.comment());
        assertEquals(category.type(), result.type());
        assertEquals(category.iconUuid(), result.iconUuid());
        assertEquals(category.created(), result.created());
        assertEquals(category.modified(), result.modified());
    }

    @Test
    void testCategoryToDTO() {
        // Given
        var category = createCategory();

        // When
        var result = DTOConverter.categoryToDTO(category);

        // Then
        assertNotNull(result);
        assertEquals(category.uuid(), result.getUuid());
        assertEquals(category.name(), result.getName());
        assertEquals(category.comment(), result.getComment());
        assertEquals(category.type().toString(), result.getType().toString());
        assertEquals(category.iconUuid(), result.getIconUuid());
        assertEquals(category.created(), result.getCreated());
        assertEquals(category.modified(), result.getModified());
    }

    @Test
    void testCategoryFromDTOWithNull() {
        // When
        var result = DTOConverter.categoryFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testCategoryToDTOWithNull() {
        // When
        var result = DTOConverter.categoryToDTO(null);

        // Then
        assertNull(result);
    }

    private static final UUID CURRENCY_FLAT_UUID = UUID.randomUUID();
    private static final UUID CONTACT_FLAT_UUID = UUID.randomUUID();

    private static Currency createCurrency() {
        return new Currency(
                CURRENCY_FLAT_UUID,
                "USD",
                "US Dollar",
                "$",
                0,
                true,
                true,
                BigDecimal.valueOf(1.0),
                1,
                true,
                TEST_CREATED,
                TEST_MODIFIED
        );
    }

    private static CurrencyFlatDTO createCurrencyDto() {
        return new CurrencyFlatDTO()
                .uuid(CURRENCY_FLAT_UUID)
                .symbol("USD")
                .description("US Dollar")
                .formatSymbol("$")
                .formatSymbolPosition(0)
                .showFormatSymbol(true)
                .def(true)
                .rate(BigDecimal.valueOf(1.0))
                .direction(1)
                .useThousandSeparator(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testCurrencyFromDTO() {
        // Given
        var currency = createCurrency();
        var dto = createCurrencyDto();

        // When
        var result = DTOConverter.currencyFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(currency.uuid(), result.uuid());
        assertEquals(currency.symbol(), result.symbol());
        assertEquals(currency.description(), result.description());
        assertEquals(currency.formatSymbol(), result.formatSymbol());
        assertEquals(currency.formatSymbolPosition(), result.formatSymbolPosition());
        assertEquals(currency.showFormatSymbol(), result.showFormatSymbol());
        assertEquals(currency.def(), result.def());
        assertEquals(currency.rate(), result.rate());
        assertEquals(currency.direction(), result.direction());
        assertEquals(currency.useThousandSeparator(), result.useThousandSeparator());
        assertEquals(currency.created(), result.created());
        assertEquals(currency.modified(), result.modified());
    }

    @Test
    void testCurrencyToDTO() {
        // Given
        var currency = createCurrency();

        // When
        var result = DTOConverter.currencyToDTO(currency);

        // Then
        assertNotNull(result);
        assertEquals(currency.uuid(), result.getUuid());
        assertEquals(currency.symbol(), result.getSymbol());
        assertEquals(currency.description(), result.getDescription());
        assertEquals(currency.formatSymbol(), result.getFormatSymbol());
        assertEquals(currency.formatSymbolPosition(), result.getFormatSymbolPosition());
        assertEquals(currency.showFormatSymbol(), result.getShowFormatSymbol());
        assertEquals(currency.def(), result.getDef());
        assertEquals(currency.rate(), result.getRate());
        assertEquals(currency.direction(), result.getDirection());
        assertEquals(currency.useThousandSeparator(), result.getUseThousandSeparator());
        assertEquals(currency.created(), result.getCreated());
        assertEquals(currency.modified(), result.getModified());
    }

    @Test
    void testCurrencyFromDTOWithNull() {
        // When
        var result = DTOConverter.currencyFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testCurrencyToDTOWithNull() {
        // When
        var result = DTOConverter.currencyToDTO(null);

        // Then
        assertNull(result);
    }

    private static Contact createContact() {
        return new Contact(
                CONTACT_FLAT_UUID,
                "Test Contact",
                ContactType.CLIENT,
                "123-456-7890",
                "987-654-3210",
                "contact@example.com",
                "https://example.com",
                "Test Contact Comment",
                "123 Main St",
                "Springfield",
                "USA",
                "12345",
                ICON_UUID,
                TEST_CREATED,
                TEST_MODIFIED
        );
    }

    private static ContactFlatDTO createContactDto() {
        return new ContactFlatDTO()
                .uuid(CONTACT_FLAT_UUID)
                .name("Test Contact")
                .type(ContactType.CLIENT)
                .phone("123-456-7890")
                .mobile("987-654-3210")
                .email("contact@example.com")
                .web("https://example.com")
                .comment("Test Contact Comment")
                .street("123 Main St")
                .city("Springfield")
                .country("USA")
                .zip("12345")
                .iconUuid(ICON_UUID)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testContactFromDTO() {
        // Given
        var contact = createContact();
        var dto = createContactDto();

        // When
        var result = DTOConverter.contactFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(contact.uuid(), result.uuid());
        assertEquals(contact.name(), result.name());
        assertEquals(contact.type(), result.type());
        assertEquals(contact.phone(), result.phone());
        assertEquals(contact.mobile(), result.mobile());
        assertEquals(contact.email(), result.email());
        assertEquals(contact.web(), result.web());
        assertEquals(contact.comment(), result.comment());
        assertEquals(contact.street(), result.street());
        assertEquals(contact.city(), result.city());
        assertEquals(contact.country(), result.country());
        assertEquals(contact.zip(), result.zip());
        assertEquals(contact.iconUuid(), result.iconUuid());
        assertEquals(contact.created(), result.created());
        assertEquals(contact.modified(), result.modified());
    }

    @Test
    void testContactToDTO() {
        // Given
        var contact = createContact();

        // When
        var result = DTOConverter.contactToDTO(contact);

        // Then
        assertNotNull(result);
        assertEquals(contact.uuid(), result.getUuid());
        assertEquals(contact.name(), result.getName());
        assertEquals(contact.type().toString(), result.getType().toString());
        assertEquals(contact.phone(), result.getPhone());
        assertEquals(contact.mobile(), result.getMobile());
        assertEquals(contact.email(), result.getEmail());
        assertEquals(contact.web(), result.getWeb());
        assertEquals(contact.comment(), result.getComment());
        assertEquals(contact.street(), result.getStreet());
        assertEquals(contact.city(), result.getCity());
        assertEquals(contact.country(), result.getCountry());
        assertEquals(contact.zip(), result.getZip());
        assertEquals(contact.iconUuid(), result.getIconUuid());
        assertEquals(contact.created(), result.getCreated());
        assertEquals(contact.modified(), result.getModified());
    }

    @Test
    void testContactFromDTOWithNull() {
        // When
        var result = DTOConverter.contactFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testContactToDTOWithNull() {
        // When
        var result = DTOConverter.contactToDTO(null);

        // Then
        assertNull(result);
    }

    private static final UUID CARD_UUID = UUID.randomUUID();
    private static final UUID CARD_ACCOUNT_UUID = UUID.randomUUID();

    private static Card createCard() {
        return new Card(
                CARD_UUID,
                CARD_ACCOUNT_UUID,
                CardType.VISA,
                "4111111111111111",
                LocalDate.of(2025, 12, 31),
                "Test Card Comment",
                true,
                TEST_CREATED,
                TEST_MODIFIED
        );
    }

    private static CardFlatDTO createCardDto() {
        return new CardFlatDTO()
                .uuid(CARD_UUID)
                .accountUuid(CARD_ACCOUNT_UUID)
                .type(org.panteleyev.money.dto.CardType.VISA)
                .number("4111111111111111")
                .expiration(LocalDate.of(2025, 12, 31))
                .comment("Test Card Comment")
                .enabled(true)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testCardFromDTO() {
        // Given
        var card = createCard();
        var dto = createCardDto();

        // When
        var result = DTOConverter.cardFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(card.uuid(), result.uuid());
        assertEquals(card.accountUuid(), result.accountUuid());
        assertEquals(card.type(), result.type());
        assertEquals(card.number(), result.number());
        assertEquals(card.expiration(), result.expiration());
        assertEquals(card.comment(), result.comment());
        assertEquals(card.enabled(), result.enabled());
        assertEquals(card.created(), result.created());
        assertEquals(card.modified(), result.modified());
    }

    @Test
    void testCardToDTO() {
        // Given
        var card = createCard();

        // When
        var result = DTOConverter.cardToDTO(card);

        // Then
        assertNotNull(result);
        assertEquals(card.uuid(), result.getUuid());
        assertEquals(card.accountUuid(), result.getAccountUuid());
        assertEquals(card.type().toString(), result.getType().toString());
        assertEquals(card.number(), result.getNumber());
        assertEquals(card.expiration(), result.getExpiration());
        assertEquals(card.comment(), result.getComment());
        assertEquals(card.enabled(), result.getEnabled());
        assertEquals(card.created(), result.getCreated());
        assertEquals(card.modified(), result.getModified());
    }

    @Test
    void testCardFromDTOWithNull() {
        // When
        var result = DTOConverter.cardFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testCardToDTOWithNull() {
        // When
        var result = DTOConverter.cardToDTO(null);

        // Then
        assertNull(result);
    }

    private static final UUID TRANSACTION_FLAT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_DEBITED_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_CREDITED_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_DEBITED_CATEGORY_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_CREDITED_CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID PARENT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_CARD_UUID = UUID.randomUUID();

    private static Transaction createTransaction() {
        return new Transaction(
                TRANSACTION_FLAT_UUID,
                BigDecimal.TEN,
                BigDecimal.ONE,
                LocalDate.of(2025, 6, 15),
                TransactionType.PURCHASE,
                "Test Transaction Comment",
                true,
                ACCOUNT_DEBITED_UUID,
                ACCOUNT_CREDITED_UUID,
                CategoryType.BANKS_AND_CASH,
                CategoryType.BANKS_AND_CASH,
                ACCOUNT_DEBITED_CATEGORY_UUID,
                ACCOUNT_CREDITED_CATEGORY_UUID,
                CONTACT_UUID,
                "INV-001",
                PARENT_UUID,
                true,
                LocalDate.of(2025, 6, 15),
                TRANSACTION_CARD_UUID,
                "Test Location",
                TEST_CREATED,
                TEST_MODIFIED
        );
    }

    private static TransactionFlatDTO createTransactionDto() {
        return new TransactionFlatDTO()
                .uuid(TRANSACTION_FLAT_UUID)
                .amount(BigDecimal.TEN)
                .creditAmount(BigDecimal.ONE)
                .transactionDate(LocalDate.of(2025, 6, 15))
                .type(org.panteleyev.money.dto.TransactionType.PURCHASE)
                .comment("Test Transaction Comment")
                .checked(true)
                .accountDebitedUuid(ACCOUNT_DEBITED_UUID)
                .accountCreditedUuid(ACCOUNT_CREDITED_UUID)
                .accountDebitedType(CategoryType.BANKS_AND_CASH)
                .accountCreditedType(CategoryType.BANKS_AND_CASH)
                .accountDebitedCategoryUuid(ACCOUNT_DEBITED_CATEGORY_UUID)
                .accountCreditedCategoryUuid(ACCOUNT_CREDITED_CATEGORY_UUID)
                .contactUuid(CONTACT_UUID)
                .invoiceNumber("INV-001")
                .parentUuid(PARENT_UUID)
                .detailed(true)
                .statementDate(LocalDate.of(2025, 6, 15))
                .cardUuid(TRANSACTION_CARD_UUID)
                .location("Test Location")
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testTransactionFromDTO() {
        // Given
        var transaction = createTransaction();
        var dto = createTransactionDto();

        // When
        var result = DTOConverter.transactionFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(transaction.uuid(), result.uuid());
        assertEquals(transaction.amount(), result.amount());
        assertEquals(transaction.creditAmount(), result.creditAmount());
        assertEquals(transaction.transactionDate(), result.transactionDate());
        assertEquals(transaction.type(), result.type());
        assertEquals(transaction.comment(), result.comment());
        assertEquals(transaction.checked(), result.checked());
        assertEquals(transaction.accountDebitedUuid(), result.accountDebitedUuid());
        assertEquals(transaction.accountCreditedUuid(), result.accountCreditedUuid());
        assertEquals(transaction.accountDebitedType(), result.accountDebitedType());
        assertEquals(transaction.accountCreditedType(), result.accountCreditedType());
        assertEquals(transaction.accountDebitedCategoryUuid(), result.accountDebitedCategoryUuid());
        assertEquals(transaction.accountCreditedCategoryUuid(), result.accountCreditedCategoryUuid());
        assertEquals(transaction.contactUuid(), result.contactUuid());
        assertEquals(transaction.invoiceNumber(), result.invoiceNumber());
        assertEquals(transaction.parentUuid(), result.parentUuid());
        assertEquals(transaction.detailed(), result.detailed());
        assertEquals(transaction.statementDate(), result.statementDate());
        assertEquals(transaction.cardUuid(), result.cardUuid());
        assertEquals(transaction.location(), result.location());
        assertEquals(transaction.created(), result.created());
        assertEquals(transaction.modified(), result.modified());
    }

    @Test
    void testTransactionToDTO() {
        // Given
        var transaction = createTransaction();

        // When
        var result = DTOConverter.transactionToDTO(transaction);

        // Then
        assertNotNull(result);
        assertEquals(transaction.uuid(), result.getUuid());
        assertEquals(transaction.amount(), result.getAmount());
        assertEquals(transaction.creditAmount(), result.getCreditAmount());
        assertEquals(transaction.transactionDate(), result.getTransactionDate());
        assertEquals(transaction.type().toString(), result.getType().toString());
        assertEquals(transaction.comment(), result.getComment());
        assertEquals(transaction.checked(), result.getChecked());
        assertEquals(transaction.accountDebitedUuid(), result.getAccountDebitedUuid());
        assertEquals(transaction.accountCreditedUuid(), result.getAccountCreditedUuid());
        assertEquals(transaction.accountDebitedType().toString(), result.getAccountDebitedType().toString());
        assertEquals(transaction.accountCreditedType().toString(), result.getAccountCreditedType().toString());
        assertEquals(transaction.accountDebitedCategoryUuid(), result.getAccountDebitedCategoryUuid());
        assertEquals(transaction.accountCreditedCategoryUuid(), result.getAccountCreditedCategoryUuid());
        assertEquals(transaction.contactUuid(), result.getContactUuid());
        assertEquals(transaction.invoiceNumber(), result.getInvoiceNumber());
        assertEquals(transaction.parentUuid(), result.getParentUuid());
        assertEquals(transaction.detailed(), result.getDetailed());
        assertEquals(transaction.statementDate(), result.getStatementDate());
        assertEquals(transaction.cardUuid(), result.getCardUuid());
        assertEquals(transaction.location(), result.getLocation());
        assertEquals(transaction.created(), result.getCreated());
        assertEquals(transaction.modified(), result.getModified());
    }

    @Test
    void testTransactionFromDTOWithNull() {
        // When
        var result = DTOConverter.transactionFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testTransactionToDTOWithNull() {
        // When
        var result = DTOConverter.transactionToDTO(null);

        // Then
        assertNull(result);
    }

    private static final UUID INVESTMENT_DEAL_UUID = UUID.randomUUID();
    private static final UUID INVESTMENT_DEAL_ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID INVESTMENT_DEAL_SECURITY_UUID = UUID.randomUUID();
    private static final UUID INVESTMENT_DEAL_CURRENCY_UUID = UUID.randomUUID();

    private static InvestmentDeal createInvestmentDeal() {
        return new InvestmentDeal.Builder()
                .uuid(INVESTMENT_DEAL_UUID)
                .accountUuid(INVESTMENT_DEAL_ACCOUNT_UUID)
                .securityUuid(INVESTMENT_DEAL_SECURITY_UUID)
                .currencyUuid(INVESTMENT_DEAL_CURRENCY_UUID)
                .dealNumber("DEAL-001")
                .dealDate(java.time.LocalDateTime.of(2025, 6, 15, 10, 30))
                .accountingDate(java.time.LocalDateTime.of(2025, 6, 16, 12, 0))
                .marketType(InvestmentMarketType.STOCK_MARKET)
                .operationType(InvestmentOperationType.PURCHASE)
                .securityAmount(100)
                .price(BigDecimal.valueOf(150.50))
                .aci(BigDecimal.valueOf(5.25))
                .dealVolume(BigDecimal.valueOf(15050.00))
                .rate(BigDecimal.valueOf(1.0))
                .exchangeFee(BigDecimal.valueOf(10.00))
                .brokerFee(BigDecimal.valueOf(25.00))
                .amount(BigDecimal.valueOf(15075.25))
                .dealType(InvestmentDealType.NORMAL)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED)
                .build();
    }

    private static InvestmentDealFlatDTO createInvestmentDealDto() {
        return new InvestmentDealFlatDTO()
                .uuid(INVESTMENT_DEAL_UUID)
                .accountUuid(INVESTMENT_DEAL_ACCOUNT_UUID)
                .securityUuid(INVESTMENT_DEAL_SECURITY_UUID)
                .currencyUuid(INVESTMENT_DEAL_CURRENCY_UUID)
                .dealNumber("DEAL-001")
                .dealDate(java.time.LocalDateTime.of(2025, 6, 15, 10, 30))
                .accountingDate(java.time.LocalDateTime.of(2025, 6, 16, 12, 0))
                .marketType(org.panteleyev.money.dto.InvestmentMarketType.STOCK_MARKET)
                .operationType(org.panteleyev.money.dto.InvestmentOperationType.PURCHASE)
                .securityAmount(100)
                .price(BigDecimal.valueOf(150.50))
                .aci(BigDecimal.valueOf(5.25))
                .dealVolume(BigDecimal.valueOf(15050.00))
                .rate(BigDecimal.valueOf(1.0))
                .exchangeFee(BigDecimal.valueOf(10.00))
                .brokerFee(BigDecimal.valueOf(25.00))
                .amount(BigDecimal.valueOf(15075.25))
                .dealType(org.panteleyev.money.dto.InvestmentDealType.NORMAL)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testInvestmentDealFromDTO() {
        // Given
        var deal = createInvestmentDeal();
        var dto = createInvestmentDealDto();

        // When
        var result = DTOConverter.investmentDealFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(deal.uuid(), result.uuid());
        assertEquals(deal.accountUuid(), result.accountUuid());
        assertEquals(deal.securityUuid(), result.securityUuid());
        assertEquals(deal.currencyUuid(), result.currencyUuid());
        assertEquals(deal.dealNumber(), result.dealNumber());
        assertEquals(deal.dealDate(), result.dealDate());
        assertEquals(deal.accountingDate(), result.accountingDate());
        assertEquals(deal.marketType(), result.marketType());
        assertEquals(deal.operationType(), result.operationType());
        assertEquals(deal.securityAmount(), result.securityAmount());
        assertEquals(deal.price(), result.price());
        assertEquals(deal.aci(), result.aci());
        assertEquals(deal.dealVolume(), result.dealVolume());
        assertEquals(deal.rate(), result.rate());
        assertEquals(deal.exchangeFee(), result.exchangeFee());
        assertEquals(deal.brokerFee(), result.brokerFee());
        assertEquals(deal.amount(), result.amount());
        assertEquals(deal.dealType(), result.dealType());
        assertEquals(deal.created(), result.created());
        assertEquals(deal.modified(), result.modified());
    }

    @Test
    void testInvestmentDealToDTO() {
        // Given
        var deal = createInvestmentDeal();

        // When
        var result = DTOConverter.investmentDealToDTO(deal);

        // Then
        assertNotNull(result);
        assertEquals(deal.uuid(), result.getUuid());
        assertEquals(deal.accountUuid(), result.getAccountUuid());
        assertEquals(deal.securityUuid(), result.getSecurityUuid());
        assertEquals(deal.currencyUuid(), result.getCurrencyUuid());
        assertEquals(deal.dealNumber(), result.getDealNumber());
        assertEquals(deal.dealDate(), result.getDealDate());
        assertEquals(deal.accountingDate(), result.getAccountingDate());
        assertEquals(deal.marketType().name(), result.getMarketType().name());
        assertEquals(deal.operationType().name(), result.getOperationType().name());
        assertEquals(deal.securityAmount(), result.getSecurityAmount());
        assertEquals(deal.price(), result.getPrice());
        assertEquals(deal.aci(), result.getAci());
        assertEquals(deal.dealVolume(), result.getDealVolume());
        assertEquals(deal.rate(), result.getRate());
        assertEquals(deal.exchangeFee(), result.getExchangeFee());
        assertEquals(deal.brokerFee(), result.getBrokerFee());
        assertEquals(deal.amount(), result.getAmount());
        assertEquals(deal.dealType().name(), result.getDealType().name());
        assertEquals(deal.created(), result.getCreated());
        assertEquals(deal.modified(), result.getModified());
    }

    @Test
    void testInvestmentDealFromDTOWithNull() {
        // When
        var result = DTOConverter.investmentDealFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testInvestmentDealToDTOWithNull() {
        // When
        var result = DTOConverter.investmentDealToDTO(null);

        // Then
        assertNull(result);
    }
    private static final UUID EXCHANGE_SECURITY_UUID = UUID.randomUUID();

    private static ExchangeSecurity createExchangeSecurity() {
        return new ExchangeSecurity.Builder()
                .uuid(EXCHANGE_SECURITY_UUID)
                .secId("RU000A100456")
                .name("Sberbank PJSC")
                .shortName("SBER")
                .isin("RU000A100456")
                .regNumber("1-01-0001-00001")
                .faceValue(BigDecimal.valueOf(10.0))
                .issueDate(LocalDate.of(1994, 7, 19))
                .matDate(LocalDate.of(2030, 12, 31))
                .daysToRedemption(2345)
                .group("equities")
                .groupName("Equities")
                .type("share")
                .typeName("Share")
                .marketValue(BigDecimal.valueOf(350.75))
                .couponValue(BigDecimal.valueOf(5.5))
                .couponPercent(BigDecimal.valueOf(5.5))
                .couponDate(LocalDate.of(2025, 7, 15))
                .couponFrequency(2)
                .accruedInterest(BigDecimal.valueOf(2.25))
                .couponPeriod(180)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED)
                .build();
    }

    private static ExchangeSecurityFlatDTO createExchangeSecurityDto() {
        return new ExchangeSecurityFlatDTO()
                .uuid(EXCHANGE_SECURITY_UUID)
                .secId("RU000A100456")
                .name("Sberbank PJSC")
                .shortName("SBER")
                .isin("RU000A100456")
                .regNumber("1-01-0001-00001")
                .faceValue(BigDecimal.valueOf(10.0))
                .issueDate(LocalDate.of(1994, 7, 19))
                .matDate(LocalDate.of(2030, 12, 31))
                .daysToRedemption(2345)
                .groupType("equities")
                .groupName("Equities")
                .type("share")
                .typeName("Share")
                .marketValue(BigDecimal.valueOf(350.75))
                .couponValue(BigDecimal.valueOf(5.5))
                .couponPercent(BigDecimal.valueOf(5.5))
                .couponDate(LocalDate.of(2025, 7, 15))
                .couponFrequency(2)
                .accruedInterest(BigDecimal.valueOf(2.25))
                .couponPeriod(180)
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testExchangeSecurityFromDTO() {
        // Given
        var security = createExchangeSecurity();
        var dto = createExchangeSecurityDto();

        // When
        var result = DTOConverter.exchangeSecurityFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(security.uuid(), result.uuid());
        assertEquals(security.secId(), result.secId());
        assertEquals(security.name(), result.name());
        assertEquals(security.shortName(), result.shortName());
        assertEquals(security.isin(), result.isin());
        assertEquals(security.regNumber(), result.regNumber());
        assertEquals(security.faceValue(), result.faceValue());
        assertEquals(security.issueDate(), result.issueDate());
        assertEquals(security.matDate(), result.matDate());
        assertEquals(security.daysToRedemption(), result.daysToRedemption());
        assertEquals(security.group(), result.group());
        assertEquals(security.groupName(), result.groupName());
        assertEquals(security.type(), result.type());
        assertEquals(security.typeName(), result.typeName());
        assertEquals(security.marketValue(), result.marketValue());
        assertEquals(security.couponValue(), result.couponValue());
        assertEquals(security.couponPercent(), result.couponPercent());
        assertEquals(security.couponDate(), result.couponDate());
        assertEquals(security.couponFrequency(), result.couponFrequency());
        assertEquals(security.accruedInterest(), result.accruedInterest());
        assertEquals(security.couponPeriod(), result.couponPeriod());
        assertEquals(security.created(), result.created());
        assertEquals(security.modified(), result.modified());
    }

    @Test
    void testExchangeSecurityToDTO() {
        // Given
        var security = createExchangeSecurity();

        // When
        var result = DTOConverter.exchangeSecurityToDTO(security);

        // Then
        assertNotNull(result);
        assertEquals(security.uuid(), result.getUuid());
        assertEquals(security.secId(), result.getSecId());
        assertEquals(security.name(), result.getName());
        assertEquals(security.shortName(), result.getShortName());
        assertEquals(security.isin(), result.getIsin());
        assertEquals(security.regNumber(), result.getRegNumber());
        assertEquals(security.faceValue(), result.getFaceValue());
        assertEquals(security.issueDate(), result.getIssueDate());
        assertEquals(security.matDate(), result.getMatDate());
        assertEquals(security.daysToRedemption(), result.getDaysToRedemption());
        assertEquals(security.group(), result.getGroupType());
        assertEquals(security.groupName(), result.getGroupName());
        assertEquals(security.type(), result.getType());
        assertEquals(security.typeName(), result.getTypeName());
        assertEquals(security.marketValue(), result.getMarketValue());
        assertEquals(security.couponValue(), result.getCouponValue());
        assertEquals(security.couponPercent(), result.getCouponPercent());
        assertEquals(security.couponDate(), result.getCouponDate());
        assertEquals(security.couponFrequency(), result.getCouponFrequency());
        assertEquals(security.accruedInterest(), result.getAccruedInterest());
        assertEquals(security.couponPeriod(), result.getCouponPeriod());
        assertEquals(security.created(), result.getCreated());
        assertEquals(security.modified(), result.getModified());
    }

    @Test
    void testExchangeSecurityFromDTOWithNull() {
        // When
        var result = DTOConverter.exchangeSecurityFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testExchangeSecurityToDTOWithNull() {
        // When
        var result = DTOConverter.exchangeSecurityToDTO(null);

        // Then
        assertNull(result);
    }
    private static final UUID EXCHANGE_SECURITY_SPLIT_UUID = UUID.randomUUID();
    private static final UUID EXCHANGE_SECURITY_SPLIT_SECURITY_UUID = UUID.randomUUID();

    private static ExchangeSecuritySplit createExchangeSecuritySplit() {
        return new ExchangeSecuritySplit.Builder()
                .uuid(EXCHANGE_SECURITY_SPLIT_UUID)
                .securityUuid(EXCHANGE_SECURITY_SPLIT_SECURITY_UUID)
                .type(ExchangeSecuritySplitType.SPLIT)
                .date(LocalDate.of(2025, 6, 15))
                .rate(BigDecimal.valueOf(2.0))
                .comment("Stock split 2-for-1")
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED)
                .build();
    }

    private static ExchangeSecuritySplitFlatDTO createExchangeSecuritySplitDto() {
        return new ExchangeSecuritySplitFlatDTO()
                .uuid(EXCHANGE_SECURITY_SPLIT_UUID)
                .securityUuid(EXCHANGE_SECURITY_SPLIT_SECURITY_UUID)
                .splitType(org.panteleyev.money.dto.ExchangeSecuritySplitType.SPLIT)
                .splitDate(LocalDate.of(2025, 6, 15))
                .rate(BigDecimal.valueOf(2.0))
                .comment("Stock split 2-for-1")
                .created(TEST_CREATED)
                .modified(TEST_MODIFIED);
    }

    @Test
    void testExchangeSecuritySplitFromDTO() {
        // Given
        var split = createExchangeSecuritySplit();
        var dto = createExchangeSecuritySplitDto();

        // When
        var result = DTOConverter.exchangeSecuritySplitFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(split.uuid(), result.uuid());
        assertEquals(split.securityUuid(), result.securityUuid());
        assertEquals(split.type(), result.type());
        assertEquals(split.date(), result.date());
        assertEquals(split.rate(), result.rate());
        assertEquals(split.comment(), result.comment());
        assertEquals(split.created(), result.created());
        assertEquals(split.modified(), result.modified());
    }

    @Test
    void testExchangeSecuritySplitToDTO() {
        // Given
        var split = createExchangeSecuritySplit();

        // When
        var result = DTOConverter.exchangeSecuritySplitToDTO(split);

        // Then
        assertNotNull(result);
        assertEquals(split.uuid(), result.getUuid());
        assertEquals(split.securityUuid(), result.getSecurityUuid());
        assertEquals(split.type().name(), result.getSplitType().name());
        assertEquals(split.date(), result.getSplitDate());
        assertEquals(split.rate(), result.getRate());
        assertEquals(split.comment(), result.getComment());
        assertEquals(split.created(), result.getCreated());
        assertEquals(split.modified(), result.getModified());
    }

    @Test
    void testExchangeSecuritySplitFromDTOWithNull() {
        // When
        var result = DTOConverter.exchangeSecuritySplitFromDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void testExchangeSecuritySplitToDTOWithNull() {
        // When
        var result = DTOConverter.exchangeSecuritySplitToDTO(null);

        // Then
        assertNull(result);
    }
}
