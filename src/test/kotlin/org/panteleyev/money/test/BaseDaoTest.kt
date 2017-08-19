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

package org.panteleyev.money.test

import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.MySQLBuilder
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import org.testng.SkipException
import javax.sql.DataSource

open class BaseDaoTest : BaseTest() {

    private var dataSource: DataSource? = null
    private var dbName: String? = null

    @Throws(Exception::class)
    open fun setupAndSkip() {
        dbName = System.getProperty("mysql.database", TEST_DB_NAME)
        val host = System.getProperty("mysql.host", "localhost")
        val user = System.getProperty("mysql.user")
        val password = System.getProperty("mysql.password")

        if (user == null || password == null) {
            throw SkipException("Test config is not set")
        }

        dataSource = MySQLBuilder()
                .host(host)
                .user(user)
                .password(password)
                .name(dbName!!)
                .build()

        MoneyDAO.initialize(dataSource)
    }

    @Throws(Exception::class)
    open fun cleanup() {
    }

    @Throws(Exception::class)
    internal fun initializeEmptyMoneyFile() {
        MoneyDAO.createTables()
        MoneyDAO.preload()
    }

    fun newCategoryId(): Int {
        return MoneyDAO.generatePrimaryKey(Category::class)
    }

    fun newAccountId(): Int {
        return MoneyDAO.generatePrimaryKey(Account::class)
    }

    fun newCurrencyId(): Int {
        return MoneyDAO.generatePrimaryKey(Currency::class)
    }

    fun newContactId(): Int {
        return MoneyDAO.generatePrimaryKey(Contact::class)
    }

    fun newTransactionGroupId(): Int {
        return MoneyDAO.generatePrimaryKey(TransactionGroup::class)
    }

    fun newTransactionId(): Int {
        return MoneyDAO.generatePrimaryKey(Transaction::class)
    }

    companion object {
        val TEST_DB_NAME = "TestDB"
    }
}
