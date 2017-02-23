/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.utilities.fx.Controller;
import java.net.URL;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransactionsTab extends Controller implements Initializable {
    private static final String FXML = "/org/panteleyev/money/TransactionsTab.fxml";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    @FXML private BorderPane        pane;
    @FXML private ChoiceBox<Object> accountFilterBox;
    @FXML private ChoiceBox<String> monthFilterBox;
    @FXML private Spinner<Integer>  yearSpinner;
    @FXML private Label             transactionCountLabel;

    private final TransactionTableView  transactionTable    = new TransactionTableView(false);
    private final TransactionEditorPane transactionEditor   = new TransactionEditorPane().load();

    private final SimpleBooleanProperty preloadingProperty = new SimpleBooleanProperty();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final SimpleMapProperty<Integer, Account> accountsProperty =
            new SimpleMapProperty<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final SimpleMapProperty<Integer, Transaction> transactionsProperty =
            new SimpleMapProperty<>();

    TransactionsTab() {
        super(FXML, MainWindowController.UI_BUNDLE_PATH, false);

        MoneyDAO dao = MoneyDAO.getInstance();

        preloadingProperty.bind(dao.preloadingProperty());
        accountsProperty.bind(dao.accountsProperty());
        transactionsProperty.bind(dao.transactionsProperty());

        accountsProperty.addListener((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(this::initAccountFilterBox);
                Platform.runLater(this::reloadTransactions);
            }
        });

        transactionsProperty.addListener((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(this::reloadTransactions);
            }
        });

        preloadingProperty.addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                Platform.runLater(this::initAccountFilterBox);
                Platform.runLater(this::reloadTransactions);
            }
        });
    }

    BorderPane getPane() {
        return pane;
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        transactionTable.setOnMouseClicked((e) -> onTransactionSelected());

        pane.setCenter(transactionTable);
        pane.setBottom(transactionEditor.getPane());

        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8146356
        TextStyle textStyle = TextStyle.FULL_STANDALONE;
        String testMonth = Month.JANUARY.getDisplayName(textStyle, Locale.getDefault());
        if (testMonth.equals("1")) {
            textStyle = TextStyle.FULL;
        } else {
            Logging.getLogger().info("JDK-8146356 has been resolved");
        }

        for (int i = 1; i <= 12; i++) {
            monthFilterBox.getItems()
                .add(Month.of(i).getDisplayName(textStyle, Locale.getDefault()));
        }

        SpinnerValueFactory<Integer> valueFactory = new IntegerSpinnerValueFactory(1970, 2050);
        yearSpinner.setValueFactory(valueFactory);
        yearSpinner.valueProperty().addListener((x,y,z) -> Platform.runLater(this::reloadTransactions));

        transactionEditor.setOnAddTransaction(this::onAddTransaction);
        transactionEditor.setOnUpdateTransaction(this::onUpdateTransaction);
        transactionEditor.setOnDeleteTransaction(this::onDeleteTransaction);

        transactionTable.setOnAddGroup(this::onAddGroup);
        transactionTable.setOnDeleteGroup(this::onDeleteGroup);
        transactionTable.setOnCheckTransaction(this::onCheckTransaction);
        transactionTable.setOnExpandGroup(this::onExpandGroup);

        setCurrentDate();

        accountFilterBox.setConverter(new ReadOnlyStringConverter<Object>() {

            @Override
            public String toString(Object object) {
                if (object instanceof String) {
                    return object.toString();
                }

                if (object instanceof Account) {
                    Account a = (Account)object;

                    switch (a.getType()) {
                        case BANKS_AND_CASH:
                            return "[" + a.getName() + "]";
                        case INCOMES:
                            return "+ " + a.getName();
                        case EXPENSES:
                            return "- " + a.getName();
                        case DEBTS:
                            return "! " + a.getName();
                        case ASSETS:
                            return ". " + a.getName();
                        default:
                            return a.getName();
                    }
                }

                return null;
            }
        });

        accountFilterBox.getSelectionModel().selectedIndexProperty()
                .addListener((x,y,z) -> Platform.runLater(this::reloadTransactions));
    }

    private void addAccountsToChoiceBox(Collection<Account> aList) {
        if (!aList.isEmpty()) {
            accountFilterBox.getItems().add(new Separator());

            aList.stream()
                    .sorted((Account o1, Account o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                    .filter(Account::isEnabled)
                    .forEach(a -> accountFilterBox.getItems().add(a));
        }
    }

    private void initAccountFilterBox() {
        accountFilterBox.getItems().setAll(
                BUNDLE.getString("text.All.Accounts")
        );

        MoneyDAO dao = MoneyDAO.getInstance();
        addAccountsToChoiceBox(dao.getAccountsByType(CategoryType.BANKS_AND_CASH));
        addAccountsToChoiceBox(dao.getAccountsByType(CategoryType.DEBTS));
        addAccountsToChoiceBox(dao.getAccountsByType(CategoryType.ASSETS));

        accountFilterBox.getSelectionModel().select(0);
    }

    private void setCurrentDate() {
        Calendar cal = Calendar.getInstance();
        monthFilterBox.getSelectionModel().select(cal.get(Calendar.MONTH));
        yearSpinner.getValueFactory().setValue(cal.get(Calendar.YEAR));
    }

    public void onPrevMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() - 1;

        if (month < 0) {
            month = 11;
            yearSpinner.getValueFactory().decrement(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    public void onNextMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;

        if (month == 12) {
            month = 0;
            yearSpinner.getValueFactory().increment(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    public void onCurrentMonth() {
        setCurrentDate();

        reloadTransactions();
    }

    private void reloadTransactions() {
        transactionTable.clear();
        transactionEditor.clear();

        transactionTable.getSelectionModel().select(null);

        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        List<Transaction> original = MoneyDAO.getInstance().getTransactions(month, year);

        List<Transaction> records = original;
        if (accountFilterBox.getSelectionModel().getSelectedIndex() != 0) {
            Integer id = Optional.ofNullable((Account)accountFilterBox.getSelectionModel().getSelectedItem())
                    .map(Account::getId)
                    .orElse(0);

            records = original.stream()
                    .filter(t -> t.getAccountCreditedId().equals(id) || t.getAccountDebitedId().equals(id))
                    .collect(Collectors.toList());
        }

        transactionTable.addRecords(records);
        transactionTable.sort();
        transactionCountLabel.setText(Integer.toString(records.size()));
    }

    public void onMonthChanged() {
        reloadTransactions();
    }

    private void onTransactionSelected() {
        transactionEditor.clear();
        transactionTable.getSelectedTransaction().ifPresent(transactionEditor::setTransaction);
    }

    private Contact createContact(String name) {
        Contact.Builder builder = new Contact.Builder()
                .id(MoneyDAO.getInstance().generatePrimaryKey(Contact.class))
                .type(ContactType.PERSONAL)
                .name(name);

        return MoneyDAO.getInstance()
            .insertContact(builder.build());
    }

    private void onAddTransaction(Transaction.Builder builder, String c) {
        MoneyDAO dao = MoneyDAO.getInstance();

        // contact
        if (c != null) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        builder.id(dao.generatePrimaryKey(Transaction.class))
            .month(month)
            .year(year)
            .groupId(0);

        dao.insertTransaction(builder.build());
        reloadTransactions();
    }

    private void onUpdateTransaction(Transaction.Builder builder, String c) {
        // contact
        if (c != null) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        builder.month(month);
        int year = yearSpinner.getValue();
        builder.year(year);

        MoneyDAO.getInstance().updateTransaction(builder.build());
        reloadTransactions();
    }

    private void onAddGroup(TransactionGroup.Builder builder, List<Transaction> transactions) {
        MoneyDAO dao = MoneyDAO.getInstance();

        TransactionGroup group = builder
                .id(dao.generatePrimaryKey(TransactionGroup.class))
                .build();

        Integer groupId = dao.insertTransactionGroup(group).getId();

        transactions.forEach(t -> dao.updateTransaction(
                new Transaction.Builder(t)
                        .groupId(groupId)
                        .build()
                )
        );

        reloadTransactions();
    }

    private void onDeleteGroup(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            Logging.getLogger().warning("Attempt to delete empty transaction group");
            return;
        }

        MoneyDAO dao = MoneyDAO.getInstance();

        Integer groupId = transactions.get(0).getGroupId();

        transactions.forEach(t -> dao.updateTransaction(
                new Transaction.Builder(t)
                        .groupId(0)
                        .build()
                )
        );

        dao.deleteTransactionGroup(groupId);

        reloadTransactions();
    }

    private void onDeleteTransaction(Integer id) {
        new Alert(AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
                .showAndWait()
                .ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        MoneyDAO.getInstance().deleteTransaction(id);
                        reloadTransactions();
                    }
                });
    }

    private void onCheckTransaction(List<Transaction> transactions, Boolean check) {
        MoneyDAO dao = MoneyDAO.getInstance();

        transactions.forEach(t -> dao.updateTransaction(
                new Transaction.Builder(t)
                        .checked(check)
                        .build()
                )
        );

        reloadTransactions();
    }

    private void onExpandGroup(TransactionGroup group, Boolean expand) {
        MoneyDAO.getInstance().updateTransactionGroup(group.expand(expand));
        reloadTransactions();
    }
}
