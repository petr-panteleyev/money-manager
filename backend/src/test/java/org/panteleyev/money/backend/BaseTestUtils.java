/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import org.panteleyev.money.backend.openapi.dto.AccountFlatDto;
import org.panteleyev.money.backend.openapi.dto.CardFlatDto;
import org.panteleyev.money.backend.openapi.dto.CardType;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDto;
import org.panteleyev.money.backend.openapi.dto.CategoryType;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.openapi.dto.ContactType;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.backend.openapi.dto.IconFlatDto;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDto;
import org.panteleyev.money.backend.openapi.dto.TransactionType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

public final class BaseTestUtils {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    static String randomString() {
        return UUID.randomUUID().toString();
    }

    static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    static CategoryType randomCategoryType() {
        int index = RANDOM.nextInt(CategoryType.values().length);
        return CategoryType.values()[index];
    }

    static CardType randomCardType() {
        int index = RANDOM.nextInt(CardType.values().length);
        return CardType.values()[index];
    }

    static ContactType randomContactType() {
        int index = RANDOM.nextInt(ContactType.values().length);
        return ContactType.values()[index];
    }

    static TransactionType randomTransactionType() {
        int index = RANDOM.nextInt(TransactionType.values().length);
        return TransactionType.values()[index];
    }

    //
    // Icon
    //

    public static IconFlatDto newIconFlatDto(UUID uuid, String name, long created, long updated) {
        try (var inputStream = BaseTestUtils.class.getResourceAsStream("/images/" + name)) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot retrieve test resource");
            }
            var bytes = inputStream.readAllBytes();
            var dto = new IconFlatDto();
            dto.setUuid(uuid);
            dto.setName(name);
            dto.setBytes(bytes);
            dto.setCreated(created);
            dto.setModified(updated);
            return dto;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    //
    // Category
    //

    public static CategoryFlatDto newCategoryFlatDto(UUID uuid, UUID iconUuid, long created, long modified) {
        var dto = new CategoryFlatDto();
        dto.setUuid(uuid);
        dto.setName(randomString());
        dto.setComment(randomString());
        dto.setType(randomCategoryType());
        dto.setIconUuid(iconUuid);
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Account
    //

    public static AccountFlatDto newAccountFlatDto(UUID uuid, CategoryFlatDto category, CurrencyFlatDto currency,
            IconFlatDto icon, long created, long modified)
    {
        var dto = new AccountFlatDto();
        dto.setUuid(uuid);
        dto.setName(randomString());
        dto.setComment(randomString());
        dto.setAccountNumber(randomString());
        dto.setOpeningBalance(randomBigDecimal());
        dto.setAccountLimit(randomBigDecimal());
        dto.setCurrencyRate(randomBigDecimal());
        dto.setType(category.getType());
        dto.setCategoryUuid(category.getUuid());
        dto.setCurrencyUuid(currency == null ? null : currency.getUuid());
        // TODO: exchange security
        dto.setEnabled(RANDOM.nextBoolean());
        dto.setInterest(randomBigDecimal());
        dto.setClosingDate(LocalDate.now());
        dto.setIconUuid(icon == null ? null : icon.getUuid());
        dto.setTotal(randomBigDecimal());
        dto.setTotalWaiting(randomBigDecimal());
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Card
    //

    public static CardFlatDto newCardFlatDto(UUID uuid, UUID accountUuid, long created, long modified) {
        var dto = new CardFlatDto();
        dto.setUuid(uuid);
        dto.setAccountUuid(accountUuid);
        dto.setType(randomCardType());
        dto.setNumber(randomString());
        dto.setExpiration(LocalDate.now());
        dto.setComment(randomString());
        dto.setEnabled(RANDOM.nextBoolean());
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Currency
    //

    public static CurrencyFlatDto newCurrencyFlatDto(UUID uuid, long created, long modified) {
        var dto = new CurrencyFlatDto();
        dto.setUuid(uuid);
        dto.setSymbol(randomString());
        dto.setDescription(randomString());
        dto.setFormatSymbol(randomString());
        dto.setFormatSymbolPosition(RANDOM.nextInt(2));
        dto.setShowFormatSymbol(RANDOM.nextBoolean());
        dto.setDef(RANDOM.nextBoolean());
        dto.setRate(randomBigDecimal());
        dto.setDirection(RANDOM.nextInt(2));
        dto.setUseThousandSeparator(RANDOM.nextBoolean());
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Contact
    //

    public static ContactFlatDto newContactFlatDto(UUID uuid, UUID iconUuid, long created, long modified) {
        var dto = new ContactFlatDto();
        dto.setUuid(uuid);
        dto.setName(randomString());
        dto.setType(randomContactType());
        dto.setComment(randomString());
        dto.setPhone(randomString());
        dto.setMobile(randomString());
        dto.setEmail(randomString());
        dto.setWeb(randomString());
        dto.setStreet(randomString());
        dto.setCity(randomString());
        dto.setCountry(randomString());
        dto.setZip(randomString());
        dto.setIconUuid(iconUuid);
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Transaction
    //

    public static TransactionFlatDto newTransactionFlatDto(
            UUID uuid,
            AccountFlatDto debitedAccount,
            AccountFlatDto creditedAccount,
            ContactFlatDto contact,
            CardFlatDto card,
            long created,
            long modified)
    {
        var dto = new TransactionFlatDto();
        dto.setUuid(uuid);
        dto.setAmount(randomBigDecimal());
        dto.setCreditAmount(randomBigDecimal());
        dto.setTransactionDate(LocalDate.now());
        dto.setType(randomTransactionType());
        dto.setComment(randomString());
        dto.setChecked(randomBoolean());
        dto.setAccountDebitedUuid(debitedAccount.getUuid());
        dto.setAccountCreditedUuid(creditedAccount.getUuid());
        dto.setAccountDebitedType(debitedAccount.getType());
        dto.setAccountCreditedType(creditedAccount.getType());
        dto.setAccountDebitedCategoryUuid(debitedAccount.getCategoryUuid());
        dto.setAccountCreditedCategoryUuid(creditedAccount.getCategoryUuid());
        dto.setContactUuid(contact == null ? null : contact.getUuid());
        dto.setInvoiceNumber(randomString());
        dto.setParentUuid(null);                // TODO: later
        dto.setDetailed(randomBoolean());
        dto.setStatementDate(LocalDate.now());
        dto.setCardUuid(card == null ? null : card.getUuid());
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }
}
