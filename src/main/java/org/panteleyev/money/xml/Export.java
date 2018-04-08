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
import org.panteleyev.money.persistence.RecordSource;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.panteleyev.money.XMLUtils.closeTag;
import static org.panteleyev.money.XMLUtils.openTag;
import static org.panteleyev.money.XMLUtils.writeTag;
import static org.panteleyev.money.XMLUtils.writeXmlHeader;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class Export {
    private final RecordSource source;

    private Collection<Category> categories = new ArrayList<>();
    private Collection<Account> accounts = new ArrayList<>();
    private Collection<Contact> contacts = new ArrayList<>();
    private Collection<Currency> currencies = new ArrayList<>();
    private Collection<TransactionGroup> transactionGroups = new ArrayList<>();
    private Collection<Transaction> transactions = new ArrayList<>();

    public Export() {
        this(getDao());
    }

    public Export(RecordSource source) {
        this.source = source;
    }

    public RecordSource getSource() {
        return source;
    }

    public Export withCategories(Collection<Category> categories) {
        this.categories = categories;
        return this;
    }

    public Export withAccounts(Collection<Account> accounts, boolean withDeps) {
        this.accounts = accounts;

        if (withDeps) {
            categories = accounts.stream()
                    .map(Account::getCategoryId)
                    .distinct()
                    .map(source::getCategory)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            currencies = accounts.stream()
                    .map(Account::getCurrencyId)
                    .distinct()
                    .map(source::getCurrency)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        }

        return this;
    }

    public Export withContacts(Collection<Contact> contacts) {
        this.contacts = contacts;
        return this;
    }

    public Export withCurrencies(Collection<Currency> currencies) {
        this.currencies = currencies;
        return this;
    }

    public Export withTransactionGroups(Collection<TransactionGroup> transactionGroups) {
        this.transactionGroups = transactionGroups;
        return this;
    }

    public Export withTransactions(Collection<Transaction> transactions, boolean withDeps) {
        this.transactions = transactions;

        if (withDeps) {
            transactionGroups = transactions.stream()
                    .filter(t -> t.getGroupId() != 0)
                    .map(Transaction::getGroupId)
                    .distinct()
                    .map(source::getTransactionGroup)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            contacts = transactions.stream()
                    .filter(t -> t.getContactId() != 0)
                    .map(Transaction::getContactId)
                    .distinct()
                    .map(source::getContact)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            var accIdList = new HashSet<Integer>();
            for (var t : transactions) {
                accIdList.add(t.getAccountDebitedId());
                accIdList.add(t.getAccountCreditedId());
            }
            withAccounts(accIdList.stream()
                    .map(source::getAccount)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList()), true);
        }

        return this;
    }

    public void doExport(OutputStream out) {
        try (var w = new PrintWriter(out)) {
            writeXmlHeader(w);

            openTag(w, "Money");

            openTag(w, "Accounts");
            for (var account : accounts) {
                exportAccount(w, account);
            }
            closeTag(w, "Accounts");
            w.flush();

            openTag(w, "Categories");
            for (var category : categories) {
                exportCategory(w, category);
            }
            closeTag(w, "Categories");
            w.flush();

            openTag(w, "Contacts");
            for (var contact : contacts) {
                exportContact(w, contact);
            }
            closeTag(w, "Contacts");
            w.flush();

            openTag(w, "Currencies");
            for (var currency : currencies) {
                exportCurrency(w, currency);
            }
            closeTag(w, "Currencies");
            w.flush();

            openTag(w, "TransactionGroups");
            for (var transactionGroup : transactionGroups) {
                exportTransactionGroup(w, transactionGroup);
            }
            closeTag(w, "TransactionGroups");
            w.flush();

            openTag(w, "Transactions");
            for (var transaction : transactions) {
                exportTransaction(w, transaction);
            }
            closeTag(w, "Transactions");
            w.flush();

            closeTag(w, "Money");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void exportCategory(Writer w, Category category) throws IOException {
        openTag(w, "Category", category.getId());

        writeTag(w, "name", category.getName());
        writeTag(w, "comment", category.getComment());
        writeTag(w, "catTypeId", category.getCatTypeId());
        writeTag(w, "expanded", category.getExpanded());
        writeTag(w, "guid", category.getGuid());
        writeTag(w, "modified", category.getModified());

        closeTag(w, "Category");
    }

    private void exportAccount(Writer w, Account account) throws IOException {
        openTag(w, "Account", account.getId());

        writeTag(w, "name", account.getName());
        writeTag(w, "comment", account.getComment());
        writeTag(w, "openingBalance", account.getOpeningBalance());
        writeTag(w, "accountLimit", account.getAccountLimit());
        writeTag(w, "currencyRate", account.getCurrencyRate());
        writeTag(w, "typeId", account.getTypeId());
        writeTag(w, "categoryId", account.getCategoryId());
        writeTag(w, "currencyId", account.getCurrencyId());
        writeTag(w, "enabled", account.getEnabled());
        writeTag(w, "guid", account.getGuid());
        writeTag(w, "modified", account.getModified());

        closeTag(w, "Account");
    }

    private void exportContact(Writer w, Contact contact) throws IOException {
        openTag(w, "Contact", contact.getId());

        writeTag(w, "name", contact.getName());
        writeTag(w, "typeId", contact.getTypeId());
        writeTag(w, "phone", contact.getPhone());
        writeTag(w, "mobile", contact.getMobile());
        writeTag(w, "email", contact.getEmail());
        writeTag(w, "web", contact.getWeb());
        writeTag(w, "comment", contact.getComment());
        writeTag(w, "street", contact.getStreet());
        writeTag(w, "city", contact.getCity());
        writeTag(w, "country", contact.getCountry());
        writeTag(w, "zip", contact.getZip());
        writeTag(w, "guid", contact.getGuid());
        writeTag(w, "modified", contact.getModified());

        closeTag(w, "Contact");
    }

    private void exportCurrency(Writer w, Currency currency) throws IOException {
        openTag(w, "Currency", currency.getId());

        writeTag(w, "symbol", currency.getSymbol());
        writeTag(w, "description", currency.getDescription());
        writeTag(w, "formatSymbol", currency.getFormatSymbol());
        writeTag(w, "formatSymbolPosition", currency.getFormatSymbolPosition());
        writeTag(w, "showFormatSymbol", currency.getShowFormatSymbol());
        writeTag(w, "default", currency.getDef());
        writeTag(w, "rate", currency.getRate());
        writeTag(w, "direction", currency.getDirection());
        writeTag(w, "useThousandSeparator", currency.getUseThousandSeparator());
        writeTag(w, "guid", currency.getGuid());
        writeTag(w, "modified", currency.getModified());

        closeTag(w, "Currency");
    }

    private void exportTransactionGroup(Writer w, TransactionGroup tg) throws IOException {
        openTag(w, "TransactionGroup", tg.getId());

        writeTag(w, "day", tg.getDay());
        writeTag(w, "month", tg.getMonth());
        writeTag(w, "year", tg.getYear());
        writeTag(w, "expanded", tg.getExpanded());
        writeTag(w, "guid", tg.getGuid());
        writeTag(w, "modified", tg.getModified());

        closeTag(w, "TransactionGroup");
    }

    private void exportTransaction(Writer w, Transaction t) throws IOException {
        openTag(w, "Transaction", t.getId());

        writeTag(w, "amount", t.getAmount());
        writeTag(w, "day", t.getDay());
        writeTag(w, "month", t.getMonth());
        writeTag(w, "year", t.getYear());
        writeTag(w, "transactionTypeId", t.getTransactionTypeId());
        writeTag(w, "comment", t.getComment());
        writeTag(w, "checked", t.getChecked());
        writeTag(w, "accountDebitedId", t.getAccountDebitedId());
        writeTag(w, "accountCreditedId", t.getAccountCreditedId());
        writeTag(w, "accountDebitedTypeId", t.getAccountDebitedTypeId());
        writeTag(w, "accountCreditedTypeId", t.getAccountCreditedTypeId());
        writeTag(w, "accountDebitedCategoryId", t.getAccountDebitedCategoryId());
        writeTag(w, "accountCreditedCategoryId", t.getAccountCreditedCategoryId());
        writeTag(w, "groupId", t.getGroupId());
        writeTag(w, "contactId", t.getContactId());
        writeTag(w, "rate", t.getRate());
        writeTag(w, "rateDirection", t.getRateDirection());
        writeTag(w, "invoiceNumber", t.getInvoiceNumber());
        writeTag(w, "guid", t.getGuid());
        writeTag(w, "modified", t.getModified());

        closeTag(w, "Transaction");
    }



}
