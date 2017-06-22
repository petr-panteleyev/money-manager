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

package org.panteleyev.money.persistence

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import org.panteleyev.persistence.DAO
import org.panteleyev.persistence.Record
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

object MoneyDAO : DAO() {
    private val categoriesMap = ConcurrentHashMap<Int, Category>()
    private val categories = FXCollections.observableMap(categoriesMap)

    private val contactsMap = ConcurrentHashMap<Int, Contact>()
    private val contacts = FXCollections.observableMap(contactsMap)

    private val currencyMap = ConcurrentHashMap<Int, Currency>()
    private val currencies = FXCollections.observableMap(currencyMap)

    private val accountsMap = ConcurrentHashMap<Int, Account>()
    private val accounts = FXCollections.observableMap(accountsMap)

    private val transactionGroupsMap = ConcurrentHashMap<Int, TransactionGroup>()
    private val transactionGroups = FXCollections.observableMap(transactionGroupsMap)

    private val transactionsMap = ConcurrentHashMap<Int, Transaction>()
    private val transactions = FXCollections.observableMap(transactionsMap)

    private val preloadingProperty = SimpleBooleanProperty(false)

    override fun setDataSource(ds: DataSource) {
        super.setDataSource(ds)

        preloadingProperty.set(true)
        categoriesMap.clear()
        contactsMap.clear()
        currencyMap.clear()
        accountsMap.clear()
        transactionGroupsMap.clear()
        transactionsMap.clear()
        preloadingProperty.set(false)
    }

    fun preloadingProperty(): BooleanProperty {
        return preloadingProperty
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    fun categories(): ObservableMap<Int, Category> {
        return categories
    }

    fun getCategory(id: Int): Category? {
        return categoriesMap[id]
    }

    fun insertCategory(category: Category): Category {
        val result = insert(category)
        categories.put(result.id, result)
        return result
    }

    fun updateCategory(category: Category): Category {
        val result = update(category)
        categories.put(result.id, result)
        return result
    }

    fun getCategories(): Collection<Category> {
        return categoriesMap.values
    }

    fun getCategoriesByType(vararg types: CategoryType): List<Category> {
        val typeList = Arrays.asList(*types)

        return getCategories().filter { typeList.contains(it.type) }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    fun currencies(): ObservableMap<Int, Currency> {
        return currencies
    }

    fun getCurrency(id: Int): Currency? {
        return if (id == 0) null else currencyMap[id]
    }

    fun insertCurrency(currency: Currency): Currency {
        val result = insert(currency)
        currencies.put(result.id, result)
        return result
    }

    fun updateCurrency(currency: Currency): Currency {
        val result = update(currency)
        currencies.put(result.id, result)
        return result
    }

    fun getCurrencies(): Collection<Currency> {
        return currencyMap.values
    }

    val defaultCurrency: Currency?
        get() = getCurrencies().find { it.def }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    fun contacts(): ObservableMap<Int, Contact> {
        return contacts
    }

    fun getContact(id: Int): Contact? {
        return if (id == 0) null else contactsMap[id]
    }

    fun insertContact(contact: Contact): Contact {
        val result = insert(contact)
        contacts.put(result.id, result)
        return result
    }

    fun updateContact(contact: Contact): Contact {
        val result = update(contact)
        contacts.put(result.id, result)
        return result
    }

    fun getContacts(): Collection<Contact> {
        return contactsMap.values
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    fun accounts(): ObservableMap<Int, Account> {
        return accounts
    }

    fun getAccount(id: Int): Account? {
        return if (id == 0) null else accountsMap[id]
    }

    fun insertAccount(account: Account): Account {
        val result = insert(account)
        accounts.put(result.id, result)
        return result
    }

    fun updateAccount(account: Account): Account {
        val result = update(account)
        accounts.put(result.id, result)
        return result
    }

    fun deleteAccount(account: Account) {
        accounts.remove(account.id)
        delete(account)
    }

    fun getAccounts(): Collection<Account> {
        return accountsMap.values
    }

    fun getAccountsByType(type: CategoryType): List<Account> {
        return accountsMap.values.filter { it.type == type }
    }

    fun getAccountsByCategory(id: Int): List<Account> {
        return accountsMap.values.filter { it.categoryId == id }
    }

    fun getAccountsByCategoryId(vararg ids: Int): List<Account> {
        return accountsMap.values.filter { ids.contains(it.categoryId )}
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transaction Groups
    ////////////////////////////////////////////////////////////////////////////

    fun transactionGroups(): ObservableMap<Int, TransactionGroup> {
        return transactionGroups
    }

    fun getTransactionGroup(id: Int): TransactionGroup? {
        return if (id == 0) null else transactionGroupsMap[id]
    }

    fun insertTransactionGroup(tg: TransactionGroup): TransactionGroup {
        val result = insert(tg)
        transactionGroups.put(result.id, result)
        return result
    }

    fun updateTransactionGroup(tg: TransactionGroup): TransactionGroup {
        val result = update(tg)
        transactionGroups.put(result.id, result)
        return result
    }

    fun deleteTransactionGroup(id: Int) {
        transactionGroups.remove(id)
        delete(id, TransactionGroup::class.java)
    }

    fun getTransactionGroups(): Collection<TransactionGroup> {
        return transactionGroupsMap.values
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    fun transactions(): ObservableMap<Int, Transaction> {
        return transactions
    }

    fun getTransaction(id: Int): Transaction? {
        return if (id == 0) null else transactionsMap[id]
    }

    fun insertTransaction(transaction: Transaction): Transaction {
        val result = insert(transaction)
        transactions.put(result.id, result)
        return result
    }

    fun updateTransaction(transaction: Transaction): Transaction {
        val result = update(transaction)
        transactions.put(result.id, result)
        return result
    }

    fun deleteTransaction(id: Int) {
        transactions.remove(id)
        delete(id, Transaction::class.java)
    }

    fun getTransactions(): Collection<Transaction> {
        return transactions.values
    }

    fun getTransactions(accounts: Collection<Account>): List<Transaction> {
        val ids = accounts.map { it.id }
        return getTransactions().filter { ids.contains(it.accountDebitedId) || ids.contains(it.accountCreditedId) }
    }

    fun getTransactions(month: Int, year: Int): List<Transaction> {
        return getTransactions().filter { it.month == month && it.year == year }
    }

    fun getTransactions(account: Account): List<Transaction> {
        val id = account.id
        return getTransactions().filter { it.accountDebitedId == id || it.accountCreditedId == id }
    }

    fun getTransactionsByCategories(categories: Collection<Category>): List<Transaction> {
        val ids = categories.map { it.id }

        return getTransactions()
                .filter { ids.contains(it.accountDebitedCategoryId) || ids.contains(it.accountCreditedCategoryId) }
    }

    val uniqueTransactionComments: Set<String>
        get() = getTransactions()
                .map { it.comment }
                .filter { !it.isEmpty() }
                .distinct()
                .toSet()

    fun getTransactionCount(account: Account): Int {
        val id = account.id

        return getTransactions().count { it.accountDebitedId == id || it.accountCreditedId == id }
    }

    fun createTables() {
        super.createTables(tableClasses)
    }

    fun preload() {
        preloadingProperty.set(true)

        preload(tableClasses)

        categoriesMap.clear()
        getAll(Category::class.java, categoriesMap)

        contactsMap.clear()
        getAll(Contact::class.java, contactsMap)

        currencyMap.clear()
        getAll(Currency::class.java, currencyMap)

        accountsMap.clear()
        getAll(Account::class.java, accountsMap)

        transactionGroupsMap.clear()
        getAll(TransactionGroup::class.java, transactionGroupsMap)

        transactionsMap.clear()
        getAll(Transaction::class.java, transactionsMap)

        preloadingProperty.set(false)
    }

    fun initialize(ds: DataSource?) {
        dataSource = ds
    }

    private val tableClasses: List<Class<out Record>>
        get() = Arrays.asList(
                Category::class.java,
                Contact::class.java,
                Currency::class.java,
                Account::class.java,
                TransactionGroup::class.java,
                Transaction::class.java
        )

    val open: Boolean
        get() = dataSource != null
}
