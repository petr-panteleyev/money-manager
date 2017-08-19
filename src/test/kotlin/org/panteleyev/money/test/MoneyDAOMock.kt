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
import org.panteleyev.money.persistence.RecordSource
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup

internal class MoneyDAOMock(
        categories: List<Category>,
        accounts: List<Account>,
        contacts: List<Contact>,
        currencies: List<Currency>,
        transactionGroups: List<TransactionGroup>,
        transactions: List<Transaction>
) : RecordSource {
    val categories = categories.associate { Pair(it.id, it) }
    val accounts = accounts.associate { Pair(it.id, it) }
    val contacts = contacts.associate { Pair(it.id, it) }
    val currencies = currencies.associate { Pair(it.id, it) }
    val transactionGroups = transactionGroups.associate { Pair(it.id, it) }
    val transactions = transactions.associate { Pair(it.id, it) }

    override fun getCategory(id: Int): Category? {
        return categories[id]
    }

    override fun getCurrency(id: Int): Currency? {
        return currencies[id]
    }

    override fun getContact(id: Int): Contact? {
        return contacts[id]
    }

    override fun getAccount(id: Int): Account? {
        return accounts[id]
    }

    override fun getTransactionGroup(id: Int): TransactionGroup? {
        return transactionGroups[id]
    }

    override fun getTransaction(id: Int): Transaction? {
        return transactions[id]
    }
}
