/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
import static org.panteleyev.money.xml.XMLUtils.appendTextNode;

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
                .map(Account::currencyUuid)
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

        appendTextNode(e, "uuid", icon.uuid());
        appendTextNode(e, "name", icon.getName());
        appendTextNode(e, "bytes", icon.getBytes());
        appendTextNode(e, "created", icon.created());
        appendTextNode(e, "modified", icon.modified());

        return e;
    }

    private static Element exportCategory(Document doc, Category category) {
        var e = doc.createElement("Category");

        appendTextNode(e, "name", category.name());
        appendTextNode(e, "comment", category.comment());
        appendTextNode(e, "type", category.type());
        appendTextNode(e, "iconUuid", category.iconUuid());
        appendTextNode(e, "guid", category.uuid());
        appendTextNode(e, "created", category.created());
        appendTextNode(e, "modified", category.modified());

        return e;
    }

    private static Element exportAccount(Document doc, Account account) {
        var e = doc.createElement("Account");

        appendTextNode(e, "name", account.name());
        appendTextNode(e, "comment", account.comment());
        appendTextNode(e, "accountNumber", account.accountNumber());
        appendTextNode(e, "openingBalance", account.openingBalance());
        appendTextNode(e, "accountLimit", account.accountLimit());
        appendTextNode(e, "currencyRate", account.currencyRate());
        appendTextNode(e, "type", account.type());
        appendTextNode(e, "categoryUuid", account.categoryUuid());
        appendTextNode(e, "currencyUuid", account.currencyUuid());
        appendTextNode(e, "enabled", account.enabled());
        appendTextNode(e, "interest", account.interest());
        appendTextNode(e, "closingDate", account.closingDate());
        appendTextNode(e, "iconUuid", account.iconUuid());
        appendTextNode(e, "cardType", account.cardType());
        appendTextNode(e, "cardNumber", account.cardNumber());
        appendTextNode(e, "total", account.total());
        appendTextNode(e, "totalWaiting", account.totalWaiting());
        appendTextNode(e, "guid", account.uuid());
        appendTextNode(e, "created", account.created());
        appendTextNode(e, "modified", account.modified());

        return e;
    }

    private static Element exportContact(Document doc, Contact contact) {
        var e = doc.createElement("Contact");

        appendTextNode(e, "name", contact.name());
        appendTextNode(e, "type", contact.type());
        appendTextNode(e, "phone", contact.phone());
        appendTextNode(e, "mobile", contact.mobile());
        appendTextNode(e, "email", contact.email());
        appendTextNode(e, "web", contact.web());
        appendTextNode(e, "comment", contact.comment());
        appendTextNode(e, "street", contact.street());
        appendTextNode(e, "city", contact.city());
        appendTextNode(e, "country", contact.country());
        appendTextNode(e, "zip", contact.zip());
        appendTextNode(e, "iconUuid", contact.iconUuid());
        appendTextNode(e, "guid", contact.uuid());
        appendTextNode(e, "created", contact.created());
        appendTextNode(e, "modified", contact.modified());

        return e;
    }

    private static Element exportCurrency(Document doc, Currency currency) {
        var e = doc.createElement("Currency");

        appendTextNode(e, "symbol", currency.symbol());
        appendTextNode(e, "description", currency.description());
        appendTextNode(e, "formatSymbol", currency.formatSymbol());
        appendTextNode(e, "formatSymbolPosition", currency.formatSymbolPosition());
        appendTextNode(e, "showFormatSymbol", currency.showFormatSymbol());
        appendTextNode(e, "default", currency.def());
        appendTextNode(e, "rate", currency.rate().toString());
        appendTextNode(e, "direction", currency.direction());
        appendTextNode(e, "useThousandSeparator", currency.useThousandSeparator());
        appendTextNode(e, "guid", currency.uuid());
        appendTextNode(e, "created", currency.created());
        appendTextNode(e, "modified", currency.modified());

        return e;
    }

    private static Element exportTransaction(Document doc, Transaction t) {
        var e = doc.createElement("Transaction");

        appendTextNode(e, "amount", t.amount());
        appendTextNode(e, "day", t.day());
        appendTextNode(e, "month", t.month());
        appendTextNode(e, "year", t.year());
        appendTextNode(e, "type", t.type());
        appendTextNode(e, "comment", t.comment());
        appendTextNode(e, "checked", t.checked());
        appendTextNode(e, "accountDebitedUuid", t.accountDebitedUuid());
        appendTextNode(e, "accountCreditedUuid", t.accountCreditedUuid());
        appendTextNode(e, "accountDebitedType", t.accountDebitedType());
        appendTextNode(e, "accountCreditedType", t.accountCreditedType());
        appendTextNode(e, "accountDebitedCategoryUuid", t.accountDebitedCategoryUuid());
        appendTextNode(e, "accountCreditedCategoryUuid", t.accountCreditedCategoryUuid());
        appendTextNode(e, "contactUuid", t.contactUuid());
        appendTextNode(e, "rate", t.rate());
        appendTextNode(e, "rateDirection", t.rateDirection());
        appendTextNode(e, "invoiceNumber", t.invoiceNumber());
        appendTextNode(e, "guid", t.uuid());
        appendTextNode(e, "created", t.created());
        appendTextNode(e, "modified", t.modified());
        appendTextNode(e, "parentUuid", t.parentUuid());
        appendTextNode(e, "detailed", t.detailed());
        appendTextNode(e, "statementDate", t.statementDate());

        return e;
    }
}
