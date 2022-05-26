/*
 Copyright (C) 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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

import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CREDITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEBITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_EXECUTION_DATE_SHORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACCOUNTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_BALANCE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CATEGORY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DAY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TRANSACTIONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_UNTIL;

class Reports {
    private static final String CSS_PATH = "/org/panteleyev/money/report.css";
    private static String css;

    static void reportTransactions(List<Transaction> transactions, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, UI.getString(I18N_WORD_TRANSACTIONS));

            w.println("<table>\n<tr>");
            th(w, UI.getString(I18N_WORD_DAY));
            th(w, UI.getString(I18N_MISC_DEBITED_ACCOUNT));
            th(w, UI.getString(I18N_MISC_CREDITED_ACCOUNT));
            th(w, UI.getString(I18N_WORD_COUNTERPARTY));
            th(w, UI.getString(I18N_WORD_COMMENT));
            th(w, UI.getString(I18N_WORD_SUM));

            for (var t : transactions) {
                w.print("<tr>");
                td(w, String.format("%02d.%02d.%04d", t.day(), t.month(), t.year()));
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

    static void reportAccounts(List<Account> accounts, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, fxString(UI, I18N_WORD_ACCOUNTS));

            w.println("<table>\n<tr>");
            th(w, UI.getString(I18N_WORD_ENTITY_NAME));
            th(w, UI.getString(I18N_WORD_CATEGORY));
            th(w, UI.getString(I18N_WORD_CURRENCY));
            w.println("<th>%%");
            th(w, UI.getString(I18N_WORD_UNTIL));
            th(w, UI.getString(I18N_WORD_COMMENT));
            th(w, UI.getString(I18N_WORD_BALANCE));

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
                td(w, "amount", Account.getBalance(a).toString());
                w.println();
            }

            printFooter(w);
        }
    }

    static void reportStatement(Statement statement, OutputStream out) {
        loadCss();
        try (var w = new PrintWriter(out)) {
            printHeader(w, UI.getString(I18N_WORD_STATEMENT));

            w.println("<table>\n<tr>");
            th(w, UI.getString(I18N_WORD_DATE));
            th(w, UI.getString(I18N_MISC_EXECUTION_DATE_SHORT));
            th(w, UI.getString(I18N_WORD_DESCRIPTION));
            th(w, UI.getString(I18N_WORD_SUM));

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
