/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.ContactType;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionGroup;
import org.panteleyev.money.persistence.model.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.BaseTestUtils.randomDay;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.panteleyev.money.BaseTestUtils.randomMonth;
import static org.panteleyev.money.BaseTestUtils.randomYear;

public class PersistenceTestUtils {
    public static CategoryType randomCategoryType() {
        int id = 1 + RANDOM.nextInt(CategoryType.values().length - 1);
        return CategoryType.get(id);
    }

    public static ContactType randomContactType() {
        int id = 1 + RANDOM.nextInt(ContactType.values().length - 1);
        return ContactType.get(id);
    }

    public static TransactionType randomTransactionType() {
        while (true) {
            int id = 1 + RANDOM.nextInt(TransactionType.values().length - 1);
            TransactionType type = TransactionType.get(id);
            if (!type.isSeparator()) {
                return type;
            }
        }
    }

    public static Account newAccount() {
        return newAccount(randomId(), randomCategoryType(), randomId(), randomId());
    }

    public static Account newAccount(int id) {
        return newAccount(id, randomCategoryType(), randomId(), randomId());
    }

    public static Account newAccount(int id, Category category, Currency currency) {
        return newAccount(id, category.getType(), category.getId(), currency.getId());
    }

    public static Account newAccount(int id, CategoryType type, int categoryId, int currencyId) {
        return new Account(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomBigDecimal(),
                type.getId(),
                categoryId,
                currencyId,
                RANDOM.nextBoolean(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Category newCategory() {
        return newCategory(randomId(), randomCategoryType());
    }

    public static Category newCategory(int id) {
        return newCategory(id, randomCategoryType());
    }

    public static Category newCategory(int id, CategoryType type) {
        return new Category(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                type.getId(),
                RANDOM.nextBoolean(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Currency newCurrency() {
        return newCurrency(randomId());
    }

    public static Currency newCurrency(int id) {
        return new Currency(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                RANDOM.nextInt(2),
                RANDOM.nextBoolean(),
                RANDOM.nextBoolean(),
                randomBigDecimal(),
                RANDOM.nextInt(2),
                RANDOM.nextBoolean(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Contact newContact() {
        return newContact(randomId());
    }

    public static Contact newContact(int id) {
        return new Contact(
                id,
                UUID.randomUUID().toString(),
                randomContactType().getId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Contact newContact(int id, String name) {
        return new Contact(
                id,
                name,
                randomContactType().getId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static TransactionGroup newTransactionGroup() {
        return newTransactionGroup(randomId());
    }

    public static TransactionGroup newTransactionGroup(int id) {
        return new TransactionGroup(id,
                randomDay(),
                randomMonth(),
                randomYear(),
                RANDOM.nextBoolean(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Transaction newTransaction() {
        return newTransaction(randomId());
    }

    public static Transaction newTransaction(int id) {
        return newTransaction(id,
                randomBigDecimal(),
                randomBigDecimal(),
                randomTransactionType(),
                randomId(),
                randomId(),
                randomCategoryType(),
                randomCategoryType(),
                randomId(),
                randomId(),
                0,
                0);
    }

    public static Transaction newTransaction(
            Account accountDebited,
            Account accountCredited,
            Contact contact) {
        return newTransaction(randomId(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomTransactionType(),
                accountDebited.getId(),
                accountCredited.getId(),
                randomCategoryType(),
                randomCategoryType(),
                randomId(),
                randomId(),
                0,
                contact.getId());
    }

    public static Transaction newTransaction(
            Account accountDebited,
            Account accountCredited,
            TransactionGroup group) {
        return newTransaction(randomId(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomTransactionType(),
                accountDebited.getId(),
                accountCredited.getId(),
                randomCategoryType(),
                randomCategoryType(),
                randomId(),
                randomId(),
                group.getId(),
                0);
    }

    public static Transaction newTransaction(
            Account accountDebited,
            Account accountCredited,
            TransactionGroup group,
            Contact contact) {
        return newTransaction(randomId(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomTransactionType(),
                accountDebited.getId(),
                accountCredited.getId(),
                randomCategoryType(),
                randomCategoryType(),
                randomId(),
                randomId(),
                group.getId(),
                contact.getId());
    }


    public static Transaction newTransaction(
            int id,
            TransactionType type,
            int accountDebitedId,
            int accountCreditedId,
            CategoryType accountDebitedType,
            CategoryType accountCreditedType,
            int accountDebitedCategoryId,
            int accountCreditedCategoryId,
            int groupId,
            int contactId) {
        return new Transaction(id,
                randomBigDecimal(),
                randomDay(),
                randomMonth(),
                randomYear(),
                type.getId(),
                UUID.randomUUID().toString(),
                RANDOM.nextBoolean(),
                accountDebitedId,
                accountCreditedId,
                accountDebitedType.getId(),
                accountCreditedType.getId(),
                accountDebitedCategoryId,
                accountCreditedCategoryId,
                groupId,
                contactId,
                randomBigDecimal(),
                RANDOM.nextInt(2),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    public static Transaction newTransaction(
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
            int groupId,
            int contactId) {
        return new Transaction(id,
                amount,
                randomDay(),
                randomMonth(),
                randomYear(),
                type.getId(),
                UUID.randomUUID().toString(),
                RANDOM.nextBoolean(),
                accountDebitedId,
                accountCreditedId,
                accountDebitedType.getId(),
                accountCreditedType.getId(),
                accountDebitedCategoryId,
                accountCreditedCategoryId,
                groupId,
                contactId,
                rate,
                RANDOM.nextInt(2),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }
}
