/*
 Copyright © 2017-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.test;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplitType;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentDealType;
import org.panteleyev.money.model.investment.InvestmentMarketType;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

public final class BaseTestUtils {
    public static final Random RANDOM = new Random(System.currentTimeMillis());

    public static int randomDay() {
        return 1 + RANDOM.nextInt(28);
    }

    public static int randomMonthNumber() {
        return 1 + RANDOM.nextInt(12);
    }

    public static Month randomMonth() {
        int index = RANDOM.nextInt(Month.values().length);
        return Month.values()[index];
    }

    public static int randomYear() {
        return 1 + RANDOM.nextInt(3000);
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    public static int randomInt() {
        return RANDOM.nextInt();
    }

    public static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    public static CategoryType randomCategoryType() {
        int index = RANDOM.nextInt(CategoryType.values().length);
        return CategoryType.values()[index];
    }

    public static CardType randomCardType() {
        int index = RANDOM.nextInt(CardType.values().length);
        return CardType.values()[index];
    }

    public static ContactType randomContactType() {
        int index = RANDOM.nextInt(ContactType.values().length);
        return ContactType.values()[index];
    }

    public static LocalDateTime randomLocalDateTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public static TransactionType randomTransactionType() {
        while (true) {
            int index = RANDOM.nextInt(TransactionType.values().length);
            var type = TransactionType.values()[index];
            if (!type.isSeparator()) {
                return type;
            }
        }
    }

    public static Account newAccount(Category category, Currency currency) {
        return new Account.Builder()
                .name(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .accountNumber(UUID.randomUUID().toString())
                .openingBalance(randomBigDecimal())
                .accountLimit(randomBigDecimal())
                .currencyRate(randomBigDecimal())
                .type(category.type())
                .categoryUuid(category.uuid())
                .currencyUuid(currency.uuid())
                .enabled(RANDOM.nextBoolean())
                .interest(randomBigDecimal())
                .closingDate(LocalDate.now())
                .uuid(UUID.randomUUID())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Account newAccount(Category category, Currency currency, Icon icon) {
        return new Account.Builder()
                .name(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .accountNumber(UUID.randomUUID().toString())
                .openingBalance(randomBigDecimal())
                .accountLimit(randomBigDecimal())
                .currencyRate(randomBigDecimal())
                .type(category.type())
                .categoryUuid(category.uuid())
                .currencyUuid(currency.uuid())
                .enabled(RANDOM.nextBoolean())
                .interest(randomBigDecimal())
                .closingDate(LocalDate.now())
                .iconUuid(icon.uuid())
                .uuid(UUID.randomUUID())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Category newCategory() {
        return newCategory(UUID.randomUUID(), randomCategoryType());
    }

    public static Category newCategory(Icon icon) {
        return newCategory(UUID.randomUUID(), icon.uuid());
    }

    public static Category newCategory(UUID uuid) {
        return newCategory(uuid, randomCategoryType());
    }

    public static Category newCategory(UUID uuid, UUID iconUuid) {
        return newCategory(uuid, randomCategoryType(), iconUuid);
    }

    public static Category newCategory(UUID uuid, CategoryType type) {
        return newCategory(uuid, type, null);
    }

    public static Category newCategory(UUID uuid, CategoryType type, UUID iconUuid) {
        return new Category.Builder()
                .name(UUID.randomUUID().toString())
                .comment(randomString())
                .type(type)
                .iconUuid(iconUuid)
                .uuid(uuid)
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Currency newCurrency() {
        return newCurrency(UUID.randomUUID());
    }

    public static Currency newCurrency(UUID uuid) {
        return new Currency.Builder()
                .symbol(randomString())
                .description(randomString())
                .formatSymbol(randomString())
                .formatSymbolPosition(RANDOM.nextInt(2))
                .showFormatSymbol(RANDOM.nextBoolean())
                .def(RANDOM.nextBoolean())
                .rate(randomBigDecimal())
                .direction(RANDOM.nextInt(2))
                .useThousandSeparator(RANDOM.nextBoolean())
                .uuid(uuid)
                .modified(System.currentTimeMillis())
                .build();
    }

    public static ExchangeSecurity newExchangeSecurity(UUID uuid) {
        return new ExchangeSecurity.Builder()
                .uuid(uuid)
                .secId(randomString())
                .name(randomString())
                .shortName(randomString())
                .isin(randomString())
                .regNumber(randomString())
                .faceValue(randomBigDecimal())
                .issueDate(LocalDate.now())
                .matDate(LocalDate.now())
                .daysToRedemption(randomInt())
                .group(randomString())
                .groupName(randomString())
                .type(randomString())
                .typeName(randomString())
                .marketValue(randomBigDecimal())
                .couponValue(randomBigDecimal())
                .couponPercent(randomBigDecimal())
                .couponDate(LocalDate.now())
                .couponFrequency(randomInt())
                .accruedInterest(randomBigDecimal())
                .couponPeriod(randomInt())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static ExchangeSecurity newExchangeSecurityShare(UUID uuid) {
        return new ExchangeSecurity.Builder()
                .uuid(uuid)
                .secId(randomString())
                .name(randomString())
                .shortName(randomString())
                .isin(randomString())
                .regNumber(randomString())
                .faceValue(randomBigDecimal())
                .issueDate(LocalDate.now())
                .daysToRedemption(randomInt())
                .group(randomString())
                .groupName(randomString())
                .type(randomString())
                .typeName(randomString())
                .marketValue(randomBigDecimal())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Contact newContact() {
        return newContact(UUID.randomUUID());
    }

    public static Contact newContact(UUID uuid) {
        return newContact(uuid, null);
    }

    public static Contact newContact(UUID uuid, UUID iconUuid) {
        return new Contact.Builder()
                .name(randomString())
                .type(randomContactType())
                .phone(randomString())
                .mobile(randomString())
                .email(randomString())
                .web(randomString())
                .comment(randomString())
                .street(randomString())
                .city(randomString())
                .country(randomString())
                .zip(randomString())
                .iconUuid(iconUuid)
                .uuid(uuid)
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Contact newContact(String name) {
        return new Contact.Builder()
                .name(name)
                .type(randomContactType())
                .phone(randomString())
                .mobile(randomString())
                .email(randomString())
                .web(randomString())
                .comment(randomString())
                .street(randomString())
                .city(randomString())
                .country(randomString())
                .zip(randomString())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Transaction newTransaction() {
        return newTransaction(UUID.randomUUID());
    }

    public static Transaction newTransaction(UUID uuid) {
        return new Transaction.Builder()
                .uuid(uuid)
                .amount(randomBigDecimal())
                .creditAmount(randomBigDecimal())
                .transactionDate(LocalDate.now())
                .type(randomTransactionType())
                .comment(randomString())
                .checked(RANDOM.nextBoolean())
                .accountDebitedUuid(UUID.randomUUID())
                .accountCreditedUuid(UUID.randomUUID())
                .accountDebitedType(randomCategoryType())
                .accountCreditedType(randomCategoryType())
                .accountDebitedCategoryUuid(UUID.randomUUID())
                .accountCreditedCategoryUuid(UUID.randomUUID())
                .contactUuid(UUID.randomUUID())
                .invoiceNumber(randomString())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Transaction newTransaction(Account accountDebited, Account accountCredited, Contact contact) {
        return new Transaction.Builder()
                .amount(randomBigDecimal())
                .creditAmount(randomBigDecimal())
                .transactionDate(LocalDate.now())
                .type(randomTransactionType())
                .comment(randomString())
                .checked(RANDOM.nextBoolean())
                .accountDebitedUuid(accountDebited.uuid())
                .accountCreditedUuid(accountCredited.uuid())
                .accountDebitedType(accountDebited.type())
                .accountCreditedType(accountCredited.type())
                .accountDebitedCategoryUuid(accountDebited.categoryUuid())
                .accountCreditedCategoryUuid(accountCredited.categoryUuid())
                .contactUuid(contact == null ? null : contact.uuid())
                .invoiceNumber(randomString())
                .uuid(UUID.randomUUID())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static Transaction newTransaction(Account accountDebited, Account accountCredited) {
        return newTransaction(accountDebited, accountCredited, null);
    }

    public static Icon newIcon(String name) {
        return newIcon(UUID.randomUUID(), name);
    }

    public static Icon newIcon(UUID uuid, String name) {
        try (var inputStream = BaseTestUtils.class.getResourceAsStream("/org/panteleyev/money/icons/" + name)) {
            var bytes = inputStream.readAllBytes();
            var timestamp = System.currentTimeMillis();
            return new Icon(uuid, name, bytes, timestamp, timestamp);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Card newCard(Account account) {
        return new Card.Builder()
                .uuid(UUID.randomUUID())
                .accountUuid(account.uuid())
                .type(randomCardType())
                .number(randomString())
                .expiration(LocalDate.now())
                .comment(randomString())
                .enabled(randomBoolean())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static InvestmentDeal newInvestment(
            Account account,
            ExchangeSecurity security,
            Currency currency)
    {
        return new InvestmentDeal.Builder()
                .accountUuid(account.uuid())
                .securityUuid(security.uuid())
                .currencyUuid(currency.uuid())
                .dealNumber(randomString())
                .dealDate(randomLocalDateTime())
                .accountingDate(randomLocalDateTime())
                .marketType(InvestmentMarketType.STOCK_MARKET)
                .operationType(InvestmentOperationType.PURCHASE)
                .securityAmount(randomInt())
                .price(randomBigDecimal())
                .aci(randomBigDecimal())
                .dealVolume(randomBigDecimal())
                .rate(randomBigDecimal())
                .exchangeFee(randomBigDecimal())
                .brokerFee(randomBigDecimal())
                .amount(randomBigDecimal())
                .dealType(InvestmentDealType.NORMAL)
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static ExchangeSecuritySplit newExchangeSecuritySplit(
            ExchangeSecurity exchangeSecurity)
    {
        return new ExchangeSecuritySplit.Builder()
                .uuid(UUID.randomUUID())
                .securityUuid(exchangeSecurity.uuid())
                .type(ExchangeSecuritySplitType.REVERSE_SPLIT)
                .rate(randomBigDecimal())
                .date(LocalDate.now())
                .comment(randomString())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();
    }

    public static byte[] randomBytes(int size) {
        var result = new byte[size];
        RANDOM.nextBytes(result);
        return result;
    }

    private BaseTestUtils() {
    }
}
