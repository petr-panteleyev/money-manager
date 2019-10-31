/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.DataCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.money.persistence.DataCache.cache;

public class Export {
    private final DataCache cache;

    private Collection<Icon> icons = new LinkedHashSet<>();
    private Collection<Category> categories = new ArrayList<>();
    private Collection<Account> accounts = new ArrayList<>();
    private Collection<Contact> contacts = new ArrayList<>();
    private Collection<Currency> currencies = new ArrayList<>();
    private Collection<Transaction> transactions = new ArrayList<>();

    public Export() {
        this(cache());
    }

    public Export(DataCache cache) {
        this.cache = cache;
    }

    public Export withIcons(Collection<Icon> icons) {
        this.icons.addAll(icons);
        return this;
    }

    public Export withCategories(Collection<Category> categories, boolean withDeps) {
        this.categories.addAll(categories);

        if (withDeps) {
            icons.addAll(categories.stream()
                .map(Category::getIconUuid)
                .distinct()
                .map(cache::getIcon)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
        }

        return this;
    }

    public Export withAccounts(Collection<Account> accounts, boolean withDeps) {
        this.accounts = accounts;

        if (withDeps) {
            withCategories(accounts.stream()
                .map(Account::getCategoryUuid)
                .distinct()
                .map(cache::getCategory)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            icons.addAll(accounts.stream()
                .map(Account::getIconUuid)
                .distinct()
                .map(cache::getIcon)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));

            currencies = accounts.stream()
                .flatMap(a -> a.getCurrencyUuid().stream())
                .distinct()
                .map(cache::getCurrency)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        }

        return this;
    }

    public Export withContacts(Collection<Contact> contacts, boolean withDeps) {
        this.contacts = contacts;

        if (withDeps) {
            icons.addAll(contacts.stream()
                .map(Contact::getIconUuid)
                .distinct()
                .map(cache::getIcon)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
        }

        return this;
    }

    public Export withCurrencies(Collection<Currency> currencies) {
        this.currencies = currencies;
        return this;
    }

    public Export withTransactions(Collection<Transaction> toExport, boolean withDeps) {
        if (withDeps) {
            transactions = toExport.stream()
                .filter(t -> t.getParentUuid().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));

            var details = toExport.stream()
                .filter(Transaction::isDetailed)
                .map(cache::getTransactionDetails)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            transactions.addAll(details);

            withContacts(toExport.stream()
                .flatMap(t -> t.getContactUuid().stream())
                .distinct()
                .map(cache::getContact)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            var accIdList = new HashSet<UUID>();
            for (var t : transactions) {
                accIdList.add(t.getAccountDebitedUuid());
                accIdList.add(t.getAccountCreditedUuid());
            }
            withAccounts(accIdList.stream()
                .map(cache::getAccount)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);
        } else {
            transactions = toExport;
        }

        return this;
    }

    public void doExport(OutputStream out) {
        try {
            var rootElement = XMLUtils.createDocument("Money");
            var doc = rootElement.getOwnerDocument();

            var iconRoot = XMLUtils.appendElement(rootElement, "Icons");
            for (var icon : icons) {
                iconRoot.appendChild(exportIcon(doc, icon));
            }

            var accountRoot = XMLUtils.appendElement(rootElement, "Accounts");
            for (var account : accounts) {
                accountRoot.appendChild(exportAccount(doc, account));
            }

            var categoryRoot = XMLUtils.appendElement(rootElement, "Categories");
            for (var category : categories) {
                categoryRoot.appendChild(exportCategory(doc, category));
            }

            var contactRoot = XMLUtils.appendElement(rootElement, "Contacts");
            for (var contact : contacts) {
                contactRoot.appendChild(exportContact(doc, contact));
            }

            var currencyRoot = XMLUtils.appendElement(rootElement, "Currencies");
            for (var currency : currencies) {
                currencyRoot.appendChild(exportCurrency(doc, currency));
            }

            var transactionRoot = XMLUtils.appendElement(rootElement, "Transactions");
            for (var transaction : transactions) {
                transactionRoot.appendChild(exportTransaction(doc, transaction));
            }

            XMLUtils.writeDocument(doc, out);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Element exportIcon(Document doc, Icon icon) {
        var e = doc.createElement("Icon");

        XMLUtils.appendTextNode(e, "uuid", icon.getUuid());
        XMLUtils.appendTextNode(e, "name", icon.getName());
        XMLUtils.appendTextNode(e, "bytes", icon.getBytes());
        XMLUtils.appendTextNode(e, "created", icon.getCreated());
        XMLUtils.appendTextNode(e, "modified", icon.getModified());

        return e;
    }

    private static Element exportCategory(Document doc, Category category) {
        var e = doc.createElement("Category");

        XMLUtils.appendTextNode(e, "name", category.getName());
        XMLUtils.appendTextNode(e, "comment", category.getComment());
        XMLUtils.appendTextNode(e, "catTypeId", category.getCatTypeId());
        XMLUtils.appendTextNode(e, "iconUuid", category.getIconUuid());
        XMLUtils.appendTextNode(e, "guid", category.getUuid());
        XMLUtils.appendTextNode(e, "created", category.getCreated());
        XMLUtils.appendTextNode(e, "modified", category.getModified());

        return e;
    }

    private static Element exportAccount(Document doc, Account account) {
        var e = doc.createElement("Account");

        XMLUtils.appendTextNode(e, "name", account.getName());
        XMLUtils.appendTextNode(e, "comment", account.getComment());
        XMLUtils.appendTextNode(e, "accountNumber", account.getAccountNumber());
        XMLUtils.appendTextNode(e, "openingBalance", account.getOpeningBalance());
        XMLUtils.appendTextNode(e, "accountLimit", account.getAccountLimit());
        XMLUtils.appendTextNode(e, "currencyRate", account.getCurrencyRate());
        XMLUtils.appendTextNode(e, "typeId", account.getTypeId());
        XMLUtils.appendTextNode(e, "categoryUuid", account.getCategoryUuid());
        account.getCurrencyUuid().ifPresent(uuid -> XMLUtils.appendTextNode(e, "currencyUuid", uuid));
        XMLUtils.appendTextNode(e, "enabled", account.getEnabled());
        XMLUtils.appendTextNode(e, "interest", account.getInterest());
        account.getClosingDate().ifPresent(closingDate -> XMLUtils.appendTextNode(e, "closingDate", closingDate));
        XMLUtils.appendTextNode(e, "iconUuid", account.getIconUuid());
        XMLUtils.appendTextNode(e, "guid", account.getUuid());
        XMLUtils.appendTextNode(e, "created", account.getCreated());
        XMLUtils.appendTextNode(e, "modified", account.getModified());

        return e;
    }

    private static Element exportContact(Document doc, Contact contact) {
        var e = doc.createElement("Contact");

        XMLUtils.appendTextNode(e, "name", contact.getName());
        XMLUtils.appendTextNode(e, "typeId", contact.getTypeId());
        XMLUtils.appendTextNode(e, "phone", contact.getPhone());
        XMLUtils.appendTextNode(e, "mobile", contact.getMobile());
        XMLUtils.appendTextNode(e, "email", contact.getEmail());
        XMLUtils.appendTextNode(e, "web", contact.getWeb());
        XMLUtils.appendTextNode(e, "comment", contact.getComment());
        XMLUtils.appendTextNode(e, "street", contact.getStreet());
        XMLUtils.appendTextNode(e, "city", contact.getCity());
        XMLUtils.appendTextNode(e, "country", contact.getCountry());
        XMLUtils.appendTextNode(e, "zip", contact.getZip());
        XMLUtils.appendTextNode(e, "iconUuid", contact.getIconUuid());
        XMLUtils.appendTextNode(e, "guid", contact.getUuid());
        XMLUtils.appendTextNode(e, "created", contact.getCreated());
        XMLUtils.appendTextNode(e, "modified", contact.getModified());

        return e;
    }

    private static Element exportCurrency(Document doc, Currency currency) {
        var e = doc.createElement("Currency");

        XMLUtils.appendTextNode(e, "symbol", currency.getSymbol());
        XMLUtils.appendTextNode(e, "description", currency.getDescription());
        XMLUtils.appendTextNode(e, "formatSymbol", currency.getFormatSymbol());
        XMLUtils.appendTextNode(e, "formatSymbolPosition", currency.getFormatSymbolPosition());
        XMLUtils.appendTextNode(e, "showFormatSymbol", currency.getShowFormatSymbol());
        XMLUtils.appendTextNode(e, "default", currency.getDef());
        XMLUtils.appendTextNode(e, "rate", currency.getRate().toString());
        XMLUtils.appendTextNode(e, "direction", currency.getDirection());
        XMLUtils.appendTextNode(e, "useThousandSeparator", currency.getUseThousandSeparator());
        XMLUtils.appendTextNode(e, "guid", currency.getUuid());
        XMLUtils.appendTextNode(e, "created", currency.getCreated());
        XMLUtils.appendTextNode(e, "modified", currency.getModified());

        return e;
    }

    private static Element exportTransaction(Document doc, Transaction t) {
        var e = doc.createElement("Transaction");

        XMLUtils.appendTextNode(e, "amount", t.getAmount());
        XMLUtils.appendTextNode(e, "day", t.getDay());
        XMLUtils.appendTextNode(e, "month", t.getMonth());
        XMLUtils.appendTextNode(e, "year", t.getYear());
        XMLUtils.appendTextNode(e, "transactionTypeId", t.getTransactionTypeId());
        XMLUtils.appendTextNode(e, "comment", t.getComment());
        XMLUtils.appendTextNode(e, "checked", t.getChecked());
        XMLUtils.appendTextNode(e, "accountDebitedUuid", t.getAccountDebitedUuid());
        XMLUtils.appendTextNode(e, "accountCreditedUuid", t.getAccountCreditedUuid());
        XMLUtils.appendTextNode(e, "accountDebitedTypeId", t.getAccountDebitedTypeId());
        XMLUtils.appendTextNode(e, "accountCreditedTypeId", t.getAccountCreditedTypeId());
        XMLUtils.appendTextNode(e, "accountDebitedCategoryUuid", t.getAccountDebitedCategoryUuid());
        XMLUtils.appendTextNode(e, "accountCreditedCategoryUuid", t.getAccountCreditedCategoryUuid());
        t.getContactUuid().ifPresent(uuid -> XMLUtils.appendTextNode(e, "contactUuid", uuid));
        XMLUtils.appendTextNode(e, "rate", t.getRate());
        XMLUtils.appendTextNode(e, "rateDirection", t.getRateDirection());
        XMLUtils.appendTextNode(e, "invoiceNumber", t.getInvoiceNumber());
        XMLUtils.appendTextNode(e, "guid", t.getUuid());
        XMLUtils.appendTextNode(e, "created", t.getCreated());
        XMLUtils.appendTextNode(e, "modified", t.getModified());
        t.getParentUuid().ifPresent(uuid -> XMLUtils.appendTextNode(e, "parentUuid", uuid));
        XMLUtils.appendTextNode(e, "detailed", t.isDetailed());

        return e;
    }
}
