/*
 Copyright © 2017-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.investment.InvestmentDeal;

import javax.xml.XMLConstants;
import javax.xml.transform.stax.StAXSource;
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

    public List<InvestmentDeal> getInvestmentDeals() {
        return investmentDeals;
    }

    public List<ExchangeSecuritySplit> getExchangeSecuritySplits() {
        return exchangeSecuritySplits;
    }

    public List<BlobContent> getBlobs() {
        return blobs;
    }

    public static void validate(InputStream inputStream) {
        try {
            if (moneySchema == null) {
                var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                moneySchema = factory.newSchema(Import.class.getResource(SCHEMA));
            }

            var validator = moneySchema.newValidator();
            try (var wrapper = XMLEventReaderWrapper.newInstance(inputStream)) {
                validator.validate(new StAXSource(wrapper.getReader()));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Import doImport(InputStream inputStream) {
        try (var reader = XMLEventReaderWrapper.newInstance(inputStream)) {
            var importParser = new ImportParser();

            while (reader.hasNext()) {
                var event = reader.nextEvent();
                event.asStartElement().ifPresent(importParser::onStartElement);
            }
            return new Import(importParser);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
