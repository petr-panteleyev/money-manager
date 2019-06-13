/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.persistence.RecordSource;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Icon;
import org.panteleyev.money.persistence.model.Transaction;
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
import static org.panteleyev.money.XMLUtils.appendElement;
import static org.panteleyev.money.XMLUtils.appendTextNode;
import static org.panteleyev.money.XMLUtils.createDocument;
import static org.panteleyev.money.XMLUtils.writeDocument;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class Export {
    private final RecordSource source;

    private Collection<Icon> icons = new LinkedHashSet<>();
    private Collection<Category> categories = new ArrayList<>();
    private Collection<Account> accounts = new ArrayList<>();
    private Collection<Contact> contacts = new ArrayList<>();
    private Collection<Currency> currencies = new ArrayList<>();
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
                .map(source::getIcon)
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
                .map(source::getCategory)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            icons.addAll(accounts.stream()
                .map(Account::getIconUuid)
                .distinct()
                .map(source::getIcon)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));

            currencies = accounts.stream()
                .flatMap(a -> a.getCurrencyUuid().stream())
                .distinct()
                .map(source::getCurrency)
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
                .map(source::getIcon)
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
                .map(source::getTransactionDetails)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            transactions.addAll(details);

            withContacts(toExport.stream()
                .flatMap(t -> t.getContactUuid().stream())
                .distinct()
                .map(source::getContact)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            var accIdList = new HashSet<UUID>();
            for (var t : transactions) {
                accIdList.add(t.getAccountDebitedUuid());
                accIdList.add(t.getAccountCreditedUuid());
            }
            withAccounts(accIdList.stream()
                .map(source::getAccount)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);
        } else {
            transactions = toExport;
        }

        return this;
    }

    public void doExport(OutputStream out) {
        try {
            var rootElement = createDocument("Money");
            var doc = rootElement.getOwnerDocument();

            var iconRoot = appendElement(rootElement, "Icons");
            for (var icon : icons) {
                iconRoot.appendChild(exportIcon(doc, icon));
            }

            var accountRoot = appendElement(rootElement, "Accounts");
            for (var account : accounts) {
                accountRoot.appendChild(exportAccount(doc, account));
            }

            var categoryRoot = appendElement(rootElement, "Categories");
            for (var category : categories) {
                categoryRoot.appendChild(exportCategory(doc, category));
            }

            var contactRoot = appendElement(rootElement, "Contacts");
            for (var contact : contacts) {
                contactRoot.appendChild(exportContact(doc, contact));
            }

            var currencyRoot = appendElement(rootElement, "Currencies");
            for (var currency : currencies) {
                currencyRoot.appendChild(exportCurrency(doc, currency));
            }

            var transactionRoot = appendElement(rootElement, "Transactions");
            for (var transaction : transactions) {
                transactionRoot.appendChild(exportTransaction(doc, transaction));
            }

            writeDocument(doc, out);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Element exportIcon(Document doc, Icon icon) {
        var e = doc.createElement("Icon");

        appendTextNode(e, "uuid", icon.getUuid());
        appendTextNode(e, "name", icon.getName());
        appendTextNode(e, "bytes", icon.getBytes());
        appendTextNode(e, "created", icon.getCreated());
        appendTextNode(e, "modified", icon.getModified());

        return e;
    }

    private static Element exportCategory(Document doc, Category category) {
        var e = doc.createElement("Category");

        appendTextNode(e, "name", category.getName());
        appendTextNode(e, "comment", category.getComment());
        appendTextNode(e, "catTypeId", category.getCatTypeId());
        appendTextNode(e, "iconUuid", category.getIconUuid());
        appendTextNode(e, "guid", category.getUuid());
        appendTextNode(e, "created", category.getCreated());
        appendTextNode(e, "modified", category.getModified());

        return e;
    }

    private static Element exportAccount(Document doc, Account account) {
        var e = doc.createElement("Account");

        appendTextNode(e, "name", account.getName());
        appendTextNode(e, "comment", account.getComment());
        appendTextNode(e, "accountNumber", account.getAccountNumber());
        appendTextNode(e, "openingBalance", account.getOpeningBalance());
        appendTextNode(e, "accountLimit", account.getAccountLimit());
        appendTextNode(e, "currencyRate", account.getCurrencyRate());
        appendTextNode(e, "typeId", account.getTypeId());
        appendTextNode(e, "categoryUuid", account.getCategoryUuid());
        account.getCurrencyUuid().ifPresent(uuid -> appendTextNode(e, "currencyUuid", uuid));
        appendTextNode(e, "enabled", account.getEnabled());
        appendTextNode(e, "interest", account.getInterest());
        account.getClosingDate().ifPresent(closingDate -> appendTextNode(e, "closingDate", closingDate));
        appendTextNode(e, "iconUuid", account.getIconUuid());
        appendTextNode(e, "guid", account.getUuid());
        appendTextNode(e, "created", account.getCreated());
        appendTextNode(e, "modified", account.getModified());

        return e;
    }

    private static Element exportContact(Document doc, Contact contact) {
        var e = doc.createElement("Contact");

        appendTextNode(e, "name", contact.getName());
        appendTextNode(e, "typeId", contact.getTypeId());
        appendTextNode(e, "phone", contact.getPhone());
        appendTextNode(e, "mobile", contact.getMobile());
        appendTextNode(e, "email", contact.getEmail());
        appendTextNode(e, "web", contact.getWeb());
        appendTextNode(e, "comment", contact.getComment());
        appendTextNode(e, "street", contact.getStreet());
        appendTextNode(e, "city", contact.getCity());
        appendTextNode(e, "country", contact.getCountry());
        appendTextNode(e, "zip", contact.getZip());
        appendTextNode(e, "iconUuid", contact.getIconUuid());
        appendTextNode(e, "guid", contact.getUuid());
        appendTextNode(e, "created", contact.getCreated());
        appendTextNode(e, "modified", contact.getModified());

        return e;
    }

    private static Element exportCurrency(Document doc, Currency currency) {
        var e = doc.createElement("Currency");

        appendTextNode(e, "symbol", currency.getSymbol());
        appendTextNode(e, "description", currency.getDescription());
        appendTextNode(e, "formatSymbol", currency.getFormatSymbol());
        appendTextNode(e, "formatSymbolPosition", currency.getFormatSymbolPosition());
        appendTextNode(e, "showFormatSymbol", currency.getShowFormatSymbol());
        appendTextNode(e, "default", currency.getDef());
        appendTextNode(e, "rate", currency.getRate().toString());
        appendTextNode(e, "direction", currency.getDirection());
        appendTextNode(e, "useThousandSeparator", currency.getUseThousandSeparator());
        appendTextNode(e, "guid", currency.getUuid());
        appendTextNode(e, "created", currency.getCreated());
        appendTextNode(e, "modified", currency.getModified());

        return e;
    }

    private static Element exportTransaction(Document doc, Transaction t) {
        var e = doc.createElement("Transaction");

        appendTextNode(e, "amount", t.getAmount());
        appendTextNode(e, "day", t.getDay());
        appendTextNode(e, "month", t.getMonth());
        appendTextNode(e, "year", t.getYear());
        appendTextNode(e, "transactionTypeId", t.getTransactionTypeId());
        appendTextNode(e, "comment", t.getComment());
        appendTextNode(e, "checked", t.getChecked());
        appendTextNode(e, "accountDebitedUuid", t.getAccountDebitedUuid());
        appendTextNode(e, "accountCreditedUuid", t.getAccountCreditedUuid());
        appendTextNode(e, "accountDebitedTypeId", t.getAccountDebitedTypeId());
        appendTextNode(e, "accountCreditedTypeId", t.getAccountCreditedTypeId());
        appendTextNode(e, "accountDebitedCategoryUuid", t.getAccountDebitedCategoryUuid());
        appendTextNode(e, "accountCreditedCategoryUuid", t.getAccountCreditedCategoryUuid());
        t.getContactUuid().ifPresent(uuid -> appendTextNode(e, "contactUuid", uuid));
        appendTextNode(e, "rate", t.getRate());
        appendTextNode(e, "rateDirection", t.getRateDirection());
        appendTextNode(e, "invoiceNumber", t.getInvoiceNumber());
        appendTextNode(e, "guid", t.getUuid());
        appendTextNode(e, "created", t.getCreated());
        appendTextNode(e, "modified", t.getModified());
        t.getParentUuid().ifPresent(uuid -> appendTextNode(e, "parentUuid", uuid));
        appendTextNode(e, "detailed", t.isDetailed());

        return e;
    }
}
