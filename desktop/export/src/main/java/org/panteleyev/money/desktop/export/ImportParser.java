/*
 Copyright © 2018-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

import org.panteleyev.commons.xml.StartElementWrapper;
import org.panteleyev.money.desktop.commons.xml.RecordSerializer;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

class ImportParser {
    enum Tag {
        Icon(ImportParser::parseIcon),
        Category(ImportParser::parseCategory),
        Account(ImportParser::parseAccount),
        Card(ImportParser::parseCard),
        Currency(ImportParser::parseCurrency),
        ExchangeSecurity(ImportParser::parseExchangeSecurity),
        Contact(ImportParser::parseContact),
        Transaction(ImportParser::parseTransaction),
        MoneyDocument(ImportParser::parseDocument),
        PeriodicPayment(ImportParser::parsePeriodicPayment),
        InvestmentDeal(ImportParser::parseInvestmentDeal),
        ExchangeSecuritySplit(ImportParser::parseExchangeSecuritySplit),
        BlobContent(ImportParser::parseBlob);

        Tag(Function<StartElementWrapper, ?> parseMethod) {
            this.parseMethod = parseMethod;
        }

        private final Function<StartElementWrapper, ?> parseMethod;

        Function<StartElementWrapper, ?> getParseMethod() {
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

    private final List<Icon> icons = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final List<Currency> currencies = new ArrayList<>();
    private final List<ExchangeSecurity> exchangeSecurities = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<MoneyDocument> documents = new ArrayList<>();
    private final List<PeriodicPayment> periodicPayments = new ArrayList<>();
    private final List<InvestmentDeal> investmentDeals = new ArrayList<>();
    private final List<ExchangeSecuritySplit> exchangeSecuritySplits = new ArrayList<>();
    private final List<BlobContent> blobs = new ArrayList<>();

    private final Map<Tag, List<?>> RECORD_LISTS = Map.ofEntries(
            Map.entry(Tag.Icon, icons),
            Map.entry(Tag.Category, categories),
            Map.entry(Tag.Account, accounts),
            Map.entry(Tag.Card, cards),
            Map.entry(Tag.Currency, currencies),
            Map.entry(Tag.ExchangeSecurity, exchangeSecurities),
            Map.entry(Tag.Contact, contacts),
            Map.entry(Tag.Transaction, transactions),
            Map.entry(Tag.MoneyDocument, documents),
            Map.entry(Tag.PeriodicPayment, periodicPayments),
            Map.entry(Tag.InvestmentDeal, investmentDeals),
            Map.entry(Tag.ExchangeSecuritySplit, exchangeSecuritySplits),
            Map.entry(Tag.BlobContent, blobs)
    );

    public List<Icon> getIcons() {
        return icons;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public List<ExchangeSecurity> getExchangeSecurities() {
        return exchangeSecurities;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<MoneyDocument> getDocuments() {
        return documents;
    }

    public List<PeriodicPayment> getPeriodicPayments() {
        return periodicPayments;
    }

    public List<InvestmentDeal> getInvestments() {
        return investmentDeals;
    }

    public List<ExchangeSecuritySplit> getExchangeSecuritySplits() {
        return exchangeSecuritySplits;
    }

    public List<BlobContent> getBlobs() {
        return blobs;
    }

    public void onStartElement(StartElementWrapper element) {
        Tag.getTag(element.getName().getLocalPart()).ifPresent(tag -> {
            var list = RECORD_LISTS.get(tag);
            var record = tag.getParseMethod().apply(element);
            ((List<Object>) list).add(record);
        });
    }

    private static Icon parseIcon(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Icon.class);
    }

    private static Category parseCategory(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Category.class);
    }

    private static Account parseAccount(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Account.class);
    }

    private static Currency parseCurrency(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Currency.class);
    }

    private static ExchangeSecurity parseExchangeSecurity(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, ExchangeSecurity.class);
    }

    private static Contact parseContact(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Contact.class);
    }

    private static Transaction parseTransaction(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Transaction.class);
    }

    private static MoneyDocument parseDocument(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, MoneyDocument.class);
    }

    private static PeriodicPayment parsePeriodicPayment(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, PeriodicPayment.class);
    }

    private static Card parseCard(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, Card.class);
    }

    private static InvestmentDeal parseInvestmentDeal(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, InvestmentDeal.class);
    }

    private static ExchangeSecuritySplit parseExchangeSecuritySplit(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, ExchangeSecuritySplit.class);
    }

    private static BlobContent parseBlob(StartElementWrapper element) {
        return RecordSerializer.deserializeRecord(element, BlobContent.class);
    }
}
