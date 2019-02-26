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

package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.MoneyRecord;
import org.panteleyev.money.persistence.model.Transaction;
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
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

class ImportParser extends DefaultHandler {
    enum Tag {
        Category(ImportParser::parseCategory),
        Account(ImportParser::parseAccount),
        Currency(ImportParser::parseCurrency),
        Contact(ImportParser::parseContact),
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
    private final List<Transaction> transactions = new ArrayList<>();

    private final Map<Tag, List<? extends MoneyRecord>> RECORD_LISTS = Map.ofEntries(
        Map.entry(Tag.Category, categories),
        Map.entry(Tag.Account, accounts),
        Map.entry(Tag.Currency, currencies),
        Map.entry(Tag.Contact, contacts),
        Map.entry(Tag.Transaction, transactions)
    );

    private Map<String, String> tags = null;
    private int currentId = 0;
    private final StringBuilder currentCharacters = new StringBuilder();

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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (NAMES.contains(qName)) {
            currentId = parseInt(attributes.getValue("id"));
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
                tags.put(qName, currentCharacters.toString());
                currentCharacters.setLength(0);
            }
        });
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        currentCharacters.append(new String(ch, start, length));
    }

    private static Category parseCategory(int id, Map<String, String> tags) {
        return new Category.Builder(id)
            .name(tags.get("name"))
            .comment(tags.get("comment"))
            .catTypeId(parseInt(tags.get("catTypeId")))
            .guid(tags.get("guid"))
            .modified(Long.parseLong(tags.get("modified")))
            .build();
    }

    private static Account parseAccount(int id, Map<String, String> tags) {
        return new Account.Builder(id)
            .name(tags.get("name"))
            .comment(tags.get("comment"))
            .accountNumber(tags.get("accountNumber"))
            .openingBalance(new BigDecimal(tags.get("openingBalance")))
            .accountLimit(new BigDecimal(tags.get("accountLimit")))
            .currencyRate(new BigDecimal(tags.get("currencyRate")))
            .typeId(parseInt(tags.get("typeId")))
            .categoryId(parseInt(tags.get("categoryId")))
            .currencyId(parseInt(tags.get("currencyId")))
            .enabled(parseBoolean(tags.get("enabled")))
            .guid(tags.get("guid"))
            .modified(Long.parseLong(tags.get("modified")))
            .build();
    }

    private static Currency parseCurrency(int id, Map<String, String> tags) {
        return new Currency.Builder(id)
            .symbol(tags.get("symbol"))
            .description(tags.get("description"))
            .formatSymbol(tags.get("formatSymbol"))
            .formatSymbolPosition(parseInt(tags.get("formatSymbolPosition")))
            .showFormatSymbol(parseBoolean(tags.get("showFormatSymbol")))
            .def(parseBoolean(tags.get("default")))
            .rate(new BigDecimal(tags.get("rate")))
            .direction(parseInt(tags.get("direction")))
            .useThousandSeparator(parseBoolean(tags.get("useThousandSeparator")))
            .guid(tags.get("guid"))
            .modified(Long.parseLong(tags.get("modified")))
            .build();
    }

    private static Contact parseContact(int id, Map<String, String> tags) {
        return new Contact.Builder(id)
            .name(tags.get("name"))
            .typeId(parseInt(tags.get("typeId")))
            .phone(tags.get("phone"))
            .mobile(tags.get("mobile"))
            .email(tags.get("email"))
            .web(tags.get("web"))
            .comment(tags.get("comment"))
            .street(tags.get("street"))
            .city(tags.get("city"))
            .country(tags.get("country"))
            .zip(tags.get("zip"))
            .guid(tags.get("guid"))
            .modified(Long.parseLong(tags.get("modified")))
            .build();
    }

    private static Transaction parseTransaction(int id, Map<String, String> tags) {
        var parentIdObj = tags.get("parentId");
        var detailedObj = tags.get("detailed");
        return new Transaction.Builder()
            .id(id)
            .amount(new BigDecimal(tags.get("amount")))
            .day(parseInt(tags.get("day")))
            .month(parseInt(tags.get("month")))
            .year(parseInt(tags.get("year")))
            .transactionTypeId(parseInt(tags.get("transactionTypeId")))
            .comment(tags.get("comment"))
            .checked(parseBoolean(tags.get("checked")))
            .accountDebitedId(parseInt(tags.get("accountDebitedId")))
            .accountCreditedId(parseInt(tags.get("accountCreditedId")))
            .accountDebitedTypeId(parseInt(tags.get("accountDebitedTypeId")))
            .accountCreditedTypeId(parseInt(tags.get("accountCreditedTypeId")))
            .accountDebitedCategoryId(parseInt(tags.get("accountDebitedCategoryId")))
            .accountCreditedCategoryId(parseInt(tags.get("accountCreditedCategoryId")))
            .contactId(parseInt(tags.get("contactId")))
            .rate(new BigDecimal(tags.get("rate")))
            .rateDirection(parseInt(tags.get("rateDirection")))
            .invoiceNumber(tags.get("invoiceNumber"))
            .parentId(parentIdObj == null ? 0 : parseInt(tags.get("parentId")))
            .detailed(detailedObj != null && parseBoolean(tags.get("detailed")))
            .guid(tags.get("guid"))
            .modified(Long.parseLong(tags.get("modified")))
            .build();
    }
}
