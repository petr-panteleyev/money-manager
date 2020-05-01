package org.panteleyev.money.xml;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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

    private final Collection<Icon> icons = new LinkedHashSet<>();
    private final Collection<Category> categories = new ArrayList<>();
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
                .map(Category::iconUuid)
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
                .map(Account::categoryUuid)
                .distinct()
                .map(cache::getCategory)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            icons.addAll(accounts.stream()
                .map(Account::iconUuid)
                .distinct()
                .map(cache::getIcon)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));

            currencies = accounts.stream()
                .map(a -> a.currencyUuid())
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
                .map(Contact::iconUuid)
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
                .filter(t -> t.parentUuid() == null)
                .collect(Collectors.toCollection(ArrayList::new));

            var details = toExport.stream()
                .filter(Transaction::detailed)
                .map(cache::getTransactionDetails)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            transactions.addAll(details);

            withContacts(toExport.stream()
                .map(Transaction::contactUuid)
                .distinct()
                .map(cache::getContact)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()), true);

            var accIdList = new HashSet<UUID>();
            for (var t : transactions) {
                accIdList.add(t.accountDebitedUuid());
                accIdList.add(t.accountCreditedUuid());
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

        XMLUtils.appendTextNode(e, "uuid", icon.uuid());
        XMLUtils.appendTextNode(e, "name", icon.getName());
        XMLUtils.appendTextNode(e, "bytes", icon.getBytes());
        XMLUtils.appendTextNode(e, "created", icon.created());
        XMLUtils.appendTextNode(e, "modified", icon.modified());

        return e;
    }

    private static Element exportCategory(Document doc, Category category) {
        var e = doc.createElement("Category");

        XMLUtils.appendTextNode(e, "name", category.name());
        XMLUtils.appendTextNode(e, "comment", category.comment());
        XMLUtils.appendTextNode(e, "type", category.type());
        XMLUtils.appendTextNode(e, "iconUuid", category.iconUuid());
        XMLUtils.appendTextNode(e, "guid", category.uuid());
        XMLUtils.appendTextNode(e, "created", category.created());
        XMLUtils.appendTextNode(e, "modified", category.modified());

        return e;
    }

    private static Element exportAccount(Document doc, Account account) {
        var e = doc.createElement("Account");

        XMLUtils.appendTextNode(e, "name", account.name());
        XMLUtils.appendTextNode(e, "comment", account.comment());
        XMLUtils.appendTextNode(e, "accountNumber", account.accountNumber());
        XMLUtils.appendTextNode(e, "openingBalance", account.openingBalance());
        XMLUtils.appendTextNode(e, "accountLimit", account.accountLimit());
        XMLUtils.appendTextNode(e, "currencyRate", account.currencyRate());
        XMLUtils.appendTextNode(e, "type", account.type());
        XMLUtils.appendTextNode(e, "categoryUuid", account.categoryUuid());
        Optional.ofNullable(account.currencyUuid()).ifPresent(uuid -> XMLUtils.appendTextNode(e, "currencyUuid", uuid));
        XMLUtils.appendTextNode(e, "enabled", account.enabled());
        XMLUtils.appendTextNode(e, "interest", account.interest());
        Optional.ofNullable(account.closingDate()).ifPresent(closingDate -> XMLUtils.appendTextNode(e, "closingDate", closingDate));
        XMLUtils.appendTextNode(e, "iconUuid", account.iconUuid());
        XMLUtils.appendTextNode(e, "cardType", account.cardType());
        XMLUtils.appendTextNode(e, "cardNumber", account.cardNumber());
        XMLUtils.appendTextNode(e, "guid", account.uuid());
        XMLUtils.appendTextNode(e, "created", account.created());
        XMLUtils.appendTextNode(e, "modified", account.modified());

        return e;
    }

    private static Element exportContact(Document doc, Contact contact) {
        var e = doc.createElement("Contact");

        XMLUtils.appendTextNode(e, "name", contact.name());
        XMLUtils.appendTextNode(e, "type", contact.type());
        XMLUtils.appendTextNode(e, "phone", contact.phone());
        XMLUtils.appendTextNode(e, "mobile", contact.mobile());
        XMLUtils.appendTextNode(e, "email", contact.email());
        XMLUtils.appendTextNode(e, "web", contact.web());
        XMLUtils.appendTextNode(e, "comment", contact.comment());
        XMLUtils.appendTextNode(e, "street", contact.street());
        XMLUtils.appendTextNode(e, "city", contact.city());
        XMLUtils.appendTextNode(e, "country", contact.country());
        XMLUtils.appendTextNode(e, "zip", contact.zip());
        XMLUtils.appendTextNode(e, "iconUuid", contact.iconUuid());
        XMLUtils.appendTextNode(e, "guid", contact.uuid());
        XMLUtils.appendTextNode(e, "created", contact.created());
        XMLUtils.appendTextNode(e, "modified", contact.modified());

        return e;
    }

    private static Element exportCurrency(Document doc, Currency currency) {
        var e = doc.createElement("Currency");

        XMLUtils.appendTextNode(e, "symbol", currency.symbol());
        XMLUtils.appendTextNode(e, "description", currency.description());
        XMLUtils.appendTextNode(e, "formatSymbol", currency.formatSymbol());
        XMLUtils.appendTextNode(e, "formatSymbolPosition", currency.formatSymbolPosition());
        XMLUtils.appendTextNode(e, "showFormatSymbol", currency.showFormatSymbol());
        XMLUtils.appendTextNode(e, "default", currency.def());
        XMLUtils.appendTextNode(e, "rate", currency.rate().toString());
        XMLUtils.appendTextNode(e, "direction", currency.direction());
        XMLUtils.appendTextNode(e, "useThousandSeparator", currency.useThousandSeparator());
        XMLUtils.appendTextNode(e, "guid", currency.uuid());
        XMLUtils.appendTextNode(e, "created", currency.created());
        XMLUtils.appendTextNode(e, "modified", currency.modified());

        return e;
    }

    private static Element exportTransaction(Document doc, Transaction t) {
        var e = doc.createElement("Transaction");

        XMLUtils.appendTextNode(e, "amount", t.amount());
        XMLUtils.appendTextNode(e, "day", t.day());
        XMLUtils.appendTextNode(e, "month", t.month());
        XMLUtils.appendTextNode(e, "year", t.year());
        XMLUtils.appendTextNode(e, "type", t.type());
        XMLUtils.appendTextNode(e, "comment", t.comment());
        XMLUtils.appendTextNode(e, "checked", t.checked());
        XMLUtils.appendTextNode(e, "accountDebitedUuid", t.accountDebitedUuid());
        XMLUtils.appendTextNode(e, "accountCreditedUuid", t.accountCreditedUuid());
        XMLUtils.appendTextNode(e, "accountDebitedType", t.accountDebitedType());
        XMLUtils.appendTextNode(e, "accountCreditedType", t.accountCreditedType());
        XMLUtils.appendTextNode(e, "accountDebitedCategoryUuid", t.accountDebitedCategoryUuid());
        XMLUtils.appendTextNode(e, "accountCreditedCategoryUuid", t.accountCreditedCategoryUuid());
        Optional.ofNullable(t.contactUuid()).ifPresent(uuid -> XMLUtils.appendTextNode(e, "contactUuid", uuid));
        XMLUtils.appendTextNode(e, "rate", t.rate());
        XMLUtils.appendTextNode(e, "rateDirection", t.rateDirection());
        XMLUtils.appendTextNode(e, "invoiceNumber", t.invoiceNumber());
        XMLUtils.appendTextNode(e, "guid", t.uuid());
        XMLUtils.appendTextNode(e, "created", t.created());
        XMLUtils.appendTextNode(e, "modified", t.modified());
        Optional.ofNullable(t.parentUuid()).ifPresent(uuid -> XMLUtils.appendTextNode(e, "parentUuid", uuid));
        XMLUtils.appendTextNode(e, "detailed", t.detailed());

        return e;
    }
}
