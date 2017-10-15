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

package org.panteleyev.money.test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.panteleyev.money.TransactionEditorPane;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionType;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TestEditorPane extends BaseDaoTest {
    private Currency curr_1;
    private Currency curr_2;
    private Category category;
    private Contact contact;
    private Account acc_1;
    private Account acc_2;
    private Account acc_3;

    private Transaction.Builder resultedBuilder = null;
    private int resultedId = 0;

    @BeforeClass
    @Override
    public void setupAndSkip() throws Exception {
        try {
            super.setupAndSkip();
            getDao().createTables();
            initializeEmptyMoneyFile();
            createData();
            new JFXPanel();         // required to initialize FX toolkit
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        super.cleanup();
    }

    private void createData() {
        curr_1 = new Currency(newCurrencyId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                1, false, false,
                new BigDecimal(RANDOM.nextDouble()), 1, false,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        curr_2 = new Currency(newCurrencyId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                1, false, false,
                new BigDecimal(RANDOM.nextDouble()), 1, false,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        category = new Category(newCategoryId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                CategoryType.BANKS_AND_CASH.getId(),
                false,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        contact = new Contact(newContactId(), UUID.randomUUID().toString());

        acc_1 = new Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.getId(),
                category.getId(),
                curr_1.getId(),
                true,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        // different currency
        acc_2 = new Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.getId(),
                category.getId(),
                curr_2.getId(),
                true,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        // same currency as (1)
        acc_3 = new Account(newAccountId(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                CategoryType.BANKS_AND_CASH.getId(),
                category.getId(),
                curr_1.getId(),
                true,
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );

        getDao().insertCurrency(curr_1);
        getDao().insertCurrency(curr_2);
        getDao().insertCategory(category);
        getDao().insertContact(contact);
        getDao().insertAccount(acc_1);
        getDao().insertAccount(acc_2);
        getDao().insertAccount(acc_3);
    }

    private <T> T getControl(TransactionEditorPane pane, String name) {
        try {
            Field f = pane.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(pane);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private TextField getTextField(TransactionEditorPane pane, String name) {
        return getControl(pane, name);
    }

    private CheckBox getCheckBox(TransactionEditorPane pane, String name) {
        return getControl(pane, name);
    }

    private TransactionEditorPane createEditorPane() {
        TransactionEditorPane pane = new TransactionEditorPane();
        pane.initControls();
        callPrivateMethod(pane, "onChangedTransactionTypes");
        callPrivateMethod(pane, "setupContactMenu");
        callPrivateMethod(pane, "setupAccountMenus");
        callPrivateMethod(pane, "setupComments");
        return pane;
    }

    private void setUserInput(TransactionEditorPane pane, Transaction t, String contactName) {
        // Transaction type
        getTextField(pane, "typeEdit").setText(t.getTransactionType().getTypeName());

        // Debited account
        getTextField(pane, "debitedAccountEdit").setText(getDao().getAccount(t.getAccountDebitedId())
                .map(Account::getName)
                .orElse(""));

        // Credited account
        getTextField(pane, "creditedAccountEdit").setText(getDao().getAccount(t.getAccountCreditedId())
                .map(Account::getName)
                .orElse(""));

        getTextField(pane, "commentEdit").setText(t.getComment());
        getTextField(pane, "sumEdit").setText(t.getAmount().toString());
        getCheckBox(pane, "checkedCheckBox").setSelected(t.getChecked());

        if (t.getContactId() != 0) {
            Optional<Contact> cntct = getDao().getContact(t.getContactId());
            getTextField(pane, "contactEdit").setText(cntct.map(Contact::getName).orElse(""));
        }
        if (contactName != null) {
            getTextField(pane, "contactEdit").setText(contactName);
        }
    }

    private void callPrivateMethod(Object object, String name) {
        try {
            Method method = object.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(object);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressButton(Object object, String name) {
        try {
            Field f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Button button = (Button) f.get(object);
            button.fireEvent(new ActionEvent());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressAddButton(Object object) {
        pressButton(object, "addButton");
    }

    private void pressUpdateButton(Object object) {
        pressButton(object, "updateButton");
    }

    private void pressDeleteButton(Object object) {
        pressButton(object, "deleteButton");
    }

    private Transaction createTestTransaction(Account debit, Account credit, Contact contact) {
        Transaction.Builder builder = new Transaction.Builder()
                .id(RANDOM.nextInt())
                .transactionType(TransactionType.CARD_PAYMENT)
                .accountCreditedType(category.getType())
                .accountDebitedType(category.getType())
                .accountCreditedCategoryId(category.getId())
                .accountDebitedCategoryId(category.getId())
                .accountDebitedId(debit.getId())
                .accountCreditedId(credit.getId())
                .day(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .comment(UUID.randomUUID().toString())
                .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
                .checked(RANDOM.nextBoolean());

        if (debit.getCurrencyId() == credit.getCurrencyId()) {
            builder.rate(BigDecimal.ONE);
        } else {
            builder.rate(new BigDecimal(RANDOM.nextDouble()));
        }

        if (contact != null) {
            builder.contactId(contact.getId());
        }

        return builder.build();
    }

    private void assertMainFields(Transaction r, Transaction t) {
        Assert.assertEquals(r.getTransactionType(), t.getTransactionType(),
                "Transaction type ID is invalid");

        // Debited account
        Assert.assertEquals(r.getAccountDebitedId(), t.getAccountDebitedId(),
                "Debited account ID is invalid");
        Assert.assertEquals(r.getAccountDebitedCategoryId(), t.getAccountDebitedCategoryId(),
                "Debited account category ID is invalid");
        Assert.assertEquals(r.getAccountDebitedType(), t.getAccountDebitedType(),
                "Debited account category type ID is invalid");

        // Credited account
        Assert.assertEquals(r.getAccountCreditedId(), t.getAccountCreditedId(),
                "Credited account ID is invalid");
        Assert.assertEquals(r.getAccountCreditedCategoryId(), t.getAccountCreditedCategoryId(),
                "Credited account category ID is invalid");
        Assert.assertEquals(r.getAccountCreditedType(), t.getAccountCreditedType(),
                "Credited account category type ID is invalid");

        Assert.assertEquals(r.getDay(), t.getDay(), "Day is invalid");
        Assert.assertEquals(r.getComment(), t.getComment(), "Comment is invalid");
        Assert.assertEquals(r.getChecked(), t.getChecked(), "Checked status is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressAddButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(1).build();

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressAddButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(1).build();

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        String newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, newContact);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertEquals(c, newContact);
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressAddButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(1).build();

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), 0, "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressAddButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_2, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertFalse(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressAddButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testUpdatedTransactionSameCurrencyNoContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnUpdateTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());

                synchronized (lock) {
                    resultedBuilder = builder;
                    lock.notify();
                }
            });

            pressUpdateButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testDeleteButton() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            callPrivateMethod(pane, "setupContactMenu");
            pane.setTransaction(transaction);

            pane.setOnDeleteTransaction(rId -> {
                synchronized (lock) {
                    resultedId = rId;
                    lock.notify();
                }
            });

            pressDeleteButton(pane);
        });

        synchronized (lock) {
            lock.wait();
        }

        Assert.assertEquals(resultedId, transaction.getId());
    }
}
