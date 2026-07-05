// Copyright © 2021-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend;

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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    static int randomInt() {
        return RANDOM.nextInt(Integer.MAX_VALUE);
    }

    static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    static LocalDate randomDate() {
        return LocalDate.now();
    }

    static LocalDateTime randomDateTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    static <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
        var values = enumClass.getEnumConstants();
        return values[RANDOM.nextInt(values.length)];
    }

    //
    // Icon
    //

    public static IconFlatDTO newIconFlatDto(UUID uuid, String name, long created, long updated) {
        try (var inputStream = BaseTestUtils.class.getResourceAsStream("/images/" + name)) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot retrieve test resource");
            }
            var bytes = inputStream.readAllBytes();
            var dto = new IconFlatDTO();
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

    public static CategoryFlatDTO newCategoryFlatDto(UUID uuid, UUID iconUuid, long created, long modified) {
        var dto = new CategoryFlatDTO();
        dto.setUuid(uuid);
        dto.setName(randomString());
        dto.setComment(randomString());
        dto.setType(randomEnum(CategoryType.class));
        dto.setIconUuid(iconUuid);
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }

    //
    // Account
    //

    public static AccountFlatDTO newAccountFlatDto(UUID uuid, CategoryFlatDTO category, CurrencyFlatDTO currency,
            ExchangeSecurityFlatDTO security, IconFlatDTO icon, long created, long modified)
    {
        var dto = new AccountFlatDTO();
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
        dto.setSecurityUuid(security == null ? null : security.getUuid());
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

    public static CardFlatDTO newCardFlatDto(UUID uuid, UUID accountUuid, long created, long modified) {
        var dto = new CardFlatDTO();
        dto.setUuid(uuid);
        dto.setAccountUuid(accountUuid);
        dto.setType(randomEnum(CardType.class));
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

    public static CurrencyFlatDTO newCurrencyFlatDto(UUID uuid, long created, long modified) {
        var dto = new CurrencyFlatDTO();
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

    public static ContactFlatDTO newContactFlatDto(UUID uuid, UUID iconUuid, long created, long modified) {
        var dto = new ContactFlatDTO();
        dto.setUuid(uuid);
        dto.setName(randomString());
        dto.setType(randomEnum(ContactType.class));
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
    // Exchange Security
    //

    public static ExchangeSecurityFlatDTO newExchangeSecurityFlatDto(UUID uuid, long created, long modified) {
        return new ExchangeSecurityFlatDTO()
                .uuid(uuid)
                .secId(randomString())
                .name(randomString())
                .shortName(randomString())
                .isin(randomString())
                .regNumber(randomString())
                .faceValue(randomBigDecimal())
                .issueDate(randomDate())
                .matDate(randomDate())
                .daysToRedemption(randomInt())
                .groupType(randomString())
                .groupName(randomString())
                .type(randomString())
                .typeName(randomString())
                .marketValue(randomBigDecimal())
                .couponValue(randomBigDecimal())
                .couponPercent(randomBigDecimal())
                .couponDate(randomDate())
                .couponFrequency(randomInt())
                .accruedInterest(randomBigDecimal())
                .couponPeriod(randomInt())
                .created(created)
                .modified(modified);
    }

    //
    // Exchange Security Split
    //

    public static ExchangeSecuritySplitFlatDTO newExchangeSecuritySplitFlatDTO(UUID uuid, UUID securityUuid,
            long created, long modified)
    {
        return new ExchangeSecuritySplitFlatDTO()
                .uuid(uuid)
                .securityUuid(securityUuid)
                .splitType(randomEnum(ExchangeSecuritySplitType.class))
                .splitDate(randomDate())
                .rate(randomBigDecimal())
                .comment(randomString())
                .created(created)
                .modified(modified);
    }

    //
    // Investment Deal
    //

    public static InvestmentDealFlatDTO newInvestmentDealFlatDTO(
            UUID uuid,
            UUID accountUuid,
            UUID currencyUuid,
            UUID securityUuid,
            long created,
            long modified)
    {
        return new InvestmentDealFlatDTO()
                .uuid(uuid)
                .accountUuid(accountUuid)
                .securityUuid(securityUuid)
                .currencyUuid(currencyUuid)
                .dealNumber(randomString())
                .dealDate(randomDateTime())
                .accountingDate(randomDateTime())
                .marketType(randomEnum(InvestmentMarketType.class))
                .operationType(randomEnum(InvestmentOperationType.class))
                .securityAmount(randomInt())
                .price(randomBigDecimal())
                .aci(randomBigDecimal())
                .dealVolume(randomBigDecimal())
                .rate(randomBigDecimal())
                .exchangeFee(randomBigDecimal())
                .brokerFee(randomBigDecimal())
                .amount(randomBigDecimal())
                .dealType(randomEnum(InvestmentDealType.class))
                .created(created)
                .modified(modified);
    }

    //
    // Transaction
    //

    public static TransactionFlatDTO newTransactionFlatDto(
            UUID uuid,
            AccountFlatDTO debitedAccount,
            AccountFlatDTO creditedAccount,
            ContactFlatDTO contact,
            CardFlatDTO card,
            long created,
            long modified)
    {
        var dto = new TransactionFlatDTO();
        dto.setUuid(uuid);
        dto.setAmount(randomBigDecimal());
        dto.setCreditAmount(randomBigDecimal());
        dto.setTransactionDate(LocalDate.now());
        dto.setType(randomEnum(TransactionType.class));
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
        dto.setLocation(randomString());
        dto.setCreated(created);
        dto.setModified(modified);
        return dto;
    }
}
