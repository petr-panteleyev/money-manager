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

package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Import {
    private static final String SCHEMA = "/org/panteleyev/money/xml/money.xsd";

    private final List<Category> categories = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final List<Currency> currencies = new ArrayList<>();
    private final List<TransactionGroup> transactionGroups = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private static Schema moneySchema = null;

    private Import(Export export) {
        categories.addAll(export.getCategories().stream()
                .map(CategoryXml::toCategory).collect(Collectors.toList()));
        accounts.addAll(export.getAccounts().stream()
                .map(AccountXml::toAccount).collect(Collectors.toList()));
        contacts.addAll(export.getContacts().stream()
                .map(ContactXml::toContact).collect(Collectors.toList()));
        currencies.addAll(export.getCurrencies().stream()
                .map(CurrencyXml::toCurrency).collect(Collectors.toList()));
        transactionGroups.addAll(export.getTransactionGroups().stream()
                .map(TransactionGroupXml::toTransactionGroup).collect(Collectors.toList()));
        transactions.addAll(export.getTransactions().stream()
                .map(TransactionXml::toTransaction).collect(Collectors.toList()));
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
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                moneySchema = factory.newSchema(Import.class.getResource(SCHEMA));
            }

            JAXBContext ctx = JAXBContext.newInstance(Export.class, AccountXml.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            unmarshaller.setSchema(moneySchema);

            Export export = (Export) unmarshaller.unmarshal(inStream);
            return new Import(export);
        } catch (JAXBException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }
}
