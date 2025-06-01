/*
 Copyright © 2017-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

import org.panteleyev.commons.xml.XMLStreamWriterWrapper;
import org.panteleyev.money.desktop.commons.DataCache;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.util.function.Consumer;

import static org.panteleyev.money.desktop.commons.xml.RecordSerializer.serialize;

public class Export {
    private static final QName ELEMENT_MONEY = new QName("Money");
    private static final QName ELEMENT_ICONS = new QName("Icons");
    private static final QName ELEMENT_CATEGORIES = new QName("Categories");
    private static final QName ELEMENT_CURRENCIES = new QName("Currencies");
    private static final QName ELEMENT_EXCHANGE_SECURITIES = new QName("ExchangeSecurities");
    private static final QName ELEMENT_ACCOUNTS = new QName("Accounts");
    private static final QName ELEMENT_CARDS = new QName("Cards");
    private static final QName ELEMENT_CONTACTS = new QName("Contacts");
    private static final QName ELEMENT_TRANSACTIONS = new QName("Transactions");
    private static final QName ELEMENT_INVESTMENT_DEALS = new QName("InvestmentDeals");
    private static final QName ELEMENT_EXCHANGE_SECURITY_SPLITS = new QName("ExchangeSecuritySplits");

    private static final ImportExportEvent DONE =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.DONE);
    private static final ImportExportEvent ICONS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.ICONS, 1);
    private static final ImportExportEvent CATEGORIES =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.CATEGORIES, 1);
    private static final ImportExportEvent CURRENCIES =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.CURRENCIES, 1);
    private static final ImportExportEvent EXCHANGE_SECURITIES =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.EXCHANGE_SECURITIES, 1);
    private static final ImportExportEvent ACCOUNTS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.ACCOUNTS, 1);
    private static final ImportExportEvent CARDS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.CARDS, 1);
    private static final ImportExportEvent CONTACTS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.CONTACTS, 1);
    private static final ImportExportEvent TRANSACTIONS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.TRANSACTIONS, 1);
    private static final ImportExportEvent INVESTMENTS_DEALS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.INVESTMENTS_DEALS, 1);
    private static final ImportExportEvent EXCHANGE_SECURITY_SPLITS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.EXCHANGE_SECURITY_SPLITS, 1);

    private final DataCache cache;

    public Export(DataCache cache) {
        this.cache = cache;
    }

    public void doExport(OutputStream out, Consumer<ImportExportEvent> progress) {
        try (var wrapper = XMLStreamWriterWrapper.newInstance(out)) {
            wrapper.document(ELEMENT_MONEY, () -> {
                progress.accept(ICONS);
                wrapper.element(ELEMENT_ICONS, () -> {
                    for (var icon : cache.getIcons()) {
                        serialize(wrapper, icon);
                    }
                });
                progress.accept(DONE);

                progress.accept(CATEGORIES);
                wrapper.element(ELEMENT_CATEGORIES, () -> {
                    for (var category : cache.getCategories()) {
                        serialize(wrapper, category);
                    }
                });
                progress.accept(DONE);

                progress.accept(CURRENCIES);
                wrapper.element(ELEMENT_CURRENCIES, () -> {
                    for (var currency : cache.getCurrencies()) {
                        serialize(wrapper, currency);
                    }
                });
                progress.accept(DONE);

                progress.accept(EXCHANGE_SECURITIES);
                wrapper.element(ELEMENT_EXCHANGE_SECURITIES, () -> {
                    for (var security : cache.getExchangeSecurities()) {
                        serialize(wrapper, security);
                    }
                });
                progress.accept(DONE);

                progress.accept(ACCOUNTS);
                wrapper.element(ELEMENT_ACCOUNTS, () -> {
                    for (var account : cache.getAccounts()) {
                        serialize(wrapper, account);
                    }
                });
                progress.accept(DONE);

                progress.accept(CARDS);
                wrapper.element(ELEMENT_CARDS, () -> {
                    for (var card : cache.getCards()) {
                        serialize(wrapper, card);
                    }
                });
                progress.accept(DONE);

                progress.accept(CONTACTS);
                wrapper.element(ELEMENT_CONTACTS, () -> {
                    for (var contact : cache.getContacts()) {
                        serialize(wrapper, contact);
                    }
                });
                progress.accept(DONE);

                progress.accept(TRANSACTIONS);
                wrapper.element(ELEMENT_TRANSACTIONS, () -> {
                    for (var transaction : cache.getTransactions().stream().filter(t -> t.parentUuid() == null).toList()) {
                        serialize(wrapper, transaction);
                    }
                    for (var transaction : cache.getTransactions().stream().filter(t -> t.parentUuid() != null).toList()) {
                        serialize(wrapper, transaction);
                    }
                });
                progress.accept(DONE);

                progress.accept(INVESTMENTS_DEALS);
                wrapper.element(ELEMENT_INVESTMENT_DEALS, () -> {
                    for (var investment : cache.getInvestmentDeals()) {
                        serialize(wrapper, investment);
                    }
                });
                progress.accept(DONE);

                progress.accept(EXCHANGE_SECURITY_SPLITS);
                wrapper.element(ELEMENT_EXCHANGE_SECURITY_SPLITS, () -> {
                    for (var split : cache.getExchangeSecuritySplits()) {
                        serialize(wrapper, split);
                    }
                });
                progress.accept(DONE);
            });
        }
    }
}
