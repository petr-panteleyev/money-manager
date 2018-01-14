/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyRecord;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.persistence.TransactionType;
import org.panteleyev.persistence.Record;
import org.testng.Assert;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaseTest {
    static final Random RANDOM = new Random(System.currentTimeMillis());

    int randomId() {
        return RANDOM.nextInt(Integer.MAX_VALUE) + 1;
    }

    int randomDay() {
        return 1 + RANDOM.nextInt(31);
    }

    int randomMonth() {
        return 1 + RANDOM.nextInt(12);
    }

    int randomYear() {
        return 1 + RANDOM.nextInt(3000);
    }

    BigDecimal randomBigDecimal() {
        return new BigDecimal(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    CategoryType randomCategoryType() {
        int id = 1 + RANDOM.nextInt(CategoryType.values().length - 1);
        return CategoryType.get(id);
    }

    ContactType randomContactType() {
        int id = 1 + RANDOM.nextInt(ContactType.values().length - 1);
        return ContactType.get(id);
    }

    TransactionType randomTransactionType() {
        while (true) {
            int id = 1 + RANDOM.nextInt(TransactionType.values().length - 1);
            TransactionType type = TransactionType.get(id);
            if (!type.isSeparator()) {
                return type;
            }
        }
    }

    Account newAccount() {
        return newAccount(randomId(), randomCategoryType(), randomId(), randomId());
    }

    Account newAccount(int id, Category category, Currency currency) {
        return newAccount(id, category.getType(), category.getId(), currency.getId());
    }

    Account newAccount(int id, CategoryType type, int categoryId, int currencyId) {
        return new Account(
                id,
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

    Category newCategory() {
        return newCategory(randomId(), randomCategoryType());
    }

    Category newCategory(int id, CategoryType type) {
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

    Currency newCurrency() {
        return newCurrency(randomId());
    }

    Currency newCurrency(int id) {
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

    Contact newContact() {
        return newContact(randomId());
    }

    Contact newContact(int id) {
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

    TransactionGroup newTransactionGroup() {
        return newTransactionGroup(randomId());
    }

    TransactionGroup newTransactionGroup(int id) {
        return new TransactionGroup(id,
                randomDay(),
                randomMonth(),
                randomYear(),
                RANDOM.nextBoolean(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    Transaction newTransaction() {
        return newTransaction(randomId(),
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

    Transaction newTransaction(
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

    Transaction newTransaction(
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

    Transaction newTransaction(
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


    Transaction newTransaction(
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

    Transaction newTransaction(
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

    void validateXML(InputStream input) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getClass().getResource("/org/panteleyev/money/xml/money.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(input));
    }

    static <T extends MoneyRecord> List<T> sortedById(Collection<T> list) {
        return list.stream()
                .sorted(Comparator.comparingInt(Record::getId))
                .collect(Collectors.toList());
    }

    static void assertEmpty(Collection c) {
        Assert.assertTrue(c.isEmpty());
    }

    static <T extends Record> void assertRecords(Collection<T> c, Record... records) {
        Assert.assertEquals(c.size(), records.length);
        Assert.assertTrue(c.containsAll(List.of(records)));
    }
}
