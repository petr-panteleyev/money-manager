/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public interface Predicates {
    // Account
    static Predicate<Account> activeAccount(boolean active) {
        return account -> account.getEnabled() == active;
    }

    static Predicate<Account> accountByUuid(UUID uuid) {
        return account -> Objects.equals(account.getUuid(), uuid);
    }

    static Predicate<Account> accountByName(String name) {
        return name.isBlank() ?
            account -> true : account -> account.getName().toLowerCase().contains(name.toLowerCase());
    }

    static Predicate<Account> accountByCategory(UUID uuid) {
        return account -> Objects.equals(account.getCategoryUuid(), uuid);
    }

    static Predicate<Account> accountByCategoryType(CategoryType type) {
        return account -> account.getType() == type;
    }

    static Predicate<Account> accountByCategoryType(Collection<CategoryType> types) {
        return account -> types.contains(account.getType());
    }

    // Contact
    static Predicate<Contact> contactByType(ContactType type) {
        return contact -> contact.getType() == type;
    }

    static Predicate<Contact> contactByName(String name) {
        return name.isBlank() ?
            contact -> true : contact -> contact.getName().toLowerCase().contains(name.toLowerCase());
    }
}
