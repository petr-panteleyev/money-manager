/*
 * Copyright (c) 2018, 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import org.panteleyev.money.persistence.BaseDaoTest;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionType;
import org.panteleyev.money.statements.StatementRecord;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TestEditorPane extends BaseDaoTest {
    private Currency curr_1;
    private Currency curr_2;
    private Category category;
    private Contact contact;
    private Account acc_1;
    private Account acc_2;
    private Account acc_3;

    @BeforeClass
    @Override
    public void setupAndSkip() {
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
        curr_1 = new Currency.Builder(newCurrencyId())
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(new BigDecimal(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

        curr_2 = new Currency.Builder(newCurrencyId())
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(new BigDecimal(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

        category = new Category.Builder(newCategoryId())
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

        contact = new Contact.Builder(newContactId())
            .name(UUID.randomUUID().toString())
            .build();

        acc_1 = new Account.Builder(newAccountId())
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryId(category.getId())
            .currencyId(curr_1.getId())
            .enabled(true)
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

        // different currency
        acc_2 = new Account.Builder(newAccountId())
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryId(category.getId())
            .currencyId(curr_2.getId())
            .enabled(true)
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

        // same currency as (1)
        acc_3 = new Account.Builder(newAccountId())
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryId(category.getId())
            .currencyId(curr_1.getId())
            .enabled(true)
            .guid(UUID.randomUUID().toString())
            .modified(System.currentTimeMillis())
            .build();

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
            .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP))
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

    private StatementRecord createTestStatementRecord() {
        StatementRecord.Builder builder = new StatementRecord.Builder()
            .actual(LocalDate.now())
            .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP).toString());
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

    private void assertStatementRecord(TransactionEditorPane pane, StatementRecord record) {
        TextField sumEdit = getControl(pane, "sumEdit");
        Assert.assertEquals(sumEdit.getText(), record.getAmount());

        Spinner<Integer> daySpinner = getControl(pane, "daySpinner");
        Assert.assertEquals((int) daySpinner.getValueFactory().getValue(), record.getActual().getDayOfMonth());
    }

    private void failOnEmptyBuilder() {
        Assert.fail("Builder is null");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.id(1).build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(),
                "Contact ID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.id(1).build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(),
                "Contact ID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        String newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            setUserInput(pane, transaction, newContact);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertEquals(c, newContact);
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.id(1).build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), 0, "Contact ID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, contact);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(),
                "Contact ID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");

        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_2, contact);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertFalse(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(),
                "Contact ID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(),
                "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testUpdatedTransactionSameCurrencyNoContact() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        BlockingQueue<Optional<Transaction.Builder>> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnUpdateTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(getTextField(pane, "rate1Edit").isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressUpdateButton(pane);
        });

        Optional<Transaction.Builder> result = queue.take();
        result.ifPresentOrElse(builder -> {
            Transaction resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getId(), transaction.getId());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactId(), transaction.getContactId(), "Contact ID is " +
                "invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testDeleteButton() throws Exception {
        Transaction transaction = createTestTransaction(acc_1, acc_3, null);

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();

            callPrivateMethod(pane, "setupContactMenu");
            pane.setTransaction(transaction);
            pane.setOnDeleteTransaction(queue::add);

            pressDeleteButton(pane);
        });

        int resultedId = queue.take();
        Assert.assertEquals(resultedId, transaction.getId());
    }

    @Test
    public void testStatementRecord() throws Exception {
        StatementRecord record = createTestStatementRecord();

        BlockingQueue<TransactionEditorPane> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            TransactionEditorPane pane = createEditorPane();
            pane.setTransactionFromStatement(record, null);
            queue.add(pane);
        });

        TransactionEditorPane pane = queue.take();
        assertStatementRecord(pane, record);
    }
}
