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

package org.panteleyev.money.xml

import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.RecordSource
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import java.io.InputStream
import java.io.OutputStream
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.validation.SchemaFactory

@XmlRootElement(name = "Money")
class Export(private val source: RecordSource = MoneyDAO) {

    @get:XmlElementWrapper(name = "Categories")
    @get:XmlElement(name = "Category")
    var categories: List<CategoryXml> = mutableListOf()

    @get:XmlElementWrapper(name = "Accounts")
    @get:XmlElement(name = "Account")
    var accounts: List<AccountXml> = mutableListOf()

    @get:XmlElementWrapper(name = "Contacts")
    @get:XmlElement(name = "Contact")
    var contacts: List<ContactXml> = mutableListOf()

    @get:XmlElementWrapper(name = "Currencies")
    @get:XmlElement(name = "Currency")
    var currencies: List<CurrencyXml> = mutableListOf()

    @get:XmlElementWrapper(name = "TransactionGroups")
    @get:XmlElement(name = "TransactionGroup")
    var transactionGroups: List<TransactionGroupXml> = mutableListOf()

    @get:XmlElementWrapper(name = "Transactions")
    @get:XmlElement(name = "Transaction")
    var transactions: List<TransactionXml> = mutableListOf()


    fun withCategories(catList: Collection<Category>): Export {
        categories = catList.map { CategoryXml(it) }
        return this
    }

    fun withAccounts(accList: Collection<Account>, withDeps: Boolean = false): Export {
        accounts = accList.map { AccountXml(it) }

        if (withDeps) {
            categories = accounts
                    .distinctBy { it.categoryId }
                    .mapNotNull { source.getCategory(it.categoryId) }
                    .map { CategoryXml(it) }

            currencies = accounts
                    .distinctBy { it.currencyId }
                    .mapNotNull { source.getCurrency(it.currencyId) }
                    .map { CurrencyXml(it) }
        }

        return this
    }

    fun withContacts(contactList: Collection<Contact>): Export {
        contacts = contactList.map { ContactXml(it) }
        return this
    }

    fun withCurrencies(currList: Collection<Currency>): Export {
        currencies = currList.map { CurrencyXml(it) }
        return this
    }

    fun withTransactionGroups(tgList: Collection<TransactionGroup>): Export {
        transactionGroups = tgList.map { TransactionGroupXml(it) }
        return this
    }

    fun withTransactions(tList: Collection<Transaction>, withDeps: Boolean = false): Export {
        transactions = tList.map { TransactionXml(it) }

        if (withDeps) {
            transactionGroups = tList
                    .distinctBy { it.groupId }
                    .mapNotNull { source.getTransactionGroup(it.groupId) }
                    .map { TransactionGroupXml(it) }

            contacts = tList
                    .distinctBy { it.contactId }
                    .mapNotNull { source.getContact(it.contactId) }
                    .map { ContactXml(it) }

            val accIdList = mutableSetOf<Int>()
            tList.forEach {
                accIdList.add(it.accountDebitedId)
                accIdList.add(it.accountCreditedId)
            }

            withAccounts(accIdList.mapNotNull { source.getAccount(it) }, true)
        }

        return this
    }

    fun export(out: OutputStream) {
        val ctx = JAXBContext.newInstance(
                Export::class.java,
                CategoryXml::class.java,
                AccountXml::class.java,
                ContactXml::class.java,
                CurrencyXml::class.java,
                TransactionGroupXml::class.java,
                Transaction::class.java
        )
        val marshaller = ctx.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(this, out)
    }
}

class Import {
    var categories: List<Category> = listOf()
    var accounts: List<Account> = listOf()
    var contacts: List<Contact> = listOf()
    var currencies: List<Currency> = listOf()
    var transactionGroups: List<TransactionGroup> = listOf()
    var transactions: List<Transaction> = mutableListOf()

    companion object {
        private val moneySchema by lazy {
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            schemaFactory.newSchema(this::class.java.getResource("/org/panteleyev/money/xml/money.xsd"))
        }

        fun import(inStream: InputStream): Import {
            val ctx = JAXBContext.newInstance(
                    Export::class.java,
                    AccountXml::class.java)
            val unmarshaller = ctx.createUnmarshaller().apply {
                schema = moneySchema
            }

            val export = unmarshaller.unmarshal(inStream) as Export

            return Import().apply {
                categories = export.categories.map { it.toCategory() }
                accounts = export.accounts.map { it.toAccount() }
                contacts = export.contacts.map { it.toContact() }
                currencies = export.currencies.map { it.toCurrency() }
                transactionGroups = export.transactionGroups.map { it.toTransactionGroup() }
                transactions = export.transactions.map { it.toTransaction() }
            }
        }
    }
}