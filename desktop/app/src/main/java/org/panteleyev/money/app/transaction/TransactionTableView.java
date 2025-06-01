/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.controlsfx.control.action.Action;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.transaction.cells.TransactionAccountRequestSumCell;
import org.panteleyev.money.app.transaction.cells.TransactionCheckCell;
import org.panteleyev.money.app.transaction.cells.TransactionCommentCell;
import org.panteleyev.money.app.transaction.cells.TransactionContactCell;
import org.panteleyev.money.app.transaction.cells.TransactionCreditedAccountCell;
import org.panteleyev.money.app.transaction.cells.TransactionDayCell;
import org.panteleyev.money.app.transaction.cells.TransactionDebitedAccountCell;
import org.panteleyev.money.app.transaction.cells.TransactionSumCell;
import org.panteleyev.money.app.transaction.cells.TransactionTypeCell;
import org.panteleyev.money.app.util.MenuUtils;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_K;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.actions.ActionBuilder.actionBuilder;

public class TransactionTableView extends TableView<Transaction> {
    public interface TransactionDetailsCallback {
        void handleTransactionDetails(Transaction transaction, List<TransactionDetail> details);
    }

    public enum Mode {
        SUMMARY(false),
        STATEMENT(true),
        QUERY(true),
        ACCOUNT(true);

        private final boolean fullDate;

        Mode(boolean fullDate) {
            this.fullDate = fullDate;
        }

        public boolean isFullDate() {
            return fullDate;
        }
    }

    private final Controller owner;
    private final Mode mode;

    private BiConsumer<List<Transaction>, Boolean> checkTransactionConsumer = (_, _) -> {
    };
    private final TransactionDetailsCallback transactionDetailsCallback;

    private final Consumer<Transaction> transactionAddedCallback;
    private final Consumer<Transaction> transactionUpdatedCallback;

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Contact> contactChangeLister = _ -> Platform.runLater(this::redraw);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Category> categoryChangeLister = _ -> Platform.runLater(this::redraw);

    // Transaction filter
    private final PredicateProperty<Transaction> transactionPredicateProperty = new PredicateProperty<>(_ -> false);

    private final FilteredList<Transaction> filteredList = new FilteredList<>(cache().getTransactions());

    // List size property
    private final SimpleIntegerProperty listSizeProperty = new SimpleIntegerProperty(0);

    private final BooleanBinding disableBinding = getSelectionModel().selectedItemProperty().isNull();

    // transaction table view actions

    private final CrudActionsHolder crudActionsHolder = new CrudActionsHolder(
            this::onCreateTransaction, this::onEditTransaction, this::onDeleteTransaction,
            disableBinding
    );

    private final Action transactionDetailsAction = actionBuilder("Детали...", this::onTransactionDetails)
            .disableBinding(disableBinding)
            .build();
    private final Action checkTransactionAction = actionBuilder("Отметить", _ -> onCheckTransactions(true))
            .accelerator(SHORTCUT_K)
            .disableBinding(disableBinding)
            .build();
    private final Action uncheckTransactionAction = actionBuilder("Снять отметку", _ -> onCheckTransactions(false))
            .accelerator(SHORTCUT_U)
            .disableBinding(disableBinding)
            .build();

    private final Collection<Action> actions;

    public TransactionTableView(Controller owner, Mode mode) {
        this(owner, mode, null, null, _ -> {}, _ -> {});
    }

    public TransactionTableView(Controller owner, Account account) {
        this(owner, Mode.ACCOUNT, account, null, _ -> {}, _ -> {});
    }

    public TransactionTableView(Controller owner,
            Mode mode,
            TransactionDetailsCallback transactionDetailsCallback,
            Consumer<Transaction> transactionAddedCallback,
            Consumer<Transaction> transactionUpdatedCallback)
    {
        this(owner, mode, null, transactionDetailsCallback, transactionAddedCallback, transactionUpdatedCallback);
    }

    TransactionTableView(Controller owner,
            Mode mode,
            Account account,
            TransactionDetailsCallback transactionDetailsCallback,
            Consumer<Transaction> transactionAddedCallback,
            Consumer<Transaction> transactionUpdatedCallback)
    {
        this.owner = owner;
        this.mode = mode;
        if (mode == Mode.ACCOUNT && account == null) {
            throw new IllegalArgumentException("Account cannot be null when mode = ACCOUNT");
        }

        this.transactionDetailsCallback = transactionDetailsCallback;
        this.transactionAddedCallback = transactionAddedCallback;
        this.transactionUpdatedCallback = transactionUpdatedCallback;

        setRowFactory(_ -> new TransactionRow());

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        var w = widthProperty().subtract(20);

        Callback<TableColumn<Transaction, Transaction>, TableCell<Transaction, Transaction>> sumCellFactory =
                mode == Mode.ACCOUNT ?
                        _ -> new TransactionAccountRequestSumCell(account) :
                        _ -> new TransactionSumCell();

        TableColumn<Transaction, Transaction> dayColumn = tableObjectColumn("День", b ->
                b.withCellFactory(_ -> new TransactionDayCell(mode.isFullDate()))
                        .withComparator(Comparators.transactionsByDate())
                        .withWidthBinding(w.multiply(0.04)));

        getColumns().setAll(List.of(
                dayColumn,
                tableObjectColumn("Тип", b ->
                        b.withCellFactory(_ -> new TransactionTypeCell())
                                .withComparator(Comparator.comparingInt((Transaction t) -> t.type().ordinal())
                                        .thenComparing(Comparators.transactionsByDate()))
                                .withWidthBinding(w.multiply(0.1))),
                tableObjectColumn("Исходный счет", b ->
                        b.withCellFactory(_ -> new TransactionDebitedAccountCell())
                                .withComparator(Comparators.transactionsByDebitedAccountName(cache())
                                        .thenComparing(Comparators.transactionsByDate()))
                                .withWidthBinding(w.multiply(0.11))),
                tableObjectColumn("Счет получателя", b ->
                        b.withCellFactory(_ -> new TransactionCreditedAccountCell())
                                .withComparator(Comparators.transactionsByCreditedAccountName(cache())
                                        .thenComparing(Comparators.transactionsByDate()))
                                .withWidthBinding(w.multiply(0.11))),
                tableObjectColumn("Контрагент", b ->
                        b.withCellFactory(_ -> new TransactionContactCell())
                                .withComparator(Comparators.transactionsByContactName(cache())
                                        .thenComparing(Comparators.transactionsByDate()))
                                .withWidthBinding(w.multiply(0.21))),
                tableObjectColumn("Комментарий", b ->
                        b.withCellFactory(_ -> new TransactionCommentCell()).withWidthBinding(w.multiply(0.35))),
                tableObjectColumn("Сумма", b ->
                        b.withCellFactory(sumCellFactory)
                                .withComparator(Comparator.comparing(Transaction::getSignedAmount))
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("", b ->
                        b.withCellFactory(_ -> new TransactionCheckCell()).withWidthBinding(w.multiply(0.03)))
        ));

        getSortOrder().add(dayColumn);
        dayColumn.setSortType(TableColumn.SortType.DESCENDING);

        actions = createActions();
        createContextMenu(actions);

        cache().getContacts().addListener(new WeakListChangeListener<>(contactChangeLister));
        cache().getCategories().addListener(new WeakListChangeListener<>(categoryChangeLister));

        filteredList.predicateProperty().bind(transactionPredicateProperty);

        var sortedList = filteredList.sorted();
        sortedList.comparatorProperty().bind(comparatorProperty());
        setItems(sortedList);
    }

    public Collection<Action> getActions() {
        return actions;
    }

    public ReadOnlyIntegerProperty listSizeProperty() {
        return listSizeProperty;
    }

    private Collection<Action> createActions() {
        var actionList = new ArrayList<Action>();
        if (mode == Mode.SUMMARY) {
            actionList.add(crudActionsHolder.getCreateAction());
        }
        actionList.add(crudActionsHolder.getUpdateAction());
        actionList.add(ACTION_SEPARATOR);
        if (mode == Mode.SUMMARY || mode == Mode.QUERY || mode == Mode.ACCOUNT) {
            actionList.addAll(List.of(
                    crudActionsHolder.getDeleteAction(),
                    ACTION_SEPARATOR
            ));
        }

        if (mode != Mode.STATEMENT) {
            actionList.addAll(List.of(
                    transactionDetailsAction,
                    ACTION_SEPARATOR
            ));
        }

        actionList.addAll(List.of(
                checkTransactionAction,
                uncheckTransactionAction
        ));
        return actionList;
    }

    private void createContextMenu(Collection<? extends Action> actions) {
        setContextMenu(MenuUtils.createContextMenu(actions));
    }

    public Predicate<Transaction> getTransactionFilter() {
        return transactionPredicateProperty.get();
    }

    public void setTransactionFilter(Predicate<Transaction> filter) {
        transactionPredicateProperty.set(filter.and(t -> t.parentUuid() == null));
        listSizeProperty.set(filteredList.size());
    }

    Optional<Transaction> getSelectedTransaction() {
        return Optional.ofNullable(getSelectionModel().getSelectedItem());
    }

    private void onCheckTransaction(List<Transaction> t, boolean checked) {
        checkTransactionConsumer.accept(t, checked);
    }

    public void setOnCheckTransaction(BiConsumer<List<Transaction>, Boolean> c) {
        checkTransactionConsumer = c;
    }

    private void redraw() {
        getColumns().getFirst().setVisible(false);
        getColumns().getFirst().setVisible(true);
    }

    void onTransactionDetails(ActionEvent ignored) {
        getSelectedTransaction().ifPresent(t -> {
            var childTransactions = cache().getTransactionDetails(t);

            if (mode == Mode.SUMMARY) {
                if (transactionDetailsCallback == null) {
                    return;
                }
                new TransactionDetailsDialog(owner, childTransactions, t.amount(), false)
                        .showAndWait()
                        .ifPresent(list -> transactionDetailsCallback.handleTransactionDetails(t, list));
            } else {
                new TransactionDetailsDialog(owner, childTransactions, BigDecimal.ZERO, true).showAndWait();
            }
        });
    }

    public void onCheckTransactions(boolean check) {
        var selection = getCurrentSelection();
        var process = getSelectionModel().getSelectedItems().stream()
                .filter(t -> t.checked() != check)
                .toList();

        onCheckTransaction(process, check);
        restoreSelection(selection);
    }

    void onCreateTransaction(ActionEvent event) {
        new TransactionDialog(owner, settings().getDialogCssFileUrl(), cache()).showAndWait().ifPresent(
                builder -> transactionAddedCallback.accept(dao().insertTransaction(builder))
        );
    }

    void onEditTransaction(ActionEvent event) {
        var selection = getCurrentSelection();
        getSelectedTransaction()
                .flatMap(selected -> new TransactionDialog(owner, settings().getDialogCssFileUrl(), selected,
                        cache()).showAndWait())
                .ifPresent(builder -> transactionUpdatedCallback.accept(dao().updateTransaction(builder)));
        restoreSelection(selection);
    }

    void onDeleteTransaction(ActionEvent event) {
        getSelectedTransaction().ifPresent(transaction ->
                new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
                        .showAndWait()
                        .ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                dao().deleteTransaction(transaction);
                            }
                        }));
    }

    public boolean checkFocus() {
        return isFocused();
    }

    public ObservableList<Transaction> selectedTransactions() {
        return getSelectionModel().getSelectedItems();
    }

    private List<UUID> getCurrentSelection() {
        return getSelectionModel().getSelectedItems().stream()
                .map(Transaction::uuid)
                .toList();
    }

    /**
     * This method checks if table has any items with uuid from the list and selects all that exists.
     *
     * @param selection uuid of items to be selected
     */
    private void restoreSelection(List<UUID> selection) {
        Platform.runLater(() -> {
            getSelectionModel().clearSelection();
            for (var uuid : selection) {
                getItems().stream()
                        .filter(t -> t.uuid().equals(uuid))
                        .findAny()
                        .ifPresent(t -> getSelectionModel().select(t));
            }
        });
    }
}
