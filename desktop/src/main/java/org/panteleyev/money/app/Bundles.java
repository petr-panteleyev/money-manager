/*
 Copyright © 2020-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.bundles.CategoryTypeBundle;
import org.panteleyev.money.bundles.ContactTypeBundle;
import org.panteleyev.money.bundles.DocumentTypeBundle;
import org.panteleyev.money.bundles.PeriodicPaymentTypeBundle;
import org.panteleyev.money.bundles.RecurrenceTypeBundle;
import org.panteleyev.money.bundles.TransactionPredicateBundle;
import org.panteleyev.money.bundles.TransactionTypeBundle;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;
import org.panteleyev.money.model.TransactionType;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

public final class Bundles {
    private static final ResourceBundle CATEGORY_TYPE_BUNDLE =
            getBundle(CategoryTypeBundle.class.getCanonicalName());
    private static final ResourceBundle CONTACT_TYPE_BUNDLE =
            getBundle(ContactTypeBundle.class.getCanonicalName());
    private static final ResourceBundle TRANSACTION_TYPE_BUNDLE =
            getBundle(TransactionTypeBundle.class.getCanonicalName());
    private static final ResourceBundle DOCUMENT_TYPE_BUNDLE =
            getBundle(DocumentTypeBundle.class.getCanonicalName());
    private static final ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
            getBundle(TransactionPredicateBundle.class.getCanonicalName());
    private static final ResourceBundle PERIODIC_PAYMENT_TYPE_BUNDLE =
            getBundle(PeriodicPaymentTypeBundle.class.getCanonicalName());
    private static final ResourceBundle RECURRENCE_TYPE_BUNDLE =
            getBundle(RecurrenceTypeBundle.class.getCanonicalName());

    public static String translate(CategoryType type) {
        return CATEGORY_TYPE_BUNDLE.getString(type.name() + "_name");
    }

    public static String translate(ContactType type) {
        return CONTACT_TYPE_BUNDLE.getString(type.name());
    }

    public static String translate(TransactionType type) {
        return TRANSACTION_TYPE_BUNDLE.getString(type.name());
    }

    public static String translate(DocumentType type) {
        return DOCUMENT_TYPE_BUNDLE.getString(type.name());
    }

    public static String translate(TransactionPredicate predicate) {
        return TRANSACTION_PREDICATE_BUNDLE.getString(predicate.name());
    }

    public static String translate(PeriodicPaymentType type) {
        return PERIODIC_PAYMENT_TYPE_BUNDLE.getString(type.name());
    }

    public static String translate(RecurrenceType type) {
        return RECURRENCE_TYPE_BUNDLE.getString(type.name());
    }

    public static String translate(Month month) {
        return month == null? "" : month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    private Bundles() {
    }
}
