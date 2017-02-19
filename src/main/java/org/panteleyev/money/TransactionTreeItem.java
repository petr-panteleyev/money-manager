/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionTreeItem implements Styles {
    private final IntegerProperty dayProperty;
    private final IntegerProperty monthProperty;
    private final IntegerProperty yearProperty;
    private final ObjectProperty<BigDecimal>  sumProperty;
    private final StringProperty  accountDebitedProperty;
    private final StringProperty  accountCreditedProperty;
    private final StringProperty  commentProperty;
    private final StringProperty  contactProperty;
    private final StringProperty  typeProperty;
    private final ObjectProperty<Boolean> approvedProperty;

    private final BooleanProperty isGroupProperty;

    private final Transaction transaction;
    private final List<TransactionTreeItem> children;
    private final String style;

    public TransactionTreeItem(Transaction t) {
        transaction = t;
        children = null;
        isGroupProperty = new SimpleBooleanProperty(false);

        MoneyDAO dao = MoneyDAO.getInstance();

        dayProperty = new SimpleIntegerProperty(this, "day", t.getDay());
        monthProperty = new SimpleIntegerProperty(this, "month", t.getMonth());
        yearProperty = new SimpleIntegerProperty(this, "year", t.getYear());

        accountDebitedProperty = new SimpleStringProperty(this, "accountDebited",
            dao.getAccount(t.getAccountDebitedId()).map(Account::getName).orElse(""));

        accountCreditedProperty = new SimpleStringProperty(this, "accountCredited",
            dao.getAccount(t.getAccountCreditedId()).map(Account::getName).orElse(""));

        contactProperty = new SimpleStringProperty(this, "contact",
                dao.getContact(t.getContactId()).map(Contact::getName).orElse(""));

        typeProperty = new SimpleStringProperty(this, "type",
                t.getTransactionType().getName());

        commentProperty = new SimpleStringProperty(this, "comment", t.getComment());

        approvedProperty = new SimpleObjectProperty<>(this, "approved", t.isChecked());

        String s = BLACK_TEXT;
        if (t.getAccountCreditedType() != t.getAccountDebitedType()) {
            if (t.getAccountDebitedType() == CategoryType.INCOMES) {
                s = BLUE_TEXT;
            } else {
                s = RED_TEXT;
            }
        }
        style = s;

        sumProperty = new SimpleObjectProperty<>(this, "sum", sumToShow(t));
    }

    public TransactionTreeItem(TransactionGroup gr, List<Transaction> trs) {
        transaction = null;
        isGroupProperty = new SimpleBooleanProperty(true);

        MoneyDAO dao = MoneyDAO.getInstance();

        children = new ArrayList<>(trs.size());
        BigDecimal sum = new BigDecimal(0);

        Integer day = null;
        Integer month = null;
        Integer year = null;
        String accountDebitedName = null;
        String comment = null;
        String typeName = null;

        for (Transaction t : trs) {
            children.add(new TransactionTreeItem(t));

            if (day == null) {
                day = t.getDay();
                month = t.getMonth();
                year = t.getYear();
            }

            sum = sum.add(sumToShow(t));

            if (accountDebitedName == null || accountDebitedName.isEmpty()) {
                accountDebitedName = dao.getAccount(t.getAccountDebitedId())
                        .map(Account::getName)
                        .orElse("");
            }

            if (typeName == null || typeName.isEmpty()) {
                typeName = t.getTransactionType().getName();
            }

            if (comment == null || comment.isEmpty()) {
                comment = t.getComment();
            }
        }

        String contactString = trs.stream()
                .map(Transaction::getContactId)
                .filter(x -> (x != null))
                .distinct()
                .map(dao::getContact)
                .filter(Optional::isPresent)
                .map(o -> o.get().getName())
                .collect(Collectors.joining(" + "));

        String accountCreditedString;

        List<Integer> accCredIDs = trs.stream()
                .map(Transaction::getAccountCreditedId)
                .filter(x -> (x != null))
                .distinct()
                .collect(Collectors.toList());

        if (accCredIDs.size() == 1) {
            accountCreditedString = dao.getAccount(accCredIDs.get(0))
                    .map(Account::getName)
                    .orElse("");
        } else {
            accountCreditedString = accCredIDs.size() + " accounts";
        }

        dayProperty = new SimpleIntegerProperty(this, "day", day);
        monthProperty = new SimpleIntegerProperty(this, "month", month);
        yearProperty = new SimpleIntegerProperty(this, "year", year);

        accountDebitedProperty = new SimpleStringProperty(this, "accountDebited", accountDebitedName);
        accountCreditedProperty = new SimpleStringProperty(this, "accountCredited", accountCreditedString);
        contactProperty = new SimpleStringProperty(this, "contact", contactString);
        typeProperty = new SimpleStringProperty(this, "type", typeName);

        sumProperty = new SimpleObjectProperty<>(this, "sum", sum);
        commentProperty = new SimpleStringProperty(this, "comment", comment);

        approvedProperty = new SimpleObjectProperty<>(this, "approved", false);

        style = (sum.signum() < 0)? RED_TEXT : BLACK_TEXT;
    }

    private BigDecimal sumToShow(Transaction t) {
        BigDecimal sum = t.getAmount();
        if (t.getAccountCreditedType() != t.getAccountDebitedType()
                && t.getAccountDebitedType() != CategoryType.INCOMES) {
            sum = sum.negate();
        }
        return sum;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public List<TransactionTreeItem> getChildren() {
        return children;
    }

    BooleanProperty isGroupProperty() {
        return isGroupProperty;
    }

    IntegerProperty dayProperty() {
        return dayProperty;
    }

    IntegerProperty monthProperty() {
        return monthProperty;
    }

    IntegerProperty yearProperty() {
        return yearProperty;
    }

    public ObjectProperty<BigDecimal> sumProperty() {
        return sumProperty;
    }

    public StringProperty accountDebitedProperty() {
        return accountDebitedProperty;
    }

    public StringProperty accountCreditedProperty() {
        return accountCreditedProperty;
    }

    public StringProperty commentProperty() {
        return commentProperty;
    }

    public StringProperty contactProperty() {
        return contactProperty;
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public ObjectProperty<Boolean> approvedProperty() {
        return approvedProperty;
    }

    public String getStyle() {
        return style;
    }
}
