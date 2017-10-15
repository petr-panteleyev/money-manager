/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.Transaction;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

public final class TransactionXml {
    private int id;
    private BigDecimal amount;
    private int day;
    private int month;
    private int year;
    private int transactionTypeId;
    private String comment;
    private boolean checked;
    private int accountDebitedId;
    private int accountCreditedId;
    private int accountDebitedTypeId;
    private int accountCreditedTypeId;
    private int accountDebitedCategoryId;
    private int accountCreditedCategoryId;
    private int groupId;
    private int contactId;
    private BigDecimal rate;
    private int rateDirection;
    private String invoiceNumber;
    private String guid;
    private long modified;

    public TransactionXml() {
    }

    public TransactionXml(Transaction t) {
        id = t.getId();
        amount = t.getAmount();
        day = t.getDay();
        month = t.getMonth();
        year = t.getYear();
        transactionTypeId = t.getTransactionTypeId();
        comment = t.getComment();
        checked = t.getChecked();
        accountDebitedId = t.getAccountDebitedId();
        accountCreditedId = t.getAccountCreditedId();
        accountDebitedTypeId = t.getAccountDebitedTypeId();
        accountCreditedTypeId = t.getAccountCreditedTypeId();
        accountDebitedCategoryId = t.getAccountDebitedCategoryId();
        accountCreditedCategoryId = t.getAccountCreditedCategoryId();
        groupId = t.getGroupId();
        contactId = t.getContactId();
        rate = t.getRate();
        rateDirection = t.getRateDirection();
        invoiceNumber = t.getInvoiceNumber();
        guid = t.getGuid();
        modified = t.getModified();
    }

    @XmlAttribute(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement(name = "amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @XmlElement(name = "day")
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @XmlElement(name = "month")
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @XmlElement(name = "year")
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @XmlElement(name = "transactionTypeId")
    public int getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(int transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    @XmlElement(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElement(name = "checked")
    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @XmlElement(name = "accountDebitedId")
    public int getAccountDebitedId() {
        return accountDebitedId;
    }

    public void setAccountDebitedId(int accountDebitedId) {
        this.accountDebitedId = accountDebitedId;
    }

    @XmlElement(name = "accountCreditedId")
    public int getAccountCreditedId() {
        return accountCreditedId;
    }

    public void setAccountCreditedId(int accountCreditedId) {
        this.accountCreditedId = accountCreditedId;
    }

    @XmlElement(name = "accountDebitedTypeId")
    public int getAccountDebitedTypeId() {
        return accountDebitedTypeId;
    }

    public void setAccountDebitedTypeId(int accountDebitedTypeId) {
        this.accountDebitedTypeId = accountDebitedTypeId;
    }

    @XmlElement(name = "accountCreditedTypeId")
    public int getAccountCreditedTypeId() {
        return accountCreditedTypeId;
    }

    public void setAccountCreditedTypeId(int accountCreditedTypeId) {
        this.accountCreditedTypeId = accountCreditedTypeId;
    }

    @XmlElement(name = "accountDebitedCategoryId")
    public int getAccountDebitedCategoryId() {
        return accountDebitedCategoryId;
    }

    public void setAccountDebitedCategoryId(int accountDebitedCategoryId) {
        this.accountDebitedCategoryId = accountDebitedCategoryId;
    }

    @XmlElement(name = "accountCreditedCategoryId")
    public int getAccountCreditedCategoryId() {
        return accountCreditedCategoryId;
    }

    public void setAccountCreditedCategoryId(int accountCreditedCategoryId) {
        this.accountCreditedCategoryId = accountCreditedCategoryId;
    }

    @XmlElement(name = "groupId")
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @XmlElement(name = "contactId")
    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    @XmlElement(name = "rate")
    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @XmlElement(name = "rateDirection")
    public int getRateDirection() {
        return rateDirection;
    }

    public void setRateDirection(int rateDirection) {
        this.rateDirection = rateDirection;
    }

    @XmlElement(name = "invoiceNumber")
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @XmlElement(name = "guid")
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @XmlElement(name = "modified")
    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public Transaction toTransaction() {
        return new Transaction(id, amount, day, month, year, transactionTypeId, comment, checked, accountDebitedId,
                accountCreditedId, accountDebitedTypeId, accountCreditedTypeId, accountDebitedCategoryId,
                accountCreditedCategoryId, groupId, contactId, rate, rateDirection, invoiceNumber, guid, modified);
    }
}
