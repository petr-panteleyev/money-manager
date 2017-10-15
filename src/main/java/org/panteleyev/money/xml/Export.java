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
import org.panteleyev.money.persistence.RecordSource;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

@XmlRootElement(name = "Money")
public class Export {
    private final RecordSource source;

    private List<CategoryXml> categories = new ArrayList<>();
    private List<AccountXml> accounts = new ArrayList<>();
    private List<ContactXml> contacts = new ArrayList<>();
    private List<CurrencyXml> currencies = new ArrayList<>();
    private List<TransactionGroupXml> transactionGroups = new ArrayList<>();
    private List<TransactionXml> transactions = new ArrayList<>();

    public Export() {
        this(getDao());
    }

    public Export(RecordSource source) {
        this.source = source;
    }

    public RecordSource getSource() {
        return source;
    }

    @XmlElementWrapper(name = "Categories")
    @XmlElement(name = "Category")
    public List<CategoryXml> getCategories() {
        return categories;
    }

    @XmlElementWrapper(name = "Accounts")
    @XmlElement(name = "Account")
    public List<AccountXml> getAccounts() {
        return accounts;
    }

    @XmlElementWrapper(name = "Contacts")
    @XmlElement(name = "Contact")
    public List<ContactXml> getContacts() {
        return contacts;
    }

    @XmlElementWrapper(name = "Currencies")
    @XmlElement(name = "Currency")
    public List<CurrencyXml> getCurrencies() {
        return currencies;
    }

    @XmlElementWrapper(name = "TransactionGroups")
    @XmlElement(name = "TransactionGroup")
    public List<TransactionGroupXml> getTransactionGroups() {
        return transactionGroups;
    }

    @XmlElementWrapper(name = "Transactions")
    @XmlElement(name = "Transaction")
    public List<TransactionXml> getTransactions() {
        return transactions;
    }

    public Export withCategories(Collection<Category> catList) {
        categories = catList.stream()
                .map(CategoryXml::new)
                .collect(Collectors.toList());
        return this;
    }

    public Export withAccounts(Collection<Account> accList, boolean withDeps) {
        accounts = accList.stream()
                .map(AccountXml::new)
                .collect(Collectors.toList());

        if (withDeps) {
            categories = accounts.stream()
                    .map(AccountXml::getCategoryId)
                    .distinct()
                    .map(source::getCategory)
                    .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                    .map(CategoryXml::new)
                    .collect(Collectors.toList());

            currencies = accounts.stream()
                    .map(AccountXml::getCurrencyId)
                    .distinct()
                    .map(source::getCurrency)
                    .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                    .map(CurrencyXml::new)
                    .collect(Collectors.toList());
        }

        return this;
    }

    public Export withContacts(Collection<Contact> contactList) {
        contacts = contactList.stream()
                .map(ContactXml::new)
                .collect(Collectors.toList());
        return this;
    }

    public Export withCurrencies(Collection<Currency> currencyList) {
        currencies = currencyList.stream()
                .map(CurrencyXml::new)
                .collect(Collectors.toList());
        return this;
    }

    public Export withTransactionGroups(Collection<TransactionGroup> tgList) {
        transactionGroups = tgList.stream()
                .map(TransactionGroupXml::new)
                .collect(Collectors.toList());
        return this;
    }

    public Export withTransactions(Collection<Transaction> tList, boolean withDeps) {
        transactions = tList.stream()
                .map(TransactionXml::new)
                .collect(Collectors.toList());

        if (withDeps) {
            transactionGroups = transactions.stream()
                    .filter(t -> t.getGroupId() != 0)
                    .map(TransactionXml::getGroupId)
                    .distinct()
                    .map(source::getTransactionGroup)
                    .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                    .map(TransactionGroupXml::new)
                    .collect(Collectors.toList());

            contacts = transactions.stream()
                    .filter(t -> t.getContactId() != 0)
                    .map(TransactionXml::getContactId)
                    .distinct()
                    .map(source::getContact)
                    .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                    .map(ContactXml::new)
                    .collect(Collectors.toList());

            Set<Integer> accIdList = new HashSet<>();
            for (Transaction t : tList) {
                accIdList.add(t.getAccountDebitedId());
                accIdList.add(t.getAccountCreditedId());
            }
            withAccounts(accIdList.stream()
                    .map(source::getAccount)
                    .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                    .collect(Collectors.toList()), true);
        }

        return this;
    }

    public void doExport(OutputStream out) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(
                    Export.class,
                    CategoryXml.class,
                    AccountXml.class,
                    ContactXml.class,
                    CurrencyXml.class,
                    TransactionGroupXml.class,
                    TransactionXml.class
            );

            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, out);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
}
