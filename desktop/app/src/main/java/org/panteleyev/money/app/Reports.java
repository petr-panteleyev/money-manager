/*
 Copyright © 2019-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.Statement;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.desktop.commons.xml.RecordSerializer.serialize;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.TRANSFORMER_FACTORY;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.XML_OUTPUT_FACTORY;

public class Reports {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public record TransactionReportRecord(
            String date,
            String debitAccount,
            String creditAccount,
            String counterparty,
            String comment,
            String amount
    ) {
    }

    public record AccountReportRecord(
            String name,
            String category,
            String currency,
            String interest,
            String expiration,
            String comment,
            String balance
    ) {
    }

    public record StatementReportRecord(
            String date,
            String executionDate,
            String description,
            String amount
    ) {
    }

    static void reportTransactions(List<Transaction> transactions, OutputStream out) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            var writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(byteArrayOutputStream);
            writer.writeStartDocument();
            writer.writeStartElement("TransactionReportRecords");

            for (var t : transactions) {
                serialize(writer, new TransactionReportRecord(
                        DATE_FORMAT.format(t.transactionDate()),
                        cache().getAccount(t.accountDebitedUuid()).map(Account::name).orElse(""),
                        cache().getAccount(t.accountCreditedUuid()).map(Account::name).orElse(""),
                        cache().getContact(t.contactUuid()).map(Contact::name).orElse(""),
                        t.comment(),
                        formatAmount(Transaction.getSignedAmount(t))
                ));
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            try (var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                transform(byteArrayInputStream, out, "TransactionReport.xsl");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void reportAccounts(List<Account> accounts, OutputStream out) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            var writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(byteArrayOutputStream);
            writer.writeStartDocument();
            writer.writeStartElement("AccountReportRecords");

            for (var a : accounts) {
                serialize(writer, new AccountReportRecord(
                        a.name(),
                        cache().getCategory(a.categoryUuid()).map(Category::name).orElse(""),
                        cache().getCurrency(a.currencyUuid()).map(Currency::symbol).orElse(""),
                        formatAmount(a.interest()),
                        a.closingDate() == null ? "" : a.closingDate().toString(),
                        a.comment(),
                        Account.getBalance(a).toString()
                ));
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            try (var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                transform(byteArrayInputStream, out, "AccountReport.xsl");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void reportStatement(Statement statement, OutputStream out) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            var writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(byteArrayOutputStream);
            writer.writeStartDocument();
            writer.writeStartElement("StatementReportRecords");

            for (var r : statement.records()) {
                serialize(writer, new StatementReportRecord(
                        r.getActual().toString(),
                        r.getExecution().toString(),
                        r.getDescription(),
                        formatAmount(r.getAmountDecimal().orElse(BigDecimal.ZERO))
                ));
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            try (var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                transform(byteArrayInputStream, out, "StatementReport.xsl");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static void transform(InputStream inputStream, OutputStream outputStream, String xslFile) {
        try (var xsl = Reports.class.getResourceAsStream("/org/panteleyev/money/xsl/" + xslFile)) {
            var transformer = TRANSFORMER_FACTORY.newTransformer(new StreamSource(xsl));
            transformer.transform(new StreamSource(inputStream), new StreamResult(outputStream));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
