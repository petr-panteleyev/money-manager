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

package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyRecord;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class ImportParser extends DefaultHandler {
    enum Tag {
        Category(ImportParser::parseCategory),
        Account(ImportParser::parseAccount),
        Currency(ImportParser::parseCurrency),
        Contact(ImportParser::parseContact),
        TransactionGroup(ImportParser::parseTransactionGroup),
        Transaction(ImportParser::parseTransaction);

        Tag(BiFunction<Integer, Map<String, String>, MoneyRecord> parseMethod) {
            this.parseMethod = parseMethod;
        }

        private BiFunction<Integer, Map<String, String>, MoneyRecord> parseMethod;

        BiFunction<Integer, Map<String, String>, MoneyRecord> getParseMethod() {
            return parseMethod;
        }

        static Optional<Tag> getTag(String name) {
            try {
                return Optional.of(Enum.valueOf(Tag.class, name));
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
    }

    private static final List<String> NAMES = Arrays.stream(Tag.values())
            .map(Tag::name)
            .collect(Collectors.toList());

    private final List<Category> categories = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final List<Currency> currencies = new ArrayList<>();
    private final List<TransactionGroup> transactionGroups = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private final Map<Tag, List<? extends MoneyRecord>> RECORD_LISTS = Map.ofEntries(
            Map.entry(Tag.Category, categories),
            Map.entry(Tag.Account, accounts),
            Map.entry(Tag.Currency, currencies),
            Map.entry(Tag.Contact, contacts),
            Map.entry(Tag.TransactionGroup, transactionGroups),
            Map.entry(Tag.Transaction, transactions)
    );

    private Map<String, String> tags = null;
    private int currentId = 0;
    private String currentCharacters = "";

    public List<Category> getCategories() {
        return categories;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public List<TransactionGroup> getTransactionGroups() {
        return transactionGroups;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (NAMES.contains(qName)) {
            currentId = Integer.parseInt(attributes.getValue("id"));
            tags = new HashMap<>();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        Tag.getTag(qName).ifPresentOrElse(tag -> {
            List<? extends MoneyRecord> list = RECORD_LISTS.get(tag);
            MoneyRecord record = tag.getParseMethod().apply(currentId, tags);
            ((List<MoneyRecord>) list).add(record);
            tags = null;
        }, () -> {
            if (tags != null) {
                tags.put(qName, currentCharacters);
                currentCharacters = "";
            }
        });
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        currentCharacters = new String(ch, start, length);
    }

    private static Category parseCategory(int id, Map<String, String> tags) {
        return new Category(id,
                tags.get("name"),
                tags.get("comment"),
                Integer.parseInt(tags.get("catTypeId")),
                Boolean.parseBoolean(tags.get("expanded")),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }

    private static Account parseAccount(int id, Map<String, String> tags) {
        return new Account(id,
                tags.get("name"),
                tags.get("comment"),
                new BigDecimal(tags.get("openingBalance")),
                new BigDecimal(tags.get("accountLimit")),
                new BigDecimal(tags.get("currencyRate")),
                Integer.parseInt(tags.get("typeId")),
                Integer.parseInt(tags.get("categoryId")),
                Integer.parseInt(tags.get("currencyId")),
                Boolean.parseBoolean(tags.get("enabled")),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }

    private static Currency parseCurrency(int id, Map<String, String> tags) {
        return new Currency(id,
                tags.get("symbol"),
                tags.get("description"),
                tags.get("formatSymbol"),
                Integer.parseInt(tags.get("formatSymbolPosition")),
                Boolean.parseBoolean(tags.get("showFormatSymbol")),
                Boolean.parseBoolean(tags.get("default")),
                new BigDecimal(tags.get("rate")),
                Integer.parseInt(tags.get("direction")),
                Boolean.parseBoolean(tags.get("useThousandSeparator")),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }

    private static Contact parseContact(int id, Map<String, String> tags) {
        return new Contact(id,
                tags.get("name"),
                Integer.parseInt(tags.get("typeId")),
                tags.get("phone"),
                tags.get("mobile"),
                tags.get("email"),
                tags.get("web"),
                tags.get("comment"),
                tags.get("street"),
                tags.get("city"),
                tags.get("country"),
                tags.get("zip"),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }

    private static TransactionGroup parseTransactionGroup(int id, Map<String, String> tags) {
        return new TransactionGroup(id,
                Integer.parseInt(tags.get("day")),
                Integer.parseInt(tags.get("month")),
                Integer.parseInt(tags.get("year")),
                Boolean.parseBoolean(tags.get("expanded")),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }

    private static Transaction parseTransaction(int id, Map<String, String> tags) {
        return new Transaction(id,
                new BigDecimal(tags.get("amount")),
                Integer.parseInt(tags.get("day")),
                Integer.parseInt(tags.get("month")),
                Integer.parseInt(tags.get("year")),
                Integer.parseInt(tags.get("transactionTypeId")),
                tags.get("comment"),
                Boolean.parseBoolean(tags.get("checked")),
                Integer.parseInt(tags.get("accountDebitedId")),
                Integer.parseInt(tags.get("accountCreditedId")),
                Integer.parseInt(tags.get("accountDebitedTypeId")),
                Integer.parseInt(tags.get("accountCreditedTypeId")),
                Integer.parseInt(tags.get("accountDebitedCategoryId")),
                Integer.parseInt(tags.get("accountCreditedCategoryId")),
                Integer.parseInt(tags.get("groupId")),
                Integer.parseInt(tags.get("contactId")),
                new BigDecimal(tags.get("rate")),
                Integer.parseInt(tags.get("rateDirection")),
                tags.get("invoiceNumber"),
                tags.get("guid"),
                Long.parseLong(tags.get("modified")));
    }
}
