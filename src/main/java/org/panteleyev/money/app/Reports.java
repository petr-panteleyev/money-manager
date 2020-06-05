/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
import java.util.List;
import java.util.stream.Collectors;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;

class Reports {
    private static final String CSS_PATH = "/org/panteleyev/money/app/report.css";
    private static String css;

    static void reportTransactions(List<Transaction> transactions, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Transactions");

            w.println("<table>\n<tr>");
            th(w, RB.getString("column.Day"));
            th(w, RB.getString("column.Account.Debited"));
            th(w, RB.getString("column.Account.Credited"));
            th(w, RB.getString("column.Payer.Payee"));
            th(w, RB.getString("Comment"));
            th(w, RB.getString("column.Sum"));

            for (var t : transactions) {
                w.print("<tr>");
                td(w, String.format("%02d.%02d.%04d", t.day(), t.month(), t.year()));
                td(w, cache().getAccount(t.accountDebitedUuid()).map(Account::name).orElse(""));
                td(w, cache().getAccount(t.accountCreditedUuid()).map(Account::name).orElse(""));
                td(w, cache().getContact(t.contactUuid()).map(Contact::name).orElse(""));
                td(w, t.comment());
                td(w, "amount", formatAmount(t.getSignedAmount()));
                w.println();
            }

            printFooter(w);
        }
    }

    static void reportAccounts(List<Account> accounts, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Accounts");

            w.println("<table>\n<tr>");
            th(w, RB.getString("column.Name"));
            th(w, RB.getString("column.Category"));
            th(w, RB.getString("Currency"));
            w.println("<th>%%");
            th(w, RB.getString("column.closing.date"));
            th(w, RB.getString("Comment"));
            th(w, RB.getString("column.Balance"));

            for (var a : accounts) {
                w.println("<tr>");
                td(w, a.name());
                td(w, cache().getCategory(a.categoryUuid())
                    .map(Category::name).orElse(""));
                td(w, cache().getCurrency(a.currencyUuid())
                    .map(Currency::symbol).orElse(""));
                td(w, formatAmount(a.interest()));
                td(w, a.closingDate().toString());
                td(w, a.comment());
                td(w, "amount", cache().calculateBalance(a, true, t -> true).toString());
                w.println();
            }

            printFooter(w);
        }
    }

    static void reportStatement(Statement statement, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, "Statement");

            w.println("<table>\n<tr>");
            th(w, RB.getString("column.Date"));
            th(w, RB.getString("column.ExecutionDate"));
            th(w, RB.getString("column.Description"));
            th(w, RB.getString("column.Sum"));

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
            switch (ch) {
                case '"':
                    result.append("&quot;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                default:
                    result.append(ch);
                    break;
            }
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