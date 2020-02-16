package org.panteleyev.money.xml;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.List;

public class Import {
    private static final String SCHEMA = "/org/panteleyev/money/xml/money.xsd";

    private final List<Icon> icons;
    private final List<Category> categories;
    private final List<Account> accounts;
    private final List<Contact> contacts;
    private final List<Currency> currencies;
    private final List<Transaction> transactions;

    private static Schema moneySchema = null;

    private Import(ImportParser importParser) {
        icons = importParser.getIcons();
        categories = importParser.getCategories();
        accounts = importParser.getAccounts();
        contacts = importParser.getContacts();
        currencies = importParser.getCurrencies();
        transactions = importParser.getTransactions();
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

    public List<Contact> getContacts() {
        return contacts;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public static Import doImport(InputStream inStream) {
        try {
            if (moneySchema == null) {
                var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                moneySchema = factory.newSchema(Import.class.getResource(SCHEMA));
            }

            var factory = SAXParserFactory.newInstance();
            factory.setSchema(moneySchema);
            factory.setValidating(true);
            var parser = factory.newSAXParser();

            var importParser = new ImportParser();
            parser.parse(inStream, importParser);

            return new Import(importParser);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
