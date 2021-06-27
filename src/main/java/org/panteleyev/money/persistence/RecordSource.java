/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents record storage capable to retrieve record by its id. Main purpose is to mock
 * MoneyDAO in various tests.
 */
public interface RecordSource {
    Optional<Icon> getIcon(UUID uuid);
    Optional<Category> getCategory(UUID uuid);
    Optional<Currency> getCurrency(UUID uuid);
    Optional<Contact> getContact(UUID uuid);
    Optional<Account> getAccount(UUID uuid);
    Optional<Transaction> getTransaction(UUID uuid);
    List<Transaction> getTransactionDetails(Transaction parent);
}
