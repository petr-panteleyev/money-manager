/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence.dto;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionGroup;
import org.panteleyev.persistence.Record;

public interface Dto<T> extends Record {
    int BINARY_LENGTH = 4096;

    T decrypt(String password);

    byte[] toJson(T object);

    static Dto newDto(Object model, String password) {
        if (model instanceof Account) {
            return new AccountDto((Account) model, password);
        } else if (model instanceof Category) {
            return new CategoryDto((Category) model, password);
        } else if (model instanceof Contact) {
            return new ContactDto((Contact) model, password);
        } else if (model instanceof Currency) {
            return new CurrencyDto((Currency) model, password);
        } else if (model instanceof Transaction) {
            return new TransactionDto((Transaction) model, password);
        } else if (model instanceof TransactionGroup) {
            return new TransactionGroupDto((TransactionGroup) model, password);
        } else {
            throw new IllegalArgumentException("Unknown record type");
        }
    }

    static Class<? extends Dto> dtoClass(Record model) {
        if (model instanceof Account) {
            return AccountDto.class;
        } else if (model instanceof Category) {
            return CategoryDto.class;
        } else if (model instanceof Contact) {
            return ContactDto.class;
        } else if (model instanceof Currency) {
            return CurrencyDto.class;
        } else if (model instanceof Transaction) {
            return TransactionDto.class;
        } else if (model instanceof TransactionGroup) {
            return TransactionGroupDto.class;
        } else {
            throw new IllegalArgumentException("Unknown record type");
        }
    }

    static Class<? extends Dto> dtoClass(Class modelClass) {
        if (Account.class.equals(modelClass)) {
            return AccountDto.class;
        } else if (Category.class.equals(modelClass)) {
            return CategoryDto.class;
        } else if (Contact.class.equals(modelClass)) {
            return ContactDto.class;
        } else if (Currency.class.equals(modelClass)) {
            return CurrencyDto.class;
        } else if (Transaction.class.equals(modelClass)) {
            return TransactionDto.class;
        } else if (TransactionGroup.class.equals(modelClass)) {
            return TransactionGroupDto.class;
        } else {
            throw new IllegalArgumentException("Unknown record class");
        }
    }
}
