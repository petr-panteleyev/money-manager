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

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import org.panteleyev.money.TransactionEditorPane
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionType
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.util.Calendar
import java.util.Random
import java.util.UUID

class TestEditorPane : BaseDaoTest() {
    private val curr_1 by lazy {
        Currency(newCurrencyId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                1, false, false, BigDecimal(RANDOM.nextDouble()), 1, false
        )
    }

    private val curr_2 by lazy {
        Currency(newCurrencyId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                1, false, false, BigDecimal(RANDOM.nextDouble()), 1, false
        )
    }

    private val category by lazy {
        Category(newCategoryId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                CategoryType.BANKS_AND_CASH.id,
                false
        )
    }

    private val contact by lazy {
        Contact(_id = newContactId(), name = UUID.randomUUID().toString())
    }

    private val acc_1: Account by lazy {
        Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.id,
                category.id,
                curr_1.id,
                true
        )
    }

    // different currency
    private val acc_2: Account by lazy {
        Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.id,
                category.id,
                curr_2.id,
                true
        )
    }

    // same currency as (1)
    private val acc_3: Account by lazy {
        Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.id,
                category.id,
                curr_1.id,
                true
        )
    }

    private var resultedBuilder: Transaction.Builder? = null
    private var resultedId: Int = 0

    @BeforeClass
    @Throws(Exception::class)
    override fun setupAndSkip() {
        try {
            super.setupAndSkip()
            MoneyDAO.createTables()
            initializeEmptyMoneyFile()
            createData()
            JFXPanel()         // required to initialize FX toolkit
        } catch (ex: Exception) {
            throw SkipException("Database not configured")
        }
    }

    @AfterClass
    @Throws(Exception::class)
    override fun cleanup() {
        super.cleanup()
    }

    private fun createData() {
        with (MoneyDAO) {
            insertCurrency(curr_1)
            insertCurrency(curr_2)
            insertCategory(category)
            insertContact(contact)
            insertAccount(acc_1)
            insertAccount(acc_2)
            insertAccount(acc_3)
        }
    }

    private fun <T> getControl(pane: TransactionEditorPane, name: String): T {
        try {
            val f = pane.javaClass.getDeclaredField(name)
            f.isAccessible = true
            return f.get(pane) as T
        } catch (ex: IllegalAccessException) {
            throw RuntimeException(ex)
        } catch (ex: IllegalArgumentException) {
            throw RuntimeException(ex)
        } catch (ex: NoSuchFieldException) {
            throw RuntimeException(ex)
        } catch (ex: SecurityException) {
            throw RuntimeException(ex)
        }
    }

    private fun getTextField(pane: TransactionEditorPane, name: String): TextField {
        return getControl(pane, name)
    }

    private fun getCheckBox(pane: TransactionEditorPane, name: String): CheckBox {
        return getControl(pane, name)
    }

    private fun createEditorPane(): TransactionEditorPane {
        val pane = TransactionEditorPane()
        pane.initControls()
        callPrivateMethod(pane, "onChangedTransactionTypes")
        callPrivateMethod(pane, "setupContactMenu")
        callPrivateMethod(pane, "setupAccountMenus")
        callPrivateMethod(pane, "setupComments")
        return pane
    }

    private fun setUserInput(pane: TransactionEditorPane, t: Transaction, contactName: String?) {
        // Transaction type
        getTextField(pane, "typeEdit").text = t.transactionType.typeName

        // Debited account
        getTextField(pane, "debitedAccountEdit").text = MoneyDAO.getAccount(t.accountDebitedId)!!.name

        // Credited account
        getTextField(pane, "creditedAccountEdit").text = MoneyDAO.getAccount(t.accountCreditedId)!!.name

        getTextField(pane, "commentEdit").text = t.comment
        getTextField(pane, "sumEdit").text = t.amount.toString()
        getCheckBox(pane, "checkedCheckBox").isSelected = t.checked

        if (t.contactId != 0) {
            val cntct = MoneyDAO.getContact(t.contactId)
            getTextField(pane, "contactEdit").text = cntct!!.name
        }
        if (contactName != null) {
            getTextField(pane, "contactEdit").text = contactName
        }
    }

    private fun callPrivateMethod(pane: Any, name: String) {
        try {
            val method = pane.javaClass.getDeclaredMethod(name)
            method.isAccessible = true
            method.invoke(pane)
        } catch (ex: IllegalAccessException) {
            throw RuntimeException(ex)
        } catch (ex: IllegalArgumentException) {
            throw RuntimeException(ex)
        } catch (ex: NoSuchMethodException) {
            throw RuntimeException(ex)
        } catch (ex: SecurityException) {
            throw RuntimeException(ex)
        } catch (ex: InvocationTargetException) {
            throw RuntimeException(ex)
        }

    }

    private fun pressButton(pane: Any, name: String) {
        try {
            val f = pane.javaClass.getDeclaredField(name)
            f.isAccessible = true
            val button = f.get(pane) as Button
            button.fireEvent(ActionEvent())
        } catch (ex: IllegalAccessException) {
            throw RuntimeException(ex)
        } catch (ex: IllegalArgumentException) {
            throw RuntimeException(ex)
        } catch (ex: NoSuchFieldException) {
            throw RuntimeException(ex)
        } catch (ex: SecurityException) {
            throw RuntimeException(ex)
        }

    }

    private fun pressAddButton(pane: Any) {
        pressButton(pane, "addButton")
    }

    private fun pressUpdateButton(pane: Any) {
        pressButton(pane, "updateButton")
    }

    private fun pressDeleteButton(pane: Any) {
        pressButton(pane, "deleteButton")
    }

    private fun createTestTransaction(debit: Account, credit: Account, contact: Contact?): Transaction {
        val builder = Transaction.Builder()
                .id(RANDOM.nextInt())
                .transactionType(TransactionType.CARD_PAYMENT)
                .accountCreditedType(category.type)
                .accountDebitedType(category.type)
                .accountCreditedCategoryId(category.id)
                .accountDebitedCategoryId(category.id)
                .accountDebitedId(debit.id)
                .accountCreditedId(credit.id)
                .day(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .comment(UUID.randomUUID().toString())
                .amount(BigDecimal(RANDOM.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
                .checked(RANDOM.nextBoolean())

        if (debit.currencyId == credit.currencyId) {
            builder.rate(BigDecimal.ONE)
        } else {
            builder.rate(BigDecimal(RANDOM.nextDouble()))
        }

        if (contact != null) {
            builder.contactId(contact.id)
        }

        return builder.build()
    }

    private fun assertMainFields(r: Transaction, t: Transaction) {
        Assert.assertEquals(r.transactionType, t.transactionType, "Transaction type ID is invalid")

        // Debited account
        Assert.assertEquals(r.accountDebitedId, t.accountDebitedId, "Debited account ID is invalid")
        Assert.assertEquals(r.accountDebitedCategoryId, t.accountDebitedCategoryId, "Debited account category ID is invalid")
        Assert.assertEquals(r.accountDebitedType, t.accountDebitedType, "Debited account category type ID is invalid")

        // Credited account
        Assert.assertEquals(r.accountCreditedId, t.accountCreditedId, "Credited account ID is invalid")
        Assert.assertEquals(r.accountCreditedCategoryId, t.accountCreditedCategoryId, "Credited account category ID is invalid")
        Assert.assertEquals(r.accountCreditedType, t.accountCreditedType, "Credited account category type ID is invalid")

        Assert.assertEquals(r.day, t.day, "Day is invalid")
        Assert.assertEquals(r.comment, t.comment, "Comment is invalid")
        Assert.assertEquals(r.checked, t.checked, "Checked status is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testNewTransactionSameCurrencyNoContact() {
        val transaction = createTestTransaction(acc_1, acc_3, null)

        resultedBuilder = null

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()

            setUserInput(pane, transaction, null)
            pane.setOnAddTransaction({ builder, c ->
                Assert.assertTrue(c.isEmpty())
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressAddButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.id(1).build()

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, transaction.contactId, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, BigDecimal.ONE, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testNewTransactionSameCurrencyExistingContact() {
        val transaction = createTestTransaction(acc_1, acc_3, contact)

        resultedBuilder = null

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()

            setUserInput(pane, transaction, null)
            pane.setOnAddTransaction({ builder, c ->
                Assert.assertTrue(c.isEmpty())
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressAddButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.id(1).build()

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, transaction.contactId, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, BigDecimal.ONE, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testNewTransactionSameCurrencyNewContact() {
        val transaction = createTestTransaction(acc_1, acc_3, contact)

        resultedBuilder = null

        val lock = java.lang.Object()

        val newContact = UUID.randomUUID().toString()

        Platform.runLater {
            val pane = createEditorPane()

            setUserInput(pane, transaction, newContact)
            pane.setOnAddTransaction({ builder, c ->
                Assert.assertEquals(c, newContact)
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressAddButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.id(1).build()

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, 0, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, BigDecimal.ONE, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testEditorFieldsInitialStateSameCurrency() {
        val transaction = createTestTransaction(acc_1, acc_3, contact)

        resultedBuilder = null

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()
            pane.setTransaction(transaction)

            pane.setOnAddTransaction({ builder, c ->
                Assert.assertTrue(c.isEmpty())
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressAddButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.build()
        Assert.assertEquals(resultedTransaction.id, transaction.id)

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, transaction.contactId, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, transaction.rate, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testEditorFieldsInitialStateDifferentCurrency() {
        val transaction = createTestTransaction(acc_1, acc_2, contact)

        resultedBuilder = null

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()
            pane.setTransaction(transaction)

            pane.setOnAddTransaction({ builder, c ->
                Assert.assertTrue(c.isEmpty())
                Assert.assertFalse(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressAddButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.build()
        Assert.assertEquals(resultedTransaction.id, transaction.id)

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, transaction.contactId, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, transaction.rate, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testUpdatedTransactionSameCurrencyNoContact() {
        val transaction = createTestTransaction(acc_1, acc_3, null)

        resultedBuilder = null

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()
            pane.setTransaction(transaction)

            pane.setOnUpdateTransaction({ builder, c ->
                Assert.assertTrue(c.isEmpty())
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled)

                synchronized(lock) {
                    resultedBuilder = builder
                    lock.notify()
                }
            })

            pressUpdateButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedBuilder)

        val resultedTransaction = resultedBuilder!!.build()
        Assert.assertEquals(resultedTransaction.id, transaction.id)

        assertMainFields(resultedTransaction, transaction)
        Assert.assertEquals(resultedTransaction.contactId, transaction.contactId, "Contact ID is invalid")
        Assert.assertEquals(resultedTransaction.rate, BigDecimal.ONE, "Rate is invalid")
        Assert.assertEquals(resultedTransaction.amount, transaction.amount, "Amount is invalid")
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteButton() {
        val transaction = createTestTransaction(acc_1, acc_3, null)

        val lock = java.lang.Object()

        Platform.runLater {
            val pane = createEditorPane()

            callPrivateMethod(pane, "setupContactMenu")
            pane.setTransaction(transaction)

            pane.setOnDeleteTransaction({ rId ->
                synchronized(lock) {
                    resultedId = rId
                    lock.notify()
                }
            })

            pressDeleteButton(pane)
        }

        synchronized(lock) {
            lock.wait()
        }

        Assert.assertNotNull(resultedId)
        Assert.assertEquals(resultedId, transaction.id)
    }

    companion object {
        private val RANDOM = Random(System.currentTimeMillis())
    }
}
