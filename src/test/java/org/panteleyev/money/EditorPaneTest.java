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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class EditorPaneTest extends BaseDaoTest {
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
        var curr_1 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(new BigDecimal(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        var curr_2 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(new BigDecimal(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        category = new Category.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        contact = new Contact.Builder()
            .name(UUID.randomUUID().toString())
            .build();

        acc_1 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_1.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // different currency
        acc_2 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_2.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // same currency as (1)
        acc_3 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_1.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
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

    private TransactionEditorPane createEditorPane() {
        var pane = new TransactionEditorPane();
        pane.initControls();
        callPrivateMethod(pane, "onChangedTransactionTypes");
        callPrivateMethod(pane, "setupContactMenu");
        callPrivateMethod(pane, "setupAccountMenus");
        callPrivateMethod(pane, "setupComments");
        return pane;
    }

    private void setUserInput(TransactionEditorPane pane, Transaction t, String contactName) {
        // Transaction type
        pane.getTypeEdit().setText(t.getTransactionType().getTypeName());

        // Debited account
        pane.getDebitedAccountEdit().setText(getDao().getAccount(t.getAccountDebitedUuid())
            .map(Account::getName)
            .orElse(""));

        // Credited account
        pane.getCreditedAccountEdit().setText(getDao().getAccount(t.getAccountCreditedUuid())
            .map(Account::getName)
            .orElse(""));

        pane.getCommentEdit().setText(t.getComment());
        pane.getSumEdit().setText(t.getAmount().toString());
        pane.getCheckedCheckBox().setSelected(t.getChecked());

        t.getContactUuid().ifPresent(uuid -> pane.getContactEdit().setText(getDao()
            .getContact(uuid)
            .map(Contact::getName)
            .orElse("")));
        if (contactName != null) {
            pane.getContactEdit().setText(contactName);
        }
    }

    private void callPrivateMethod(Object object, String name) {
        try {
            var method = object.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(object);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressAddButton(TransactionEditorPane pane) {
        pane.getAddButton().fireEvent(new ActionEvent());
    }

    private void pressUpdateButton(TransactionEditorPane pane) {
        pane.getUpdateButton().fireEvent(new ActionEvent());
    }

    private void pressDeleteButton(TransactionEditorPane pane) {
        pane.getDeleteButton().fireEvent(new ActionEvent());
    }

    private Transaction createTestTransaction(Account debit, Account credit, Contact contact) {
        var now = LocalDate.now();
        var builder = new Transaction.Builder()
            .guid(UUID.randomUUID())
            .transactionType(TransactionType.CARD_PAYMENT)
            .accountCreditedType(category.getType())
            .accountDebitedType(category.getType())
            .accountCreditedCategoryUuid(category.getUuid())
            .accountDebitedCategoryUuid(category.getUuid())
            .accountDebitedUuid(debit.getUuid())
            .accountCreditedUuid(credit.getUuid())
            .day(now.getDayOfMonth())
            .month(now.getMonthValue())
            .year(now.getYear())
            .comment(UUID.randomUUID().toString())
            .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP))
            .checked(RANDOM.nextBoolean());

        if (Objects.equals(debit.getCurrencyUuid(), credit.getCurrencyUuid())) {
            builder.rate(BigDecimal.ONE);
        } else {
            builder.rate(new BigDecimal(RANDOM.nextDouble()));
        }

        if (contact != null) {
            builder.contactUuid(contact.getUuid());
        }

        return builder.build();
    }

    private StatementRecord createTestStatementRecord() {
        var builder = new StatementRecord.Builder()
            .actual(LocalDate.now())
            .amount(new BigDecimal(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP).toString());
        return builder.build();
    }

    private void assertMainFields(Transaction r, Transaction t) {
        Assert.assertEquals(r.getTransactionType(), t.getTransactionType(),
            "Transaction type ID is invalid");

        // Debited account
        Assert.assertEquals(r.getAccountDebitedUuid(), t.getAccountDebitedUuid(),
            "Debited account UUID is invalid");
        Assert.assertEquals(r.getAccountDebitedCategoryUuid(), t.getAccountDebitedCategoryUuid(),
            "Debited account category UUID is invalid");
        Assert.assertEquals(r.getAccountDebitedType(), t.getAccountDebitedType(),
            "Debited account category type ID is invalid");

        // Credited account
        Assert.assertEquals(r.getAccountCreditedUuid(), t.getAccountCreditedUuid(),
            "Credited account UUID is invalid");
        Assert.assertEquals(r.getAccountCreditedCategoryUuid(), t.getAccountCreditedCategoryUuid(),
            "Credited account category UUID is invalid");
        Assert.assertEquals(r.getAccountCreditedType(), t.getAccountCreditedType(),
            "Credited account category type ID is invalid");

        Assert.assertEquals(r.getDay(), t.getDay(), "Day is invalid");
        Assert.assertEquals(r.getComment(), t.getComment(), "Comment is invalid");
        Assert.assertEquals(r.getChecked(), t.getChecked(), "Checked status is invalid");
    }

    private void assertStatementRecord(TransactionEditorPane pane, StatementRecord record) {
        Assert.assertEquals(pane.getSumEdit().getText(), record.getAmount());
        Assert.assertEquals((int) pane.getDaySpinner().getValueFactory().getValue(),
            record.getActual().getDayOfMonth());
    }

    private void failOnEmptyBuilder() {
        Assert.fail("Builder is null");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        var newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, newContact);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertEquals(c, newContact);
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertTrue(resultedTransaction.getContactUuid().isEmpty(), "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");

        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_2, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertFalse(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(),
                "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testUpdatedTransactionSameCurrencyNoContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnUpdateTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressUpdateButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testDeleteButton() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<UUID>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();

            callPrivateMethod(pane, "setupContactMenu");
            pane.setTransaction(transaction);
            pane.setOnDeleteTransaction(queue::add);

            pressDeleteButton(pane);
        });

        var resultedUuid = queue.take();
        Assert.assertEquals(resultedUuid, transaction.getUuid());
    }

    @Test
    public void testStatementRecord() throws Exception {
        var record = createTestStatementRecord();

        var queue = new ArrayBlockingQueue<TransactionEditorPane>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransactionFromStatement(record, null);
            queue.add(pane);
        });

        var pane = queue.take();
        assertStatementRecord(pane, record);
    }
}
