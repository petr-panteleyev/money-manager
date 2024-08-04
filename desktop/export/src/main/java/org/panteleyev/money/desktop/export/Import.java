/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

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

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.List;

public class Import {
    private static final String SCHEMA = "/xsd/money.xsd";

    private final List<Icon> icons;
    private final List<Category> categories;
    private final List<Account> accounts;
    private final List<Card> cards;
    private final List<Contact> contacts;
    private final List<Currency> currencies;
    private final List<ExchangeSecurity> exchangeSecurities;
    private final List<Transaction> transactions;
    private final List<MoneyDocument> documents;
    private final List<PeriodicPayment> periodicPayments;
    private final List<InvestmentDeal> investmentDeals;
    private final List<ExchangeSecuritySplit> exchangeSecuritySplits;
    private final List<BlobContent> blobs;

    private static Schema moneySchema = null;

    private Import(ImportParser importParser) {
        icons = importParser.getIcons();
        categories = importParser.getCategories();
        accounts = importParser.getAccounts();
        cards = importParser.getCards();
        contacts = importParser.getContacts();
        currencies = importParser.getCurrencies();
        exchangeSecurities = importParser.getExchangeSecurities();
        transactions = importParser.getTransactions();
        documents = importParser.getDocuments();
        periodicPayments = importParser.getPeriodicPayments();
        investmentDeals = importParser.getInvestments();
        exchangeSecuritySplits = importParser.getExchangeSecuritySplits();
        blobs = importParser.getBlobs();
    }

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

    public List<InvestmentDeal> getInvestmentDeals() {
        return investmentDeals;
    }

    public List<ExchangeSecuritySplit> getExchangeSecuritySplits() {
        return exchangeSecuritySplits;
    }

    public List<BlobContent> getBlobs() {
        return blobs;
    }

    public static Import doImport(InputStream inputStream) {
        try {
            var importParser = new ImportParser();

            if (moneySchema == null) {
                var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                moneySchema = factory.newSchema(Import.class.getResource(SCHEMA));
            }

            var factory = SAXParserFactory.newInstance();
            factory.setSchema(moneySchema);
            factory.setValidating(false);
            var parser = factory.newSAXParser();

            parser.parse(inputStream, importParser);
            return new Import(importParser);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
