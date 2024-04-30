/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.app.investment.InvestmentSummaryTreeData;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.persistence.DataCache;

import java.util.Comparator;

import static org.panteleyev.money.app.Bundles.translate;
import static org.panteleyev.money.app.GlobalContext.cache;

public final class Comparators {
    private Comparators() {
    }

    private static final Comparator<Category> CATEGORIES_BY_NAME = Comparator.comparing(Category::name);

    private static final Comparator<Category> CATEGORIES_BY_TYPE = (o1, o2) -> {
        var typeString1 = translate(o1.type());
        var typeString2 = translate(o2.type());
        return typeString1.compareTo(typeString2);
    };

    private static final Comparator<Account> ACCOUNTS_BY_NAME = Comparator.comparing(Account::name);

    private static final Comparator<Account> ACCOUNTS_BY_CLOSING_DATE = (a1, a2) -> {
        if (a1.closingDate() == null && a2.closingDate() == null) {
            return 0;
        }

        if (a1.closingDate() != null && a2.closingDate() != null) {
            return a1.closingDate().compareTo(a2.closingDate());
        }

        if (a1.closingDate() != null) {
            return -1;
        } else {
            return 1;
        }
    };

    private static final Comparator<Transaction> TRANSACTION_BY_DATE =
            Comparator.comparing(Transaction::transactionDate)
                    .thenComparing(Transaction::created);

    private static final Comparator<InvestmentDeal> INVESTMENT_DEAL_BY_DEAL_DATE =
            Comparator.comparing(InvestmentDeal::dealDate)
                    .thenComparing(InvestmentDeal::created);

    private static final Comparator<InvestmentSummaryTreeData> INVESTMENT_SUMMARY_TREE_DATA_BY_PERCENTAGE =
            Comparator.comparing(InvestmentSummaryTreeData::percentage);

    public static Comparator<Category> categoriesByName() {
        return CATEGORIES_BY_NAME;
    }

    public static Comparator<Category> categoriesByType() {
        return CATEGORIES_BY_TYPE;
    }

    public static Comparator<Account> accountsByName() {
        return ACCOUNTS_BY_NAME;
    }

    public static Comparator<Account> accountsByClosingDate() {
        return ACCOUNTS_BY_CLOSING_DATE;
    }

    public static Comparator<InvestmentSummaryTreeData> investmentSummaryTreeDataByPercentage() {
        return INVESTMENT_SUMMARY_TREE_DATA_BY_PERCENTAGE;
    }

    public static Comparator<Account> accountsByCategory(DataCache cache) {
        return (a1, a2) -> {
            var c1 = cache.getCategory(a1.categoryUuid()).map(Category::name).orElse("");
            var c2 = cache.getCategory(a2.categoryUuid()).map(Category::name).orElse("");
            return c1.compareTo(c2);
        };
    }

    public static Comparator<Transaction> transactionsByDate() {
        return TRANSACTION_BY_DATE;
    }

    public static Comparator<Transaction> transactionsByDebitedAccountName(DataCache cache) {
        return (t1, t2) -> {
            var name1 = cache.getAccount(t1.accountDebitedUuid()).map(Account::name).orElse("");
            var name2 = cache.getAccount(t2.accountDebitedUuid()).map(Account::name).orElse("");
            return name1.compareTo(name2);
        };
    }

    public static Comparator<Transaction> transactionsByCreditedAccountName(DataCache cache) {
        return (t1, t2) -> {
            var name1 = cache.getAccount(t1.accountCreditedUuid()).map(Account::name).orElse("");
            var name2 = cache.getAccount(t2.accountCreditedUuid()).map(Account::name).orElse("");
            return name1.compareTo(name2);
        };
    }

    public static Comparator<Transaction> transactionsByContactName(DataCache cache) {
        return (t1, t2) -> {
            var name1 = cache.getContact(t1.contactUuid()).map(Contact::name).orElse("");
            var name2 = cache.getContact(t2.contactUuid()).map(Contact::name).orElse("");
            return name1.compareTo(name2);
        };
    }

    public static Comparator<Card> cardsByCategory(DataCache cache) {
        return (c1, c2) -> {
            var name1 = cache.getAccount(c1.accountUuid())
                    .map(Account::categoryUuid)
                    .flatMap(uuid -> cache().getCategory(uuid))
                    .map(Category::name)
                    .orElse("");
            var name2 = cache.getAccount(c2.accountUuid())
                    .map(Account::categoryUuid)
                    .flatMap(uuid -> cache().getCategory(uuid))
                    .map(Category::name)
                    .orElse("");
            return name1.compareTo(name2);
        };
    }

    public static Comparator<InvestmentDeal> investmentDealByDealDate() {
        return INVESTMENT_DEAL_BY_DEAL_DATE;
    }
}
