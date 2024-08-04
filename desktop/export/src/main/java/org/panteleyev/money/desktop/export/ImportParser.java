/*
 Copyright © 2018-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.panteleyev.money.desktop.commons.xml.RecordSerializer.deserializeRecord;

class ImportParser extends DefaultHandler {
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

        Tag(Function<Attributes, ?> parseMethod) {
            this.parseMethod = parseMethod;
        }

        private final Function<Attributes, ?> parseMethod;

        Function<Attributes, ?> getParseMethod() {
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

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        Tag.getTag(qName).ifPresent(tag -> {
            var list = RECORD_LISTS.get(tag);
            var record = tag.getParseMethod().apply(attributes);
            ((List<Object>) list).add(record);
        });
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    private static Icon parseIcon(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Icon.class);
    }

    private static Category parseCategory(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Category.class);
    }

    private static Account parseAccount(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Account.class);
    }

    private static Currency parseCurrency(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Currency.class);
    }

    private static ExchangeSecurity parseExchangeSecurity(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, ExchangeSecurity.class);
    }

    private static Contact parseContact(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Contact.class);
    }

    private static Transaction parseTransaction(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Transaction.class);
    }

    private static MoneyDocument parseDocument(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, MoneyDocument.class);
    }

    private static PeriodicPayment parsePeriodicPayment(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, PeriodicPayment.class);
    }

    private static Card parseCard(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, Card.class);
    }

    private static InvestmentDeal parseInvestmentDeal(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, InvestmentDeal.class);
    }

    private static ExchangeSecuritySplit parseExchangeSecuritySplit(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, ExchangeSecuritySplit.class);
    }

    private static BlobContent parseBlob(Attributes attributes) {
        return RecordSerializer.deserializeRecord(attributes, BlobContent.class);
    }
}
