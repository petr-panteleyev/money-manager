/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.xml.XMLUtils.appendTextNode;

public class Export {
    static final String DOCUMENTS_ZIP_DIRECTORY = "documents/";

    private final DataCache cache;
    private final MoneyDAO dao;

    public Export() {
        this(cache(), dao());
    }

    public Export(DataCache cache, MoneyDAO dao) {
        this.cache = cache;
        this.dao = dao;
    }

    public void doExport(ZipOutputStream out) {
        try {
            exportMainEntry(out);
            exportDocuments(out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void exportMainEntry(ZipOutputStream out) throws IOException {
        var entryName = generateFileName() + ".xml";
        out.putNextEntry(new ZipEntry(entryName));

        var rootElement = XMLUtils.createDocument("Money");
        var doc = rootElement.getOwnerDocument();

        var iconRoot = XMLUtils.appendElement(rootElement, "Icons");
        for (var icon : cache.getIcons()) {
            iconRoot.appendChild(exportIcon(doc, icon));
        }

        var accountRoot = XMLUtils.appendElement(rootElement, "Accounts");
        for (var account : cache.getAccounts()) {
            accountRoot.appendChild(exportAccount(doc, account));
        }

        var cardRoot = XMLUtils.appendElement(rootElement, "Cards");
        for (var card : cache.getCards()) {
            cardRoot.appendChild(exportCard(doc, card));
        }

        var categoryRoot = XMLUtils.appendElement(rootElement, "Categories");
        for (var category : cache.getCategories()) {
            categoryRoot.appendChild(exportCategory(doc, category));
        }

        var contactRoot = XMLUtils.appendElement(rootElement, "Contacts");
        for (var contact : cache.getContacts()) {
            contactRoot.appendChild(exportContact(doc, contact));
        }

        var currencyRoot = XMLUtils.appendElement(rootElement, "Currencies");
        for (var currency : cache.getCurrencies()) {
            currencyRoot.appendChild(exportCurrency(doc, currency));
        }

        var exchangeSecurityRoot = XMLUtils.appendElement(rootElement, "ExchangeSecurities");
        for (var security : cache.getExchangeSecurities()) {
            exchangeSecurityRoot.appendChild(exportExchangeSecurity(doc, security));
        }

        var transactionRoot = XMLUtils.appendElement(rootElement, "Transactions");
        for (var transaction : cache.getTransactions()) {
            transactionRoot.appendChild(exportTransaction(doc, transaction));
        }

        var documentRoot = XMLUtils.appendElement(rootElement, "Documents");
        for (var document : cache.getDocuments()) {
            documentRoot.appendChild(exportDocument(doc, document));
        }

        var periodicPaymentRoot = XMLUtils.appendElement(rootElement, "PeriodicPayments");
        for (var payment : cache.getPeriodicPayments()) {
            periodicPaymentRoot.appendChild(exportPeriodicPayment(doc, payment));
        }

        var investmentRoot = XMLUtils.appendElement(rootElement, "InvestmentDeals");
        for (var investment: cache.getInvestmentDeals()) {
            investmentRoot.appendChild(exportInvestment(doc, investment));
        }

        XMLUtils.writeDocument(doc, out);
        out.closeEntry();
    }

    private void exportDocuments(ZipOutputStream out) throws IOException {
        out.putNextEntry(new ZipEntry(DOCUMENTS_ZIP_DIRECTORY));
        out.closeEntry();

        for (var document : cache.getDocuments()) {
            out.putNextEntry(new ZipEntry(DOCUMENTS_ZIP_DIRECTORY + document.uuid()));
            var bytes = dao.getDocumentBytes(document);
            out.write(bytes);
            out.closeEntry();
        }
    }

    private static Element exportIcon(Document doc, Icon icon) {
        var e = doc.createElement("Icon");

        appendTextNode(e, "uuid", icon.uuid());
        appendTextNode(e, "name", icon.name());
        appendTextNode(e, "bytes", icon.bytes());
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
        appendTextNode(e, "securityUuid", account.securityUuid());
        appendTextNode(e, "enabled", account.enabled());
        appendTextNode(e, "interest", account.interest());
        appendTextNode(e, "closingDate", account.closingDate());
        appendTextNode(e, "iconUuid", account.iconUuid());
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

    private static Element exportExchangeSecurity(Document doc, ExchangeSecurity security) {
        var e = doc.createElement("ExchangeSecurity");

        appendTextNode(e, "uuid", security.uuid());
        appendTextNode(e, "secId", security.secId());
        appendTextNode(e, "name", security.name());
        appendTextNode(e, "shortName", security.shortName());
        appendTextNode(e, "isin", security.isin());
        appendTextNode(e, "regNumber", security.regNumber());
        appendTextNode(e, "faceValue", security.faceValue());
        appendTextNode(e, "issueDate", security.issueDate());
        appendTextNode(e, "matDate", security.matDate());
        appendTextNode(e, "daysToRedemption", security.daysToRedemption());
        appendTextNode(e, "group", security.group());
        appendTextNode(e, "groupName", security.groupName());
        appendTextNode(e, "type", security.type());
        appendTextNode(e, "typeName", security.typeName());
        appendTextNode(e, "marketValue", security.marketValue());
        appendTextNode(e, "couponValue", security.couponValue());
        appendTextNode(e, "couponPercent", security.couponPercent());
        appendTextNode(e, "couponDate", security.couponDate());
        appendTextNode(e, "couponFrequency", security.couponFrequency());
        appendTextNode(e, "accruedInterest", security.accruedInterest());
        appendTextNode(e, "couponPeriod", security.couponPeriod());
        appendTextNode(e, "created", security.created());
        appendTextNode(e, "modified", security.modified());

        return e;
    }

    private static Element exportTransaction(Document doc, Transaction t) {
        var e = doc.createElement("Transaction");

        appendTextNode(e, "amount", t.amount());
        appendTextNode(e, "creditAmount", t.creditAmount());
        appendTextNode(e, "transactionDate", t.transactionDate());
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
        appendTextNode(e, "invoiceNumber", t.invoiceNumber());
        appendTextNode(e, "guid", t.uuid());
        appendTextNode(e, "created", t.created());
        appendTextNode(e, "modified", t.modified());
        appendTextNode(e, "parentUuid", t.parentUuid());
        appendTextNode(e, "detailed", t.detailed());
        appendTextNode(e, "statementDate", t.statementDate());
        appendTextNode(e, "cardUuid", t.cardUuid());

        return e;
    }

    private static Element exportDocument(Document doc, MoneyDocument moneyDocument) {
        var e = doc.createElement("Document");

        appendTextNode(e, "uuid", moneyDocument.uuid());
        appendTextNode(e, "ownerUuid", moneyDocument.ownerUuid());
        appendTextNode(e, "contactUuid", moneyDocument.contactUuid());
        appendTextNode(e, "type", moneyDocument.documentType());
        appendTextNode(e, "fileName", moneyDocument.fileName());
        appendTextNode(e, "date", moneyDocument.date());
        appendTextNode(e, "size", moneyDocument.size());
        appendTextNode(e, "mimeType", moneyDocument.mimeType());
        appendTextNode(e, "description", moneyDocument.description());
        appendTextNode(e, "created", moneyDocument.created());
        appendTextNode(e, "modified", moneyDocument.modified());

        return e;
    }

    private static Element exportPeriodicPayment(Document doc, PeriodicPayment payment) {
        var e = doc.createElement("PeriodicPayment");

        appendTextNode(e, "uuid", payment.uuid());
        appendTextNode(e, "name", payment.name());
        appendTextNode(e, "paymentType", payment.paymentType());
        appendTextNode(e, "recurrenceType", payment.recurrenceType());
        appendTextNode(e, "amount", payment.amount());
        appendTextNode(e, "dayOfMonth", payment.dayOfMonth());
        appendTextNode(e, "month", payment.month());
        appendTextNode(e, "accountDebitedUuid", payment.accountDebitedUuid());
        appendTextNode(e, "accountCreditedUuid", payment.accountCreditedUuid());
        appendTextNode(e, "contactUuid", payment.contactUuid());
        appendTextNode(e, "comment", payment.comment());
        appendTextNode(e, "created", payment.created());
        appendTextNode(e, "modified", payment.modified());

        return e;
    }

    private static Element exportCard(Document doc, Card card) {
        var e = doc.createElement("Card");

        appendTextNode(e, "uuid", card.uuid());
        appendTextNode(e, "accountUuid", card.accountUuid());
        appendTextNode(e, "type", card.type());
        appendTextNode(e, "number", card.number());
        appendTextNode(e, "expiration", card.expiration());
        appendTextNode(e, "comment", card.comment());
        appendTextNode(e, "enabled", card.enabled());
        appendTextNode(e, "created", card.created());
        appendTextNode(e, "modified", card.modified());

        return e;
    }

    private static Element exportInvestment(Document doc, InvestmentDeal investmentDeal) {
        var e = doc.createElement("InvestmentDeal");

        appendTextNode(e, "uuid", investmentDeal.uuid());
        appendTextNode(e, "accountUuid", investmentDeal.accountUuid());
        appendTextNode(e, "securityUuid", investmentDeal.securityUuid());
        appendTextNode(e, "currencyUuid", investmentDeal.currencyUuid());
        appendTextNode(e, "dealNumber", investmentDeal.dealNumber());
        appendTextNode(e, "dealDate", investmentDeal.dealDate());
        appendTextNode(e, "accountingDate", investmentDeal.accountingDate());
        appendTextNode(e, "marketType", investmentDeal.marketType());
        appendTextNode(e, "operationType", investmentDeal.operationType());
        appendTextNode(e, "securityAmount", investmentDeal.securityAmount());
        appendTextNode(e, "price", investmentDeal.price());
        appendTextNode(e, "aci", investmentDeal.aci());
        appendTextNode(e, "dealVolume", investmentDeal.dealVolume());
        appendTextNode(e, "rate", investmentDeal.rate());
        appendTextNode(e, "exchangeFee", investmentDeal.exchangeFee());
        appendTextNode(e, "brokerFee", investmentDeal.brokerFee());
        appendTextNode(e, "amount", investmentDeal.amount());
        appendTextNode(e, "dealType", investmentDeal.dealType());
        appendTextNode(e, "created", investmentDeal.created());
        appendTextNode(e, "modified", investmentDeal.modified());

        return e;
    }
}
