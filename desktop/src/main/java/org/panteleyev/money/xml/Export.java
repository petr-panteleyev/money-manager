/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.function.Consumer;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.xml.RecordSerializer.serialize;

public class Export {
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

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
    private static final ImportExportEvent DOCUMENTS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.DOCUMENTS, 1);
    private static final ImportExportEvent PERIODIC_PAYMENTS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.PERIODIC_PAYMENTS, 1);
    private static final ImportExportEvent INVESTMENTS_DEALS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.INVESTMENTS_DEALS, 1);
    private static final ImportExportEvent EXCHANGE_SECURITY_SPLITS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.EXCHANGE_SECURITY_SPLITS, 1);
    private static final ImportExportEvent BLOBS =
            new ImportExportEvent(ImportExportEvent.ImportExportEventType.BLOBS, 1);

    private final DataCache cache;
    private final MoneyDAO dao;

    public Export() {
        this(cache(), dao());
    }

    public Export(DataCache cache, MoneyDAO dao) {
        this.cache = cache;
        this.dao = dao;
    }

    public void doExport(OutputStream out, Consumer<ImportExportEvent> progress) {
        try {
            var writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writer.writeStartDocument();
            writer.writeStartElement("Money");

            progress.accept(ICONS);
            exportBlock(writer, "Icons", () -> {
                for (var icon : cache.getIcons()) {
                    serialize(writer, icon);
                }
            });
            progress.accept(DONE);

            progress.accept(CATEGORIES);
            exportBlock(writer, "Categories", () -> {
                for (var category : cache.getCategories()) {
                    serialize(writer, category);
                }
            });
            progress.accept(DONE);

            progress.accept(CURRENCIES);
            exportBlock(writer, "Currencies", () -> {
                for (var currency : cache.getCurrencies()) {
                    serialize(writer, currency);
                }
            });
            progress.accept(DONE);

            progress.accept(EXCHANGE_SECURITIES);
            exportBlock(writer, "ExchangeSecurities", () -> {
                for (var security : cache.getExchangeSecurities()) {
                    serialize(writer, security);
                }
            });
            progress.accept(DONE);

            progress.accept(ACCOUNTS);
            exportBlock(writer, "Accounts", () -> {
                for (var account : cache.getAccounts()) {
                    serialize(writer, account);
                }
            });
            progress.accept(DONE);

            progress.accept(CARDS);
            exportBlock(writer, "Cards", () -> {
                for (var card : cache.getCards()) {
                    serialize(writer, card);
                }
            });
            progress.accept(DONE);

            progress.accept(CONTACTS);
            exportBlock(writer, "Contacts", () -> {
                for (var contact : cache.getContacts()) {
                    serialize(writer, contact);
                }
            });
            progress.accept(DONE);

            progress.accept(TRANSACTIONS);
            exportBlock(writer, "Transactions", () -> {
                for (var transaction : cache.getTransactions().stream().filter(t -> t.parentUuid() == null).toList()) {
                    serialize(writer, transaction);
                }
                for (var transaction : cache.getTransactions().stream().filter(t -> t.parentUuid() != null).toList()) {
                    serialize(writer, transaction);
                }
            });
            progress.accept(DONE);

            progress.accept(DOCUMENTS);
            exportBlock(writer, "MoneyDocuments", () -> {
                for (var document : cache.getDocuments()) {
                    serialize(writer, document);
                }
            });
            progress.accept(DONE);

            progress.accept(PERIODIC_PAYMENTS);
            exportBlock(writer, "PeriodicPayments", () -> {
                for (var payment : cache.getPeriodicPayments()) {
                    serialize(writer, payment);
                }
            });
            progress.accept(DONE);

            progress.accept(INVESTMENTS_DEALS);
            exportBlock(writer, "InvestmentDeals", () -> {
                for (var investment : cache.getInvestmentDeals()) {
                    serialize(writer, investment);
                }
            });
            progress.accept(DONE);

            progress.accept(EXCHANGE_SECURITY_SPLITS);
            exportBlock(writer, "ExchangeSecuritySplits", () -> {
                for (var split : cache.getExchangeSecuritySplits()) {
                    serialize(writer, split);
                }
            });
            progress.accept(DONE);

            progress.accept(BLOBS);
            exportBlock(writer, "Blobs", () -> {
                for (var document : cache.getDocuments()) {
                    exportDocumentContent(writer, document);
                }
            });
            progress.accept(DONE);

            writer.writeEndDocument();
            writer.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void exportBlock(XMLStreamWriter writer, String name, Runnable runnable) throws XMLStreamException {
        writer.writeStartElement(name);
        runnable.run();
        writer.writeEndElement();
    }

    private void exportDocumentContent(XMLStreamWriter writer, MoneyDocument moneyDocument) {
        serialize(writer, new BlobContent(
                moneyDocument.uuid(),
                BlobContent.BlobType.DOCUMENT,
                dao.getDocumentBytes(moneyDocument)
        ));
    }
}
