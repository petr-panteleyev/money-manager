/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataCache {
    private static final DataCache INSTANCE = new DataCache();

    private final ObservableList<Icon> icons = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final ObservableList<Currency> currencies = FXCollections.observableArrayList();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    public static DataCache cache() {
        return INSTANCE;
    }

    public void clear() {
        icons.clear();
        categories.clear();
        contacts.clear();
        currencies.clear();
        accounts.clear();
        transactions.clear();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generic methods
    ////////////////////////////////////////////////////////////////////////////

    public static <T extends MoneyRecord> Optional<T> getRecord(Collection<T> collection, UUID uuid) {
        return collection.stream().filter(r -> r.uuid().equals(uuid)).findAny();
    }

    private static <T extends MoneyRecord> void updateRecord(List<T> list, T record) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uuid().equals(record.uuid())) {
                list.set(i, record);
                break;
            }
        }
    }

    private static <T extends MoneyRecord> void removeRecord(Collection<T> collection, UUID uuid) {
        for (var iterator = collection.iterator(); iterator.hasNext(); ) {
            var record = iterator.next();
            if (record.uuid().equals(uuid)) {
                iterator.remove();
                return;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Icons
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Icon> getIcon(UUID uuid) {
        return getRecord(icons, uuid);
    }

    public ObservableList<Icon> getIcons() {
        return icons;
    }

    public void add(Icon icon) {
        icons.add(icon);
    }

    public void update(Icon icon) {
        updateRecord(icons, icon);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Category> getCategory(UUID uuid) {
        return getRecord(categories, uuid);
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public List<Category> getCategoriesByType(CategoryType... types) {
        var typeList = List.of(types);

        return getCategories().stream()
            .filter(category -> typeList.contains(category.type()))
            .toList();
    }

    public List<Category> getCategoriesByType(EnumSet<CategoryType> types) {
        return getCategories().stream()
            .filter(category -> types.contains(category.type()))
            .toList();
    }

    public void add(Category category) {
        categories.add(category);
    }

    public void update(Category category) {
        updateRecord(categories, category);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Currency> getCurrency(UUID uuid) {
        return getRecord(currencies, uuid);
    }

    public ObservableList<Currency> getCurrencies() {
        return currencies;
    }

    public Optional<Currency> getDefaultCurrency() {
        return getCurrencies().stream().filter(Currency::def).findFirst();
    }

    public void add(Currency currency) {
        currencies.add(currency);
    }

    public void update(Currency currency) {
        updateRecord(currencies, currency);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Contact> getContact(UUID uuid) {
        return getRecord(contacts, uuid);
    }

    public ObservableList<Contact> getContacts() {
        return contacts;
    }

    public void add(Contact contact) {
        contacts.add(contact);
    }

    public void update(Contact contact) {
        updateRecord(contacts, contact);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Account> getAccount(UUID uuid) {
        return getRecord(accounts, uuid);
    }

    public ObservableList<Account> getAccounts() {
        return accounts;
    }

    public Stream<Account> getAccounts(Predicate<Account> filter) {
        return getAccounts().stream().filter(filter);
    }

    public List<Account> getAccountsByType(CategoryType type) {
        return getAccounts().stream()
            .filter(account -> account.type() == type)
            .toList();
    }

    public List<Account> getAccountsByCategory(UUID uuid) {
        return getAccounts().stream()
            .filter(account -> account.categoryUuid().equals(uuid))
            .toList();
    }

    public List<Account> getAccountsByCategoryId(UUID... ids) {
        var catIDs = List.of(ids);

        return getAccounts().stream()
            .filter(account -> catIDs.contains(account.categoryUuid()))
            .toList();
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return getAccounts().stream()
            .filter(Account::enabled)
            .filter(a -> Objects.equals(a.getAccountNumberNoSpaces(), accountNumber))
            .findFirst();
    }

    public void add(Account account) {
        accounts.add(account);
    }

    public void update(Account account) {
        updateRecord(accounts, account);
    }

    public void remove(Account account) {
        removeRecord(accounts, account.uuid());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public Optional<Transaction> getTransaction(UUID uuid) {
        return getRecord(transactions, uuid);
    }

    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public List<Transaction> getTransactions(Collection<Account> accounts) {
        var ids = accounts.stream()
            .map(Account::uuid)
            .toList();

        return getTransactions().stream()
            .filter(tr -> ids.contains(tr.accountDebitedUuid()) || ids.contains(tr.accountCreditedUuid()))
            .toList();
    }

    public List<Transaction> getTransactionDetails(Transaction parent) {
        return getTransactions().stream()
            .filter(t -> Objects.equals(t.parentUuid(), parent.uuid()))
            .toList();
    }

    public List<Transaction> getTransactions(int month, int year) {
        return getTransactions().stream()
            .filter(tr -> tr.month() == month && tr.year() == year)
            .toList();
    }

    public List<Transaction> getTransactions(Account account) {
        var uuid = account.uuid();
        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.accountDebitedUuid(), uuid)
                || Objects.equals(tr.accountCreditedUuid(), uuid))
            .toList();
    }

    public List<Transaction> getTransactionsByCategories(Collection<Category> categories) {
        var uuids = categories.stream()
            .map(Category::uuid)
            .toList();

        return getTransactions().stream()
            .filter(tr -> uuids.contains(tr.accountDebitedCategoryUuid())
                || uuids.contains(tr.accountCreditedCategoryUuid()))
            .toList();
    }

    public Set<String> getUniqueTransactionComments() {
        return getTransactions().stream()
            .map(Transaction::comment)
            .filter(c -> !c.isEmpty())
            .collect(Collectors.toSet());
    }

    public long getTransactionCount(Account account) {
        var uuid = account.uuid();

        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.accountDebitedUuid(), uuid)
                || Objects.equals(tr.accountCreditedUuid(), uuid))
            .count();
    }

    public Stream<Transaction> getTransactions(Predicate<Transaction> filter) {
        return getTransactions().stream().filter(filter);
    }

    public void add(Transaction transaction) {
        transactions.add(transaction);
    }

    public void update(Transaction transaction) {
        updateRecord(transactions, transaction);
    }

    public void remove(Transaction transaction) {
        removeRecord(transactions, transaction.uuid());
    }

    /**
     * Calculates balance of all transactions related to the specified account.
     *
     * @param total whether initial balance should be added to the result
     * @return account balance
     */
    public BigDecimal calculateBalance(Account account, boolean total, Predicate<Transaction> filter) {
        return getTransactions(account).stream()
            .filter(t -> t.parentUuid() == null)
            .filter(filter)
            .map(t -> {
                var amount = t.amount();
                if (Objects.equals(account.uuid(), t.accountCreditedUuid())) {
                    // handle conversion rate
                    var rate = t.rate();
                    if (rate.compareTo(BigDecimal.ZERO) != 0 && rate.compareTo(BigDecimal.ONE) != 0) {
                        amount = t.rateDirection() == 0 ?
                            amount.divide(rate, RoundingMode.HALF_UP) : amount.multiply(rate);
                    }
                } else {
                    amount = amount.negate();
                }
                return amount;
            })
            .reduce(total ? account.openingBalance().add(account.accountLimit()) : BigDecimal.ZERO,
                BigDecimal::add);
    }

    public BigDecimal calculateBalance(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.parentUuid() == null)
            .map(t -> {
                var amount = t.amount();
                // handle conversion rate
                var rate = t.rate();
                if (rate.compareTo(BigDecimal.ZERO) != 0 && rate.compareTo(BigDecimal.ONE) != 0) {
                    amount = t.rateDirection() == 0 ?
                        amount.divide(rate, RoundingMode.HALF_UP) : amount.multiply(rate);
                }
                return amount;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
