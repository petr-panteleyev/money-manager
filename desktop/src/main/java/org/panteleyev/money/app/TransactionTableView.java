/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.cells.DocumentCountCell;
import org.panteleyev.money.app.cells.TransactionAccountRequestSumCell;
import org.panteleyev.money.app.cells.TransactionCheckCell;
import org.panteleyev.money.app.cells.TransactionCommentCell;
import org.panteleyev.money.app.cells.TransactionContactCell;
import org.panteleyev.money.app.cells.TransactionCreditedAccountCell;
import org.panteleyev.money.app.cells.TransactionDayCell;
import org.panteleyev.money.app.cells.TransactionDebitedAccountCell;
import org.panteleyev.money.app.cells.TransactionRow;
import org.panteleyev.money.app.cells.TransactionSumCell;
import org.panteleyev.money.app.cells.TransactionTypeCell;
import org.panteleyev.money.app.details.TransactionDetailsDialog;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ATTACH_DOCUMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_DELETE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_UNCHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CREDITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEBITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DAY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DETAILS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DOCUMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

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

    private BiConsumer<List<Transaction>, Boolean> checkTransactionConsumer = (x, y) -> {};
    private final TransactionDetailsCallback transactionDetailsCallback;

    private final Consumer<Transaction> transactionAddedCallback;
    private final Consumer<Transaction> transactionUpdatedCallback;

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Contact> contactChangeLister = change -> Platform.runLater(this::redraw);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Category> categoryChangeLister = change -> Platform.runLater(this::redraw);

    // Transaction filter
    private final PredicateProperty<Transaction> transactionPredicateProperty = new PredicateProperty<>(x -> false);

    private final FilteredList<Transaction> filteredList = new FilteredList<>(cache().getTransactions());

    // List size property
    private final SimpleIntegerProperty listSizeProperty = new SimpleIntegerProperty(0);

    TransactionTableView(Controller owner, Mode mode) {
        this(owner, mode, null, null, x -> {}, x -> {});
    }

    TransactionTableView(Controller owner, Account account) {
        this(owner, Mode.ACCOUNT, account, null, x -> {}, x -> {});
    }

    TransactionTableView(Controller owner,
                         Mode mode,
                         TransactionDetailsCallback transactionDetailsCallback,
                         Consumer<Transaction> transactionAddedCallback,
                         Consumer<Transaction> transactionUpdatedCallback) {
        this(owner, mode, null, transactionDetailsCallback, transactionAddedCallback, transactionUpdatedCallback);
    }

    TransactionTableView(Controller owner,
                         Mode mode,
                         Account account,
                         TransactionDetailsCallback transactionDetailsCallback,
                         Consumer<Transaction> transactionAddedCallback,
                         Consumer<Transaction> transactionUpdatedCallback) {
        this.owner = owner;
        this.mode = mode;
        if (mode == Mode.ACCOUNT && account == null) {
            throw new IllegalArgumentException("Account cannot be null when mode = ACCOUNT");
        }

        this.transactionDetailsCallback = transactionDetailsCallback;
        this.transactionAddedCallback = transactionAddedCallback;
        this.transactionUpdatedCallback = transactionUpdatedCallback;

        setRowFactory(x -> new TransactionRow());

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        var w = widthProperty().subtract(20);
        var dayComparator = mode.isFullDate() ?
                cache().getTransactionByDateComparator() : cache().getTransactionByDayComparator();

        Callback<TableColumn<Transaction, Transaction>, TableCell<Transaction, Transaction>> sumCellFactory =
                mode == Mode.ACCOUNT ?
                        x -> new TransactionAccountRequestSumCell(account) :
                        x -> new TransactionSumCell();

        getColumns().setAll(List.of(
                tableObjectColumn(fxString(UI, I18N_WORD_DAY), b ->
                        b.withCellFactory(x -> new TransactionDayCell(mode.isFullDate()))
                                .withComparator(dayComparator)
                                .withWidthBinding(w.multiply(0.04))),
                tableObjectColumn(fxString(UI, I18N_WORD_TYPE), b ->
                        b.withCellFactory(x -> new TransactionTypeCell())
                                .withComparator(Comparator.comparingInt((Transaction t) -> t.type().ordinal())
                                        .thenComparing(dayComparator))
                                .withWidthBinding(w.multiply(0.1))),
                tableObjectColumn(fxString(UI, I18N_MISC_DEBITED_ACCOUNT), b ->
                        b.withCellFactory(x -> new TransactionDebitedAccountCell())
                                .withComparator(Comparator.comparing(Transaction::accountDebitedUuid)
                                        .thenComparing(dayComparator))
                                .withWidthBinding(w.multiply(0.1))),
                tableObjectColumn(fxString(UI, I18N_MISC_CREDITED_ACCOUNT), b ->
                        b.withCellFactory(x -> new TransactionCreditedAccountCell()).withWidthBinding(w.multiply(0.1))),
                tableObjectColumn(fxString(UI, I18N_WORD_COUNTERPARTY), b ->
                        b.withCellFactory(x -> new TransactionContactCell()).withWidthBinding(w.multiply(0.2))),
                tableObjectColumn(fxString(UI, I18N_WORD_COMMENT), b ->
                        b.withCellFactory(x -> new TransactionCommentCell()).withWidthBinding(w.multiply(0.35))),
                tableObjectColumn(fxString(UI, I18N_WORD_SUM), b ->
                        b.withCellFactory(sumCellFactory)
                                .withComparator(Comparator.comparing(Transaction::getSignedAmount))
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("", b ->
                        b.withCellFactory(x -> new TransactionCheckCell()).withWidthBinding(w.multiply(0.03))),
                tableObjectColumn("", b ->
                        b.withCellFactory(x -> new DocumentCountCell<>()).withWidthBinding(w.multiply(0.03)))
        ));

        var dayColumn = getColumns().get(0);
        getSortOrder().add(dayColumn);
        dayColumn.setSortType(TableColumn.SortType.DESCENDING);

        createContextMenu();

        cache().getContacts().addListener(new WeakListChangeListener<>(contactChangeLister));
        cache().getCategories().addListener(new WeakListChangeListener<>(categoryChangeLister));

        filteredList.predicateProperty().bind(transactionPredicateProperty);

        var sortedList = filteredList.sorted();
        sortedList.comparatorProperty().bind(comparatorProperty());
        setItems(sortedList);
    }

    ReadOnlyIntegerProperty listSizeProperty() {
        return listSizeProperty;
    }

    private void createContextMenu() {
        var disableBinding = getSelectionModel().selectedItemProperty().isNull();

        var newMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), event -> onNewTransaction());
        var editMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), event -> onEditTransaction());
        var deleteMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_DELETE, ELLIPSIS), event -> onDeleteTransaction());
        var detailsMenuItem = menuItem(fxString(UI, I18N_WORD_DETAILS, ELLIPSIS), event -> onTransactionDetails());
        var checkMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_CHECK), event -> onCheckTransactions(true));
        var uncheckMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_UNCHECK), event -> onCheckTransactions(false));

        editMenuItem.disableProperty().bind(disableBinding);
        deleteMenuItem.disableProperty().bind(disableBinding);
        detailsMenuItem.disableProperty().bind(disableBinding);

        var ctxMenu = new ContextMenu();

        if (mode == Mode.SUMMARY) {
            ctxMenu.getItems().addAll(
                    newMenuItem
            );
        }

        ctxMenu.getItems().addAll(
                editMenuItem,
                new SeparatorMenuItem()
        );

        if (mode == Mode.SUMMARY || mode == Mode.QUERY || mode == Mode.ACCOUNT) {
            ctxMenu.getItems().addAll(
                    deleteMenuItem,
                    new SeparatorMenuItem()
            );
        }

        if (mode != Mode.STATEMENT) {
            ctxMenu.getItems().addAll(
                    detailsMenuItem,
                    new SeparatorMenuItem(),
                    menuItem(fxString(UI, I18N_MENU_ITEM_ATTACH_DOCUMENT, ELLIPSIS),
                            event -> onAttachDocument(), disableBinding),
                    menuItem(fxString(UI, I18N_WORD_DOCUMENTS, ELLIPSIS),
                            event -> onDocuments(), disableBinding),
                    new SeparatorMenuItem()
            );
        }

        ctxMenu.getItems().addAll(
                checkMenuItem,
                uncheckMenuItem
        );

        setContextMenu(ctxMenu);
    }

    Predicate<Transaction> getTransactionFilter() {
        return transactionPredicateProperty.get();
    }

    void setTransactionFilter(Predicate<Transaction> filter) {
        transactionPredicateProperty.set(filter.and(t -> t.parentUuid() == null));
        listSizeProperty.set(filteredList.size());
    }

    Optional<Transaction> getSelectedTransaction() {
        return Optional.ofNullable(getSelectionModel().getSelectedItem());
    }

    private void onCheckTransaction(List<Transaction> t, boolean checked) {
        checkTransactionConsumer.accept(t, checked);
    }

    void setOnCheckTransaction(BiConsumer<List<Transaction>, Boolean> c) {
        checkTransactionConsumer = c;
    }

    private void redraw() {
        getColumns().get(0).setVisible(false);
        getColumns().get(0).setVisible(true);
    }

    void onTransactionDetails() {
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

    void onCheckTransactions(boolean check) {
        var selection = getCurrentSelection();
        var process = getSelectionModel().getSelectedItems().stream()
                .filter(t -> t.checked() != check)
                .toList();

        onCheckTransaction(process, check);
        restoreSelection(selection);
    }

    void onNewTransaction() {
        new TransactionDialog(owner, settings().getDialogCssFileUrl(), cache()).showAndWait().ifPresent(
                builder -> transactionAddedCallback.accept(dao().insertTransaction(builder))
        );
    }

    void onEditTransaction() {
        var selection = getCurrentSelection();
        getSelectedTransaction()
                .flatMap(selected -> new TransactionDialog(owner, settings().getDialogCssFileUrl(), selected,
                        cache()).showAndWait())
                .ifPresent(builder -> transactionUpdatedCallback.accept(dao().updateTransaction(builder)));
        restoreSelection(selection);
    }

    void onDeleteTransaction() {
        getSelectedTransaction().ifPresent(transaction ->
                new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
                        .showAndWait()
                        .ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                dao().deleteTransaction(transaction);
                            }
                        }));
    }

    boolean checkFocus() {
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

    private void onDocuments() {
        getSelectedTransaction().ifPresent(BaseController::getDocumentController);
    }

    private void onAttachDocument() {
        getSelectedTransaction().ifPresent(transaction -> {
            var controller = BaseController.getDocumentController(transaction);
            controller.onAddDocument();
        });
    }
}
