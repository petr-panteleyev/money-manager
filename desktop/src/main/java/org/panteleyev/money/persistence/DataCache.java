/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
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
    private static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DATE =
            Comparator.comparing(Transaction::year)
                    .thenComparing(Transaction::month)
                    .thenComparing(Transaction::day)
                    .thenComparingLong(Transaction::created);

    private static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DAY =
            Comparator.comparingInt(Transaction::day).thenComparingLong(Transaction::created);

    private static final Comparator<Category> COMPARE_CATEGORY_BY_NAME = Comparator.comparing(Category::name);
    private static final Comparator<Category> COMPARE_CATEGORY_BY_TYPE =
            (o1, o2) -> o1.type().toString().compareToIgnoreCase(o2.type().toString());

    private final static Comparator<Account> COMPARE_ACCOUNT_BY_NAME = Comparator.comparing(Account::name);

    private final ObservableList<Icon> icons = FXCollections.observableArrayList();
    private final ObservableList<MoneyDocument> documents = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final ObservableList<Currency> currencies = FXCollections.observableArrayList();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<PeriodicPayment> periodicPayments = FXCollections.observableArrayList();
    private final ObservableList<ExchangeSecurity> exchangeSecurities = FXCollections.observableArrayList();

    public void clear() {
        icons.clear();
        documents.clear();
        categories.clear();
        contacts.clear();
        currencies.clear();
        accounts.clear();
        transactions.clear();
        periodicPayments.clear();
        exchangeSecurities.clear();
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
    // Documents
    ////////////////////////////////////////////////////////////////////////////

    public Optional<MoneyDocument> getDocument(UUID uuid) {
        return getRecord(documents, uuid);
    }

    public ObservableList<MoneyDocument> getDocuments() {
        return documents;
    }

    public void add(MoneyDocument document) {
        documents.add(document);
    }

    public void update(MoneyDocument document) {
        updateRecord(documents, document);
    }

    public long getDocumentCount(MoneyRecord owner) {
        return documents.stream()
                .filter(doc -> doc.ownerUuid().equals(owner.uuid()))
                .count();
    }

    public Set<String> getUniqueDocumentDescriptions() {
        return getDocuments().stream()
                .map(MoneyDocument::description)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());
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

    public Comparator<Category> getCategoryByNameComparator() {
        return COMPARE_CATEGORY_BY_NAME;
    }

    public Comparator<Category> getCategoryByTypeComparator() {
        return COMPARE_CATEGORY_BY_TYPE;
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

    public void remove(Currency currency) {
        removeRecord(currencies, currency.uuid());
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

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return getAccounts().stream()
                .filter(Account::enabled)
                .filter(a -> Objects.equals(Account.getAccountNumberNoSpaces(a), accountNumber))
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

    public Comparator<Account> getAccountByNameComparator() {
        return COMPARE_ACCOUNT_BY_NAME;
    }

    public Comparator<Account> getAccountByCategoryComparator() {
        return (a1, a2) -> {
            var c1 = getCategory(a1.categoryUuid()).map(Category::name).orElse("");
            var c2 = getCategory(a2.categoryUuid()).map(Category::name).orElse("");
            return c1.compareTo(c2);
        };
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

    public Comparator<Transaction> getTransactionByDateComparator() {
        return COMPARE_TRANSACTION_BY_DATE;
    }

    public Comparator<Transaction> getTransactionByDayComparator() {
        return COMPARE_TRANSACTION_BY_DAY;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Periodic Payments
    ////////////////////////////////////////////////////////////////////////////

    public Optional<PeriodicPayment> getPeriodicPayment(UUID uuid) {
        return getRecord(periodicPayments, uuid);
    }

    public ObservableList<PeriodicPayment> getPeriodicPayments() {
        return periodicPayments;
    }

    public void add(PeriodicPayment periodicPayment) {
        periodicPayments.add(periodicPayment);
    }

    public void update(PeriodicPayment periodicPayment) {
        updateRecord(periodicPayments, periodicPayment);
    }

    public void remove(PeriodicPayment periodicPayment) {
        removeRecord(periodicPayments, periodicPayment.uuid());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Exchange Securities
    ////////////////////////////////////////////////////////////////////////////

    public Optional<ExchangeSecurity> getExchangeSecurity(UUID uuid) {
        return getRecord(exchangeSecurities, uuid);
    }

    public ObservableList<ExchangeSecurity> getExchangeSecurities() {
        return exchangeSecurities;
    }

    public void add(ExchangeSecurity exchangeSecurity) {
        exchangeSecurities.add(exchangeSecurity);
    }

    public void update(ExchangeSecurity exchangeSecurity) {
        updateRecord(exchangeSecurities, exchangeSecurity);
    }

    public void remove(ExchangeSecurity exchangeSecurity) {
        removeRecord(exchangeSecurities, exchangeSecurity.uuid());
    }


    /**
     * Calculates balance of all transactions related to the specified account.
     *
     * @param total whether initial balance should be added to the result
     * @return account balance
     */
    public BigDecimal calculateBalance(Account account, boolean total, Predicate<Transaction> filter) {
        var initialBalance = total ?
                account.openingBalance().add(account.accountLimit()) :
                BigDecimal.ZERO;

        return getTransactions(account).stream()
                .filter(filter)
                .filter(t -> t.parentUuid() == null)
                .map(t -> Objects.equals(account.uuid(), t.accountCreditedUuid()) ?
                        Transaction.getConvertedAmount(t) :
                        Transaction.getNegatedAmount(t))
                .reduce(initialBalance, BigDecimal::add);
    }

    public static BigDecimal calculateBalance(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.parentUuid() == null)
                .map(Transaction::getConvertedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
