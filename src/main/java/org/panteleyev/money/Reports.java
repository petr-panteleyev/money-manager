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

package org.panteleyev.money;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
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
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class Reports {
    private static final String CSS_PATH = "/org/panteleyev/money/report.css";
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
            th(w, RB.getString("column.Comment"));
            th(w, RB.getString("column.Sum"));

            for (var t : transactions) {
                w.print("<tr>");
                td(w, String.format("%02d.%02d.%04d", t.getDay(), t.getMonth(), t.getYear()));
                td(w, getDao().getAccount(t.getAccountDebitedUuid()).map(Account::getName).orElse(""));
                td(w, getDao().getAccount(t.getAccountCreditedUuid()).map(Account::getName).orElse(""));
                td(w, getDao().getContact(t.getContactUuid().orElse(null)).map(Contact::getName).orElse(""));
                td(w, t.getComment());
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
            th(w, RB.getString("column.Currency"));
            w.println("<th>%%");
            th(w, RB.getString("column.closing.date"));
            th(w, RB.getString("column.Comment"));
            th(w, RB.getString("column.Balance"));

            for (var a : accounts) {
                w.println("<tr>");
                td(w, a.getName());
                td(w, getDao().getCategory(a.getCategoryUuid())
                    .map(Category::getName).orElse(""));
                td(w, getDao().getCurrency(a.getCurrencyUuid().orElse(null))
                    .map(Currency::getSymbol).orElse(""));
                td(w, formatAmount(a.getInterest()));
                td(w, a.getClosingDate().toString());
                td(w, a.getComment());
                td(w, "amount", a.calculateBalance(true, t -> true).toString());
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

            for (var r : statement.getRecords()) {
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
