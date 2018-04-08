/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.List;

public class Import {
    private static final String SCHEMA = "/org/panteleyev/money/xml/money.xsd";

    private final List<Category> categories;
    private final List<Account> accounts;
    private final List<Contact> contacts;
    private final List<Currency> currencies;
    private final List<TransactionGroup> transactionGroups;
    private final List<Transaction> transactions;

    private static Schema moneySchema = null;

    private Import(ImportParser importParser) {
        categories = importParser.getCategories();
        accounts = importParser.getAccounts();
        contacts = importParser.getContacts();
        currencies = importParser.getCurrencies();
        transactionGroups = importParser.getTransactionGroups();
        transactions = importParser.getTransactions();
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

    public List<TransactionGroup> getTransactionGroups() {
        return transactionGroups;
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
