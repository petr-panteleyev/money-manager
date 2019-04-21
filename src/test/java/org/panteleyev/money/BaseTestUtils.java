/*
 * Copyright (c) 2018, 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.ContactType;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

public interface BaseTestUtils {
    Random RANDOM = new Random(System.currentTimeMillis());

    static int randomDay() {
        return 1 + RANDOM.nextInt(28);
    }

    static int randomMonth() {
        return 1 + RANDOM.nextInt(12);
    }

    static int randomYear() {
        return 1 + RANDOM.nextInt(3000);
    }

    static String randomString() {
        return UUID.randomUUID().toString();
    }

    static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    static BigDecimal randomBigDecimal() {
        return new BigDecimal(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    static int randomPort() {
        return RANDOM.nextInt(0xFFFF);
    }

    static CategoryType randomCategoryType() {
        int id = 1 + RANDOM.nextInt(CategoryType.values().length - 1);
        return CategoryType.get(id);
    }

    static ContactType randomContactType() {
        int id = 1 + RANDOM.nextInt(ContactType.values().length - 1);
        return ContactType.get(id);
    }

    static TransactionType randomTransactionType() {
        while (true) {
            int id = 1 + RANDOM.nextInt(TransactionType.values().length - 1);
            var type = TransactionType.get(id);
            if (!type.isSeparator()) {
                return type;
            }
        }
    }

    static Account newAccount(Category category, Currency currency) {
        return new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .openingBalance(randomBigDecimal())
            .accountLimit(randomBigDecimal())
            .currencyRate(randomBigDecimal())
            .typeId(category.getType().getId())
            .categoryUuid(category.getGuid())
            .currencyUuid(currency.getGuid())
            .enabled(RANDOM.nextBoolean())
            .interest(randomBigDecimal())
            .closingDate(LocalDate.now())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();
    }

    static Category newCategory() {
        return newCategory(UUID.randomUUID(), randomCategoryType());
    }

    static Category newCategory(UUID uuid) {
        return newCategory(uuid, randomCategoryType());
    }

    static Category newCategory(UUID uuid, CategoryType type) {
        return new Category.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .catTypeId(type.getId())
            .guid(uuid)
            .modified(System.currentTimeMillis())
            .build();
    }

    static Currency newCurrency() {
        return newCurrency(UUID.randomUUID());
    }

    static Currency newCurrency(UUID uuid) {
        return new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(RANDOM.nextInt(2))
            .showFormatSymbol(RANDOM.nextBoolean())
            .def(RANDOM.nextBoolean())
            .rate(randomBigDecimal())
            .direction(RANDOM.nextInt(2))
            .useThousandSeparator(RANDOM.nextBoolean())
            .guid(uuid)
            .modified(System.currentTimeMillis())
            .build();
    }

    static Contact newContact() {
        return newContact(UUID.randomUUID());
    }

    static Contact newContact(UUID uuid) {
        return new Contact.Builder()
            .name(UUID.randomUUID().toString())
            .typeId(randomContactType().getId())
            .phone(UUID.randomUUID().toString())
            .mobile(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .web(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .street(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .country(UUID.randomUUID().toString())
            .zip(UUID.randomUUID().toString())
            .guid(uuid)
            .modified(System.currentTimeMillis())
            .build();
    }

    static Contact newContact(String name) {
        return new Contact.Builder()
            .name(name)
            .typeId(randomContactType().getId())
            .phone(UUID.randomUUID().toString())
            .mobile(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .web(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .street(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .country(UUID.randomUUID().toString())
            .zip(UUID.randomUUID().toString())
            .guid(UUID.randomUUID())
            .created(System.currentTimeMillis())
            .modified(System.currentTimeMillis())
            .build();
    }

    static Transaction newTransaction() {
        return newTransaction(UUID.randomUUID());
    }

    static Transaction newTransaction(UUID uuid) {
        return new Transaction.Builder()
            .guid(uuid)
            .amount(randomBigDecimal())
            .day(randomDay())
            .month(randomMonth())
            .year(randomYear())
            .transactionTypeId(randomTransactionType().getId())
            .comment(UUID.randomUUID().toString())
            .checked(RANDOM.nextBoolean())
            .accountDebitedUuid(UUID.randomUUID())
            .accountCreditedUuid(UUID.randomUUID())
            .accountDebitedTypeId(randomCategoryType().getId())
            .accountCreditedTypeId(randomCategoryType().getId())
            .accountDebitedCategoryUuid(UUID.randomUUID())
            .accountCreditedCategoryUuid(UUID.randomUUID())
            .contactUuid(UUID.randomUUID())
            .rate(randomBigDecimal())
            .rateDirection(RANDOM.nextInt(2))
            .invoiceNumber(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();
    }

    static Transaction newTransaction(Account accountDebited, Account accountCredited, Contact contact) {
        return new Transaction.Builder()
            .amount(randomBigDecimal())
            .day(randomDay())
            .month(randomMonth())
            .year(randomYear())
            .transactionTypeId(randomTransactionType().getId())
            .comment(UUID.randomUUID().toString())
            .checked(RANDOM.nextBoolean())
            .accountDebitedUuid(accountDebited.getGuid())
            .accountCreditedUuid(accountCredited.getGuid())
            .accountDebitedTypeId(accountDebited.getType().getId())
            .accountCreditedTypeId(accountCredited.getType().getId())
            .accountDebitedCategoryUuid(accountDebited.getCategoryUuid())
            .accountCreditedCategoryUuid(accountCredited.getCategoryUuid())
            .contactUuid(contact == null ? null : contact.getGuid())
            .rate(randomBigDecimal())
            .rateDirection(RANDOM.nextInt(2))
            .invoiceNumber(UUID.randomUUID().toString())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();
    }

    static Transaction newTransaction(Account accountDebited, Account accountCredited) {
        return newTransaction(accountDebited, accountCredited, null);
    }

    /*
    static Transaction newTransaction(
        int id,
        TransactionType type,
        int accountDebitedId,
        int accountCreditedId,
        CategoryType accountDebitedType,
        CategoryType accountCreditedType,
        int accountDebitedCategoryId,
        int accountCreditedCategoryId,
        int contactId)
    {
        return new Transaction.Builder()
            .amount(randomBigDecimal())
            .day(randomDay())
            .month(randomMonth())
            .year(randomYear())
            .transactionTypeId(type.getId())
            .comment(UUID.randomUUID().toString())
            .checked(RANDOM.nextBoolean())
            .accountDebitedId(accountDebitedId)
            .accountCreditedId(accountCreditedId)
            .accountDebitedTypeId(accountDebitedType.getId())
            .accountCreditedTypeId(accountCreditedType.getId())
            .accountDebitedCategoryId(accountDebitedCategoryId)
            .accountCreditedCategoryId(accountCreditedCategoryId)
            .contactId(contactId)
            .rate(randomBigDecimal())
            .rateDirection(RANDOM.nextInt(2))
            .invoiceNumber(UUID.randomUUID().toString())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();
    }

     */

    /*
    static Transaction newTransaction(
        int id,
        BigDecimal amount,
        BigDecimal rate,
        TransactionType type,
        int accountDebitedId,
        int accountCreditedId,
        CategoryType accountDebitedType,
        CategoryType accountCreditedType,
        int accountDebitedCategoryId,
        int accountCreditedCategoryId,
        int contactId)
    {
        return new Transaction.Builder()
            .id(id)
            .amount(amount)
            .day(randomDay())
            .month(randomMonth())
            .year(randomYear())
            .transactionTypeId(type.getId())
            .comment(UUID.randomUUID().toString())
            .checked(RANDOM.nextBoolean())
            .accountDebitedId(accountDebitedId)
            .accountCreditedId(accountCreditedId)
            .accountDebitedTypeId(accountDebitedType.getId())
            .accountCreditedTypeId(accountCreditedType.getId())
            .accountDebitedCategoryId(accountDebitedCategoryId)
            .accountCreditedCategoryId(accountCreditedCategoryId)
            .contactId(contactId)
            .rate(rate)
            .rateDirection(RANDOM.nextInt(2))
            .invoiceNumber(UUID.randomUUID().toString())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();
    }

     */
}
