/*
 Copyright © 2020-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class Predicates {
    // Account
    public static Predicate<Account> activeAccount(boolean active) {
        return account -> account.enabled() == active;
    }

    public static Predicate<Account> accountByUuid(UUID uuid) {
        return account -> Objects.equals(account.uuid(), uuid);
    }

    public static Predicate<Account> accountByName(String name) {
        return name.isBlank() ?
                _ -> true : account -> account.name().toLowerCase().contains(name.toLowerCase());
    }

    public static Predicate<Account> accountByCategory(UUID uuid) {
        return account -> Objects.equals(account.categoryUuid(), uuid);
    }

    public static Predicate<Account> accountByCategoryType(CategoryType type) {
        return account -> account.type() == type;
    }

    public static Predicate<Account> accountByCategoryType(Collection<CategoryType> types) {
        return account -> types.contains(account.type());
    }

    // Contact
    public static Predicate<Contact> contactByType(ContactType type) {
        return contact -> contact.type() == type;
    }

    public static Predicate<Contact> contactByName(String name) {
        return name.isBlank() ?
                _ -> true : contact -> contact.name().toLowerCase().contains(name.toLowerCase());
    }

    public static Predicate<Card> activeCard(boolean active) {
        return card -> card.enabled() == active;
    }

    public static Predicate<Card> cardByNumber(String number) {
        return number.isBlank() ?
                _ -> true : card -> card.number().toLowerCase().contains(number.toLowerCase());
    }

    private Predicates() {
    }
}
