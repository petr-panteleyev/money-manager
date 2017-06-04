/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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
package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.persistence.TransactionType;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

class BaseTest {
    static final Random RANDOM = new Random(System.currentTimeMillis());

    CategoryType randomCategoryType() {
        Integer id = 1 + RANDOM.nextInt(CategoryType.values().length - 1);
        return CategoryType.get(id);
    }

    ContactType randomContactType() {
        Integer id = 1 + RANDOM.nextInt(ContactType.values().length - 1);
        return ContactType.get(id);
    }

    TransactionType randomTransactionType() {
        Integer id = 1 + RANDOM.nextInt(TransactionType.values().length - 1);
        return TransactionType.get(id);
    }

    Account newAccount() {
        return newAccount(RANDOM.nextInt(), randomCategoryType(), RANDOM.nextInt(), RANDOM.nextInt());
    }

    Account newAccount(Integer id, CategoryType type, Integer categoryId, Integer currencyId) {
        return new Account(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal(RANDOM.nextDouble()),
                new BigDecimal(RANDOM.nextDouble()),
                new BigDecimal(RANDOM.nextDouble()),
                type.getId(),
                categoryId,
                currencyId,
                RANDOM.nextBoolean()
        );
    }

    Category newCategory() {
        return newCategory(RANDOM.nextInt(), randomCategoryType());
    }

    Category newCategory(Integer id, CategoryType type) {
        return new Category(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                type.getId(),
                RANDOM.nextBoolean()
        );
    }

    Currency newCurrency() {
        return newCurrency(RANDOM.nextInt());
    }

    Currency newCurrency(Integer id) {
        return new Currency(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                RANDOM.nextInt(),
                RANDOM.nextBoolean(),
                RANDOM.nextBoolean(),
                new BigDecimal(RANDOM.nextDouble()),
                RANDOM.nextInt(),
                RANDOM.nextBoolean()
        );
    }

    Contact newContact() {
        return newContact(RANDOM.nextInt());
    }

    Contact newContact(Integer id) {
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
                UUID.randomUUID().toString()
        );
    }

    TransactionGroup newTransactionGroup() {
        return newTransactionGroup(RANDOM.nextInt());
    }

    TransactionGroup newTransactionGroup(Integer id) {
        return new TransactionGroup(id,
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextBoolean());
    }

    Transaction newTransaction(
            Integer id,
            TransactionType type,
            Integer accountDebitedId,
            Integer accountCreditedId,
            CategoryType accountDebitedType,
            CategoryType accountCreditedType,
            Integer accountDebitedCategoryId,
            Integer accountCreditedCategoryId,
            Integer groupId,
            Integer contactId)
    {
        return new Transaction(id,
                new BigDecimal(RANDOM.nextDouble()),
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextInt(),
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
                new BigDecimal(RANDOM.nextDouble()),
                RANDOM.nextInt(),
                UUID.randomUUID().toString()
        );
    }
}
