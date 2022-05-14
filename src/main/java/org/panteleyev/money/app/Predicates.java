/*
 Copyright (c) 2017-2022, Petr Panteleyev

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
        return account -> account.enabled() == active;
    }

    static Predicate<Account> accountByUuid(UUID uuid) {
        return account -> Objects.equals(account.uuid(), uuid);
    }

    static Predicate<Account> accountByName(String name) {
        return name.isBlank() ?
            account -> true : account -> account.name().toLowerCase().contains(name.toLowerCase());
    }

    static Predicate<Account> accountByCategory(UUID uuid) {
        return account -> Objects.equals(account.categoryUuid(), uuid);
    }

    static Predicate<Account> accountByCategoryType(CategoryType type) {
        return account -> account.type() == type;
    }

    static Predicate<Account> accountByCategoryType(Collection<CategoryType> types) {
        return account -> types.contains(account.type());
    }

    // Contact
    static Predicate<Contact> contactByType(ContactType type) {
        return contact -> contact.type() == type;
    }

    static Predicate<Contact> contactByName(String name) {
        return name.isBlank() ?
            contact -> true : contact -> contact.name().toLowerCase().contains(name.toLowerCase());
    }
}
