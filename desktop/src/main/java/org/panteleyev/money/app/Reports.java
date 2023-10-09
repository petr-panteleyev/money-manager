/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.Statement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.panteleyev.money.app.GlobalContext.cache;

public class Reports {
    private static final String CSS_PATH = "/org/panteleyev/money/report.css";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static String css;

    static void reportTransactions(List<Transaction> transactions, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Проводки");

            w.println("<table>\n<tr>");
            th(w, "День");
            th(w, "Исходный счет");
            th(w, "Счет получателя");
            th(w, "Контрагент");
            th(w, "Комментарий");
            th(w, "Сумма");

            for (var t : transactions) {
                w.print("<tr>");
                td(w, DATE_FORMAT.format(t.transactionDate()));
                td(w, cache().getAccount(t.accountDebitedUuid()).map(Account::name).orElse(""));
                td(w, cache().getAccount(t.accountCreditedUuid()).map(Account::name).orElse(""));
                td(w, cache().getContact(t.contactUuid()).map(Contact::name).orElse(""));
                td(w, t.comment());
                td(w, "amount", formatAmount(Transaction.getSignedAmount(t)));
                w.println();
            }

            printFooter(w);
        }
    }

    public static void reportAccounts(List<Account> accounts, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Счета");

            w.println("<table>\n<tr>");
            th(w, "Название");
            th(w, "Категория");
            th(w, "Валюта");
            w.println("<th>%%");
            th(w, "До");
            th(w, "Комментарий");
            th(w, "Баланс");

            for (var a : accounts) {
                w.println("<tr>");
                td(w, a.name());
                td(w, cache().getCategory(a.categoryUuid())
                        .map(Category::name).orElse(""));
                td(w, cache().getCurrency(a.currencyUuid())
                        .map(Currency::symbol).orElse(""));
                td(w, formatAmount(a.interest()));
                td(w, a.closingDate() == null ? "" : a.closingDate().toString());
                td(w, a.comment());
                td(w, "amount", Account.getBalance(a).toString());
                w.println();
            }

            printFooter(w);
        }
    }

    static void reportStatement(Statement statement, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Выписка");

            w.println("<table>\n<tr>");
            th(w, "Дата");
            th(w, "Дата исп.");
            th(w, "Описание");
            th(w, "Сумма");

            for (var r : statement.records()) {
                w.println("<tr>");
                td(w, r.getActual().toString());
                td(w, r.getExecution().toString());
                td(w, r.getDescription());
                td(w, "amount", formatAmount(r.getAmountDecimal().orElse(BigDecimal.ZERO)));
                w.println();
            }

            printFooter(w);
        }
    }

    private static void printHeader(PrintWriter w, String title) {
        w.println("<html>\n<head>\n<title>" + title + "</title>\n<style>\n" + css + "\n</style>\n<head>\n<body>");
    }

    private static void printFooter(PrintWriter w) {
        w.println("</table>\n</body>\n</html>");
    }

    private static void th(PrintWriter w, String text) {
        w.print("<th>" + escape(text));
    }

    private static void td(PrintWriter w, String text) {
        w.print("<td>" + escape(text));
    }

    private static void td(PrintWriter w, String cssClass, String text) {
        w.print("<td class='" + cssClass + "'>" + escape(text));
    }

    private static String escape(String s) {
        var result = new StringBuilder();

        for (var ch : s.toCharArray()) {
            result.append(
                    switch (ch) {
                        case '"' -> "&quot;";
                        case '&' -> "&amp;";
                        case '<' -> "&lt;";
                        case '>' -> "&gt;";
                        default -> ch;
                    }
            );
        }

        return result.toString();
    }

    private static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static void loadCss() {
        synchronized (Reports.class) {
            if (css != null) {
                return;
            }

            try (var in = Reports.class.getResourceAsStream(CSS_PATH);
                 var reader = new BufferedReader(new InputStreamReader(in))) {
                css = reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
