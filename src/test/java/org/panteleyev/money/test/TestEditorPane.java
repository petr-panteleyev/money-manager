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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestEditorPane extends BaseDaoTest {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private Currency curr_1 = new Currency(null,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            1, false, false, new BigDecimal(RANDOM.nextDouble()), 1, false
    );

    private Currency curr_2 = new Currency(null,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            1, false, false, new BigDecimal(RANDOM.nextDouble()), 1, false
    );

    private Category category = new Category(null,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            CategoryType.BANKS_AND_CASH,
            false
    );

    private Contact contact;

    private Account acc_1;
    private Account acc_2;  // different currency
    private Account acc_3;  // same currency as (1)

    private Transaction.Builder resultedBuilder;
    private Integer resultedId;

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
        curr_1 = new Currency.Builder(curr_1).id(getDao().generatePrimaryKey(Currency.class)).build();
        getDao().insertCurrency(curr_1);

        curr_2 = new Currency.Builder(curr_2).id(getDao().generatePrimaryKey(Currency.class)).build();
        getDao().insertCurrency(curr_2);

        category = new Category.Builder(category).id(getDao().generatePrimaryKey(Category.class)).build();
        getDao().insertCategory(category);

        contact = new Contact.Builder()
                .id(getDao().generatePrimaryKey(Contact.class))
                .name(UUID.randomUUID().toString())
                .build();
        getDao().insertContact(contact);

        acc_1 = new Account(getDao().generatePrimaryKey(Account.class),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ONE,
            CategoryType.BANKS_AND_CASH,
            category.getId(),
            curr_1.getId(),
            true
        );
        getDao().insertAccount(acc_1);

        acc_2 = new Account(getDao().generatePrimaryKey(Account.class),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ONE,
            CategoryType.BANKS_AND_CASH,
            category.getId(),
            curr_2.getId(),
            true
        );
        getDao().insertAccount(acc_2);

        acc_3 = new Account(getDao().generatePrimaryKey(Account.class),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ONE,
            CategoryType.BANKS_AND_CASH,
            category.getId(),
            curr_1.getId(),
            true
        );
        getDao().insertAccount(acc_3);
    }

    private <T> T getControl(TransactionEditorPane pane, String name, Class<T> clazz) {
        try {
            Field f = pane.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(pane);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private TextField getTextField(TransactionEditorPane pane, String name) {
        return getControl(pane, name, TextField.class);
    }

    private CheckBox getCheckBox(TransactionEditorPane pane, String name) {
        return getControl(pane, name, CheckBox.class);
    }

    private TransactionEditorPane createEditorPane() {
        TransactionEditorPane pane = new TransactionEditorPane().load();
        pane.initControls();
        callPrivateMethod(pane, "onChangedTransactionTypes");
        callPrivateMethod(pane, "setupContactMenu");
        callPrivateMethod(pane, "setupAccountMenus");
        callPrivateMethod(pane, "setupComments");
        return pane;
    }

    private void setUserInput(TransactionEditorPane pane, Transaction t, String contactName) {
        // Transaction type
        getTextField(pane, "typeEdit").setText(t.getTransactionType().getName());

        // Debited account
        getTextField(pane, "debitedAccountEdit")
                .setText(getDao().getAccount(t.getAccountDebitedId()).map(Account::getName).orElse(""));

        // Credited account
        getTextField(pane, "creditedAccountEdit")
                .setText(getDao().getAccount(t.getAccountCreditedId()).map(Account::getName).orElse(""));

        getTextField(pane, "commentEdit").setText(t.getComment());
        getTextField(pane, "sumEdit").setText(t.getAmount().toString());
        getCheckBox(pane, "checkedCheckBox").setSelected(t.isChecked());

        if (t.getContactId() != null) {
            Contact cntct = getDao().getContact(t.getContactId()).get();
            getTextField(pane, "contactEdit").setText(cntct.getName());
        }
        if (contactName != null) {
            getTextField(pane, "contactEdit").setText(contactName);
        }
    }

    private void callPrivateMethod(Object pane, String name) {
        try {
            Method method = pane.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(pane);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressButton(Object pane, String name) {
        try {
            Field f = pane.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Button button = (Button)f.get(pane);
            button.fireEvent(new ActionEvent());
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressAddButton(Object pane) {
        pressButton(pane, "addButton");
    }

    private void pressUpdateButton(Object pane) {
        pressButton(pane, "updateButton");
    }

    private void pressDeleteButton(Object pane) {
        pressButton(pane, "deleteButton");
    }

    private Transaction createTestTransaction(Account debit, Account credit, Contact contact) {
        Transaction.Builder builder = new Transaction.Builder()
                .id(RANDOM.nextInt())
                .transactionType(TransactionType.CARD_PAYMENT)
                .accountCreditedType(category.getCatType())
                .accountDebitedType(category.getCatType())
                .accountCreditedCategoryId(category.getId())
                .accountDebitedCategoryId(category.getId())
                .accountDebitedId(debit.getId())
                .accountCreditedId(credit.getId())
                .day(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .comment(UUID.randomUUID().toString())
                .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
                .checked(RANDOM.nextBoolean());

        if (debit.getCurrencyId().equals(credit.getCurrencyId())) {
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
        assertEquals(r.getTransactionType(), t.getTransactionType(), "Transaction type ID is invalid");

        // Debited account
        assertEquals(r.getAccountDebitedId(), t.getAccountDebitedId(), "Debited account ID is invalid");
        assertEquals(r.getAccountDebitedCategoryId(), t.getAccountDebitedCategoryId(), "Debited account category ID is invalid");
        assertEquals(r.getAccountDebitedType(), t.getAccountDebitedType(), "Debited account category type ID is invalid");

        // Credited account
        assertEquals(r.getAccountCreditedId(), t.getAccountCreditedId(), "Credited account ID is invalid");
        assertEquals(r.getAccountCreditedCategoryId(), t.getAccountCreditedCategoryId(), "Credited account category ID is invalid");
        assertEquals(r.getAccountCreditedType(), t.getAccountCreditedType(), "Credited account category type ID is invalid");

        assertEquals(r.getDay(), t.getDay(), "Day is invalid");
        assertEquals(r.getComment(), t.getComment(), "Comment is invalid");
        assertEquals(r.isChecked(), t.isChecked(), "Checked status is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            setUserInput(pane, transaction, null);
            pane.setOnAddTransaction((builder, c) -> {
                assertNull(c);
                assertTrue(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(0).build();

        assertMainFields(resultedTransaction, transaction);
        assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            setUserInput(pane, transaction, null);
            pane.setOnAddTransaction((builder, c) -> {
                assertNull(c);
                assertTrue(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(0).build();

        assertMainFields(resultedTransaction, transaction);
        assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        final String newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            setUserInput(pane, transaction, newContact);
            pane.setOnAddTransaction((builder, c) -> {
                assertEquals(c, newContact);
                assertTrue(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.id(0).build();

        assertMainFields(resultedTransaction, transaction);
        assertNull(resultedTransaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                assertNull(c);
                assertTrue(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_2, contact);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                assertNull(c);
                assertFalse(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testUpdatedTransactionSameCurrencyNoContact() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        resultedBuilder = null;

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnUpdateTransaction((builder, c) -> {
                assertNull(c);
                assertTrue(getTextField(pane, "rate1Edit").isDisabled());

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

        assertNotNull(resultedBuilder);

        Transaction resultedTransaction = resultedBuilder.build();
        assertEquals(resultedTransaction.getId(), transaction.getId());

        assertMainFields(resultedTransaction, transaction);
        assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is invalid");
        assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
        assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
    }

    @Test
    public void testDeleteButton() throws Exception {
        final Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        final Object lock = new Object();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            callPrivateMethod(pane, "setupContactMenu");
            pane.setTransaction(transaction);

            pane.setOnDeleteTransaction((rId) -> {
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

        assertNotNull(resultedId);
        assertEquals(resultedId, transaction.getId());
    }
}
