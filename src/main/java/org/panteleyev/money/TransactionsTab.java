/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.statements.StatementRecord;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class TransactionsTab extends BorderPane {

    private final ChoiceBox<Object> accountFilterBox = new ChoiceBox<>();
    private final ChoiceBox<String> monthFilterBox = new ChoiceBox<>();
    private final Spinner<Integer> yearSpinner = new Spinner<>();
    private final Label transactionCountLabel = new Label();

    private final TransactionTableView transactionTable = new TransactionTableView(false);
    private final TransactionEditorPane transactionEditor = new TransactionEditorPane();

    private final MapChangeListener<Integer, Account> accountListener = change -> {
        Platform.runLater(this::initAccountFilterBox);
        Platform.runLater(this::reloadTransactions);
    };

    TransactionsTab() {
        Button prevButton = new Button("", getButtonImage("arrow-left-16.png"));
        prevButton.setOnAction(event -> onPrevMonth());

        Button todayButton = new Button("", getButtonImage("bullet-black-16.png"));
        todayButton.setOnAction(event -> onCurrentMonth());

        Button nextButton = new Button("", getButtonImage("arrow-right-16.png"));
        nextButton.setOnAction(event -> onNextMonth());

        monthFilterBox.setOnAction(event -> onMonthChanged());

        HBox hBox = new HBox(5.0,
                accountFilterBox,
                monthFilterBox,
                yearSpinner,
                prevButton,
                todayButton,
                nextButton,
                new Label("Transactions:"),
                transactionCountLabel
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        setTop(hBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        transactionTable.setOnMouseClicked(event -> onTransactionSelected());

        transactionCountLabel.textProperty().bind(transactionTable.listSizeProperty().asString());

        setCenter(transactionTable);
        setBottom(transactionEditor);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory
                .IntegerSpinnerValueFactory(1970, 2050);
        yearSpinner.setValueFactory(valueFactory);
        yearSpinner.valueProperty().addListener((x, y, z) -> Platform.runLater(this::reloadTransactions));

        transactionEditor.setOnAddTransaction(this::onAddTransaction);
        transactionEditor.setOnUpdateTransaction(this::onUpdateTransaction);
        transactionEditor.setOnDeleteTransaction(this::onDeleteTransaction);

        transactionTable.setOnAddGroup(this::onAddGroup);
        transactionTable.setOnDeleteGroup(this::onDeleteGroup);
        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        setCurrentDate();

        accountFilterBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                if (obj instanceof String) {
                    return obj.toString();
                }

                if (obj instanceof Account) {
                    Account account = (Account) obj;
                    switch (account.getType()) {
                        case BANKS_AND_CASH:
                            return "[" + account.getName() + "]";
                        case INCOMES:
                            return "+ " + account.getName();
                        case EXPENSES:
                            return "- " + account.getName();
                        case DEBTS:
                            return "! " + account.getName();
                        case ASSETS:
                            return ". " + account.getName();
                        default:
                            return account.getName();
                    }
                }

                return null;
            }
        });

        accountFilterBox.getSelectionModel().selectedIndexProperty()
                .addListener((x, y, z) -> Platform.runLater(this::reloadTransactions));

        getDao().accounts().addListener(accountListener);

        getDao().preloadingProperty().addListener(
                (x, y, newValue) -> {
                    if (!newValue) {
                        Platform.runLater(this::initAccountFilterBox);
                        Platform.runLater(this::reloadTransactions);
                    }
                });
    }

    TransactionEditorPane getTransactionEditor() {
        return transactionEditor;
    }

    private ImageView getButtonImage(String name) {
        ImageView image = new ImageView(new Image("/org/panteleyev/money/res/" + name));
        image.setFitHeight(16.0);
        image.setFitWidth(16.0);
        return image;
    }

    private void addAccountsToChoiceBox(Collection<Account> aList) {
        if (!aList.isEmpty()) {
            accountFilterBox.getItems().add(new Separator());

            aList.stream()
                    .filter(Account::getEnabled)
                    .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
                    .forEach(account -> accountFilterBox.getItems().add(account));
        }
    }

    private void initAccountFilterBox() {
        accountFilterBox.getItems().setAll(RB.getString("text.All.Accounts"));

        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.BANKS_AND_CASH));
        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.DEBTS));
        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.ASSETS));

        accountFilterBox.getSelectionModel().clearAndSelect(0);
    }

    private void setCurrentDate() {
        Calendar cal = Calendar.getInstance();
        monthFilterBox.getSelectionModel().select(cal.get(Calendar.MONTH));
        yearSpinner.getValueFactory().setValue(cal.get(Calendar.YEAR));
    }

    private void setDate(LocalDate date) {
        monthFilterBox.getSelectionModel().select(date.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(date.getYear());
    }

    private void onPrevMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() - 1;

        if (month < 0) {
            month = 11;
            yearSpinner.getValueFactory().decrement(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onNextMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;

        if (month == 12) {
            month = 0;
            yearSpinner.getValueFactory().increment(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onCurrentMonth() {
        setCurrentDate();

        reloadTransactions();
    }

    private void reloadTransactions() {
        transactionTable.clear();
        transactionTable.getSelectionModel().select(null);

        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        Predicate<Transaction> filter = t -> t.getMonth() == month && t.getYear() == year;

        Object selected = accountFilterBox.getSelectionModel().getSelectedItem();
        if (selected instanceof Account) {
            int id = ((Account) selected).getId();
            filter = filter.and(t -> t.getAccountCreditedId() == id || t.getAccountDebitedId() == id);
        }

        transactionTable.setTransactionFilter(filter);
        transactionTable.sort();
    }

    private void onMonthChanged() {
        reloadTransactions();
    }

    private void onTransactionSelected() {
        transactionEditor.clear();
        if (transactionTable.getSelectedTransactionCount() == 1) {
            transactionTable.getSelectedTransaction().ifPresent(transactionEditor::setTransaction);
        }
    }

    private Contact createContact(String name) {
        return getDao().insertContact(new Contact(getDao().generatePrimaryKey(Contact.class), name));
    }

    private void onAddTransaction(Transaction.Builder builder, String c) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        builder.id(getDao().generatePrimaryKey(Transaction.class))
                .month(month)
                .year(year)
                .groupId(0);

        transactionEditor.clear();
        getDao().insertTransaction(builder.build());
    }

    private void onUpdateTransaction(Transaction.Builder builder, String c) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        builder.month(month);
        int year = yearSpinner.getValue();
        builder.year(year);

        transactionEditor.clear();
        getDao().updateTransaction(builder.build());
    }

    private void onAddGroup(TransactionGroup group, List<Transaction> transactions) {
        int groupId = getDao().generatePrimaryKey(TransactionGroup.class);

        TransactionGroup grp = group.copy(groupId);

        for (Transaction t : transactions) {
            getDao().updateTransaction(t.setGroupId(groupId));
        }

        getDao().insertTransactionGroup(grp);
    }

    private void onDeleteGroup(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            Logging.getLogger().warning("Attempt to delete empty transaction group");
            return;
        }

        int groupId = transactions.get(0).getGroupId();

        for (Transaction t : transactions) {
            getDao().updateTransaction(t.setGroupId(0));
        }

        getDao().deleteTransactionGroup(groupId);
    }

    private void onDeleteTransaction(int id) {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
                .showAndWait()
                .ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        transactionEditor.clear();
                        getDao().deleteTransaction(id);
                    }
                });
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (Transaction t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
    }

    void scrollToEnd() {
        if (transactionTable.getDayColumn().getSortType().equals(TableColumn.SortType.ASCENDING)) {
            transactionTable.scrollTo(getDao().transactions().size() - 1);
        } else {
            transactionTable.scrollTo(0);
        }
    }

    public void handleStatementRecord(StatementRecord record, Account account) {
        Platform.runLater(() -> {
            setDate(record.getActual());
            transactionEditor.setTransactionFromStatement(record, account);
        });
    }
}
