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
import org.panteleyev.money.MoneyApplication
import org.panteleyev.money.xml.Import
import org.panteleyev.persistence.DAO
import org.panteleyev.persistence.Record
import java.sql.Connection
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource
import kotlin.reflect.KClass

interface Named {
    val name : String
}

interface MoneyRecord : Record {
    override val id: Int
    val guid: String
    val modified: Long
}

internal enum class ImportAction {
    INSERT,
    UPDATE,
    IGNORE
}

internal typealias ImportMap = MutableMap<Int, Pair<Int, ImportAction>>

internal fun ImportMap.getMappedId(id: Int): Int = this[id]?.first ?: id

object MoneyDAO : DAO(null), RecordSource {
    const val FIELD_SCALE = 6

    private const val BATCH_SIZE = 1000

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

    fun preloadingProperty(): BooleanProperty {
        return preloadingProperty
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    fun categories(): ObservableMap<Int, Category> {
        return categories
    }

    override fun getCategory(id: Int): Category? {
        return categoriesMap[id]
    }

    fun insertCategory(category: Category): Category {
        val result = insert(category)
        categories.put(result!!.id, result)
        return result
    }

    fun updateCategory(category: Category): Category {
        val result = update(category)
        categories.put(result!!.id, result)
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

    override fun getCurrency(id: Int): Currency? {
        return if (id == 0) null else currencyMap[id]
    }

    fun insertCurrency(currency: Currency): Currency {
        val result = insert(currency)
        currencies.put(result!!.id, result)
        return result
    }

    fun updateCurrency(currency: Currency): Currency {
        val result = update(currency)
        currencies.put(result!!.id, result)
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

    override fun getContact(id: Int): Contact? {
        return if (id == 0) null else contactsMap[id]
    }

    fun insertContact(contact: Contact): Contact {
        val result = insert(contact)
        contacts.put(result!!.id, result)
        return result
    }

    fun updateContact(contact: Contact): Contact {
        val result = update(contact)
        contacts.put(result!!.id, result)
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

    override fun getAccount(id: Int): Account? {
        return if (id == 0) null else accountsMap[id]
    }

    fun insertAccount(account: Account): Account {
        val result = insert(account)
        accounts.put(result!!.id, result)
        return result
    }

    fun updateAccount(account: Account): Account {
        val result = update(account)
        accounts.put(result!!.id, result)
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

    override fun getTransactionGroup(id: Int): TransactionGroup? {
        return if (id == 0) null else transactionGroupsMap[id]
    }

    fun insertTransactionGroup(tg: TransactionGroup): TransactionGroup {
        val result = insert(tg)
        transactionGroups.put(result!!.id, result)
        return result
    }

    fun updateTransactionGroup(tg: TransactionGroup): TransactionGroup {
        val result = update(tg)
        transactionGroups.put(result!!.id, result)
        return result
    }

    fun deleteTransactionGroup(id: Int) {
        transactionGroups.remove(id)
        delete(id, TransactionGroup::class)
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

    override fun getTransaction(id: Int): Transaction? {
        return if (id == 0) null else transactionsMap[id]
    }

    fun insertTransaction(transaction: Transaction): Transaction {
        val result = insert(transaction)
        transactions.put(result!!.id, result)
        return result
    }

    fun updateTransaction(transaction: Transaction): Transaction {
        val result = update(transaction)
        transactions.put(result!!.id, result)
        return result
    }

    fun deleteTransaction(id: Int) {
        transactions.remove(id)
        delete(id, Transaction::class)
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

    fun dropTables() {
        super.dropTables(tableClasses.reversed())
    }

    fun preload(progress: (String) -> Unit = {}) {
        preloadingProperty.set(true)

        progress("Preloading primary keys... ")
        preload(tableClasses)
        progress(" done\n")

        progress("Preloading data...\n")

        progress("    categories... ")
        categoriesMap.clear()
        getAll(Category::class, categoriesMap)
        progress("done\n")

        progress("    contacts... ")
        contactsMap.clear()
        getAll(Contact::class, contactsMap)
        progress("done\n")

        progress("    currencies... ")
        currencyMap.clear()
        getAll(Currency::class, currencyMap)
        progress("done\n")

        progress("    accounts... ")
        accountsMap.clear()
        getAll(Account::class, accountsMap)
        progress("done\n")

        progress("    transaction groups... ")
        transactionGroupsMap.clear()
        getAll(TransactionGroup::class, transactionGroupsMap)
        progress("done\n")

        progress("    transactions... ")
        transactionsMap.clear()
        getAll(Transaction::class, transactionsMap)
        progress("done\n")

        progress("done\n")
        preloadingProperty.set(false)
    }

    private fun deleteAll(conn: Connection, tables: List<KClass<out Record>>) {
        tables.forEach {
            deleteAll(it, conn)
            resetPrimaryKey(it)
        }
    }

    fun importFullDump(imp: Import, progress: (String) -> Unit) {
        connection.use { conn ->
            progress("Truncating tables... ")
            deleteAll(conn, tableClasses.reversed())
            progress(" done\n")

            progress("Importing data...\n")

            progress("    categories... ")
            insert(conn, BATCH_SIZE, imp.categories)
            progress("done\n")

            progress("    currencies... ")
            insert(conn, BATCH_SIZE, imp.currencies)
            progress("done\n")

            progress("    accounts... ")
            insert(conn, BATCH_SIZE, imp.accounts)
            progress("done\n")

            progress("    contacts... ")
            insert(conn, BATCH_SIZE, imp.contacts)
            progress("done\n")

            progress("    transaction groups... ")
            insert(conn, BATCH_SIZE, imp.transactionGroups)
            progress("done\n")

            progress("    transactions... ")
            insert(conn, BATCH_SIZE, imp.transactions)
            progress("done\n")

            progress("done\n")
        }
    }

    fun importRecords(imp: Import, progress: (String) -> Unit) {

        fun Collection<MoneyRecord>.findByGuid(guid: String): MoneyRecord? {
            return this.find { it.guid == guid }
        }

        fun mapImportedIds(idMap: ImportMap, existing: Collection<MoneyRecord>, toImport: List<MoneyRecord>) {
            toImport.forEach {
                val found = existing.findByGuid(it.guid)
                if (found != null) {
                    if (it.modified > found.modified) {
                        idMap[it.id] = Pair(found.id, ImportAction.UPDATE)
                    } else {
                        idMap[it.id] = Pair(found.id, ImportAction.IGNORE)
                    }
                } else {
                    idMap[it.id] = Pair(generatePrimaryKey(it::class), ImportAction.INSERT)
                }
            }
        }

        fun importTable(conn: Connection, toImport: List<MoneyRecord>, idMap: ImportMap,
                        replacement: (MoneyRecord) -> MoneyRecord) {
            toImport.forEach {
                val action = idMap[it.id]!!.second

                if (action != ImportAction.IGNORE) {
                    val replaced = replacement(it)

                    if (action == ImportAction.INSERT) {
                        insert(conn, replaced)
                    } else {
                        update(conn, replaced)
                    }
                }
            }
        }

        val categoryIdMap: ImportMap = mutableMapOf()
        val currencyIdMap: ImportMap = mutableMapOf()
        val contactIdMap: ImportMap = mutableMapOf()
        val accountIdMap: ImportMap = mutableMapOf()
        val transactionGroupIdMap: ImportMap = mutableMapOf()
        val transactionIdMap: ImportMap = mutableMapOf()

        mapImportedIds(currencyIdMap, currencyMap.values, imp.currencies)
        mapImportedIds(categoryIdMap, categoriesMap.values, imp.categories)
        mapImportedIds(contactIdMap, contactsMap.values, imp.contacts)
        mapImportedIds(accountIdMap, accountsMap.values, imp.accounts)
        mapImportedIds(transactionGroupIdMap, transactionGroupsMap.values, imp.transactionGroups)
        mapImportedIds(transactionIdMap, transactionsMap.values, imp.transactions)

        dataSource!!.connection.use { conn ->
            conn.autoCommit = false

            try {
                importTable(conn, imp.categories, categoryIdMap, {
                    (it as Category).copy(id = categoryIdMap.getMappedId(it.id))
                })

                importTable(conn, imp.currencies, currencyIdMap, {
                    (it as Currency).copy(id = currencyIdMap.getMappedId(it.id))
                })

                importTable(conn, imp.contacts, contactIdMap, {
                    (it as Contact).copy(id = contactIdMap.getMappedId(it.id))
                })

                importTable(conn, imp.accounts, accountIdMap, {
                    (it as Account).copy(id = accountIdMap.getMappedId(it.id),
                            categoryId = categoryIdMap.getMappedId(it.categoryId))
                })

                importTable(conn, imp.transactionGroups, transactionGroupIdMap, {
                    (it as TransactionGroup).copy(id = transactionGroupIdMap.getMappedId(it.id))
                })

                importTable(conn, imp.transactions, transactionIdMap, {
                    (it as Transaction).copy(id = transactionIdMap.getMappedId(it.id),
                            accountDebitedId = accountIdMap.getMappedId(it.accountDebitedId),
                            accountCreditedId = accountIdMap.getMappedId(it.accountCreditedId),
                            accountDebitedCategoryId = categoryIdMap.getMappedId(it.accountDebitedCategoryId),
                            accountCreditedCategoryId = categoryIdMap.getMappedId(it.accountCreditedCategoryId),
                            contactId = contactIdMap.getMappedId(it.contactId),
                            groupId = transactionGroupIdMap.getMappedId(it.groupId))
                })

                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                MoneyApplication.uncaughtException(ex)
            }
        }
    }

    fun initialize(ds: DataSource?) {
        dataSource = ds

        preloadingProperty.set(true)
        categoriesMap.clear()
        contactsMap.clear()
        currencyMap.clear()
        accountsMap.clear()
        transactionGroupsMap.clear()
        transactionsMap.clear()
        preloadingProperty.set(false)
    }

    private val tableClasses: List<KClass<out Record>>
        get() = listOf(
                Category::class,
                Contact::class,
                Currency::class,
                Account::class,
                TransactionGroup::class,
                Transaction::class
        )

    val open: Boolean
        get() = dataSource != null
}
