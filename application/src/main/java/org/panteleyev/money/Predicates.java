package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
