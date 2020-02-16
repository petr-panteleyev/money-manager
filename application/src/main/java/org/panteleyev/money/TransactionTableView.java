package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.WeakMapChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import org.panteleyev.money.cells.TransactionCheckCell;
import org.panteleyev.money.cells.TransactionContactCell;
import org.panteleyev.money.cells.TransactionCreditedAccountCell;
import org.panteleyev.money.cells.TransactionDayCell;
import org.panteleyev.money.cells.TransactionDebitedAccountCell;
import org.panteleyev.money.cells.TransactionRow;
import org.panteleyev.money.cells.TransactionSumCell;
import org.panteleyev.money.cells.TransactionTypeCell;
import org.panteleyev.money.details.TransactionDetailsDialog;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.xml.Export;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;

class TransactionTableView extends TableView<Transaction> {
    public interface TransactionDetailsCallback {
        void handleTransactionDetails(Transaction transaction, List<TransactionDetail> details);
    }

    public enum Mode {
        ACCOUNT(false),
        STATEMENT(true),
        QUERY(true);

        private final boolean fullDate;

        Mode(boolean fullDate) {
            this.fullDate = fullDate;
        }

        public boolean isFullDate() {
            return fullDate;
        }
    }

    private final Mode mode;
    private BiConsumer<List<Transaction>, Boolean> checkTransactionConsumer = (x, y) -> { };
    private final TransactionDetailsCallback transactionDetailsCallback;

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Transaction> transactionChangeListener = change ->
        Platform.runLater(() -> transactionListener(change));

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Contact> contactChangeLister = change -> Platform.runLater(this::redraw);

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Category> categoryChangeLister = change -> Platform.runLater(this::redraw);

    // Columns
    private final TableColumn<Transaction, Transaction> dayColumn;

    // Transaction filter
    private Predicate<Transaction> transactionFilter = x -> false;

    // List size property
    private SimpleIntegerProperty listSizeProperty = new SimpleIntegerProperty(0);

    TransactionTableView(Mode mode) {
        this(mode, null);
    }

    TransactionTableView(Mode mode, TransactionDetailsCallback transactionDetailsCallback) {
        this.mode = mode;
        this.transactionDetailsCallback = transactionDetailsCallback;
        setRowFactory(x -> new TransactionRow());

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        var w = widthProperty().subtract(20);

        dayColumn = newTableColumn(RB, "Day", x -> new TransactionDayCell(mode.isFullDate()),
            mode.isFullDate() ? MoneyDAO.COMPARE_TRANSACTION_BY_DATE : MoneyDAO.COMPARE_TRANSACTION_BY_DAY,
            w.multiply(0.05));
        dayColumn.setSortType(TableColumn.SortType.ASCENDING);

        getColumns().setAll(List.of(
            dayColumn,
            newTableColumn(RB, "Type", x -> new TransactionTypeCell(),
                Comparator.comparingInt((Transaction t) -> t.getTransactionType().getId())
                    .thenComparing(dayColumn.getComparator()), w.multiply(0.1)),
            newTableColumn(RB, "column.Account.Debited", x -> new TransactionDebitedAccountCell(),
                Comparator.comparing(Transaction::getAccountDebitedUuid)
                    .thenComparing(dayColumn.getComparator()), w.multiply(0.1)),
            newTableColumn(RB, "column.Account.Credited", x -> new TransactionCreditedAccountCell(), w.multiply(0.1)),
            newTableColumn(RB, "Counterparty", x -> new TransactionContactCell(), w.multiply(0.2)),
            newTableColumn(RB, "Comment", null, Transaction::getComment, w.multiply(0.35)),
            newTableColumn(RB, "Sum", x -> new TransactionSumCell(),
                Comparator.comparing(Transaction::getSignedAmount), w.multiply(0.05)),
            newTableColumn("", x -> {
                var cell = new TransactionCheckCell();
                cell.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        toggleTransactionCheck();
                    }
                });
                return cell;
            }, w.multiply(0.05))
        ));

        getSortOrder().add(dayColumn);

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        createContextMenu();

        cache().transactions().addListener(new WeakMapChangeListener<>(transactionChangeListener));
        cache().contacts().addListener(new WeakMapChangeListener<>(contactChangeLister));
        cache().categories().addListener(new WeakMapChangeListener<>(categoryChangeLister));
    }

    TableColumn<Transaction, Transaction> getDayColumn() {
        return dayColumn;
    }

    ReadOnlyIntegerProperty listSizeProperty() {
        return listSizeProperty;
    }

    private void createContextMenu() {
        var exportMenuItem = newMenuItem(RB, "menu.Context.Export", x -> onExportTransactions());
        var detailsMenuItem = newMenuItem(RB, "menu.item.details", x -> onTransactionDetails());
        var checkMenuItem = newMenuItem(RB, "menu.item.check",
            new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN),
            x -> onCheckTransactions(true));
        var uncheckMenuItem = newMenuItem(RB, "menu.item.uncheck",
            new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN),
            x -> onCheckTransactions(false));

        detailsMenuItem.disableProperty().bind(getSelectionModel().selectedItemProperty().isNull());

        var ctxMenu = new ContextMenu();

        if (mode != Mode.STATEMENT) {
            ctxMenu.getItems().addAll(
                exportMenuItem,
                new SeparatorMenuItem(),
                detailsMenuItem,
                new SeparatorMenuItem()
            );
        }

        ctxMenu.getItems().addAll(
            checkMenuItem,
            uncheckMenuItem
        );

        setContextMenu(ctxMenu);
    }

    void clear() {
        getItems().clear();
    }

    Predicate<Transaction> getTransactionFilter() {
        return transactionFilter;
    }

    private void addRecords(List<Transaction> transactions) {
        transactions.forEach(t -> getItems().add(t));
        listSizeProperty.set(getItems().size());
    }

    void setTransactionFilter(Predicate<Transaction> filter) {
        transactionFilter = filter.and(t -> t.getParentUuid().isEmpty());

        getSelectionModel().clearSelection();
        clear();

        addRecords(cache().getTransactions().stream().filter(transactionFilter).collect(Collectors.toList()));
        sort();
    }

    int getSelectedTransactionCount() {
        return getSelectionModel().getSelectedIndices().size();
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

    private void onExportTransactions() {
        var toExport = getSelectionModel().getSelectedItems();
        if (toExport.isEmpty()) {
            return;
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        var selected = fileChooser.showSaveDialog(null);
        if (selected != null) {
            CompletableFuture.runAsync(() -> {
                try (var out = new FileOutputStream(selected)) {
                    new Export().withTransactions(toExport, true)
                        .doExport(out);
                    Options.setLastExportDir(selected.getParent());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    private void transactionListener(MapChangeListener.Change<? extends UUID, ? extends Transaction> change) {
        var added = change.wasAdded() && transactionFilter.test(change.getValueAdded()) ?
            change.getValueAdded() : null;

        int index = change.wasRemoved() ?
            getItems().indexOf(change.getValueRemoved()) : -1;

        if (index != -1) {
            if (added != null) {
                getItems().set(index, added);
            } else {
                getItems().remove(index);
            }
        } else {
            if (added != null) {
                getItems().add(added);
            }
        }

        listSizeProperty.set(getItems().size());

        Platform.runLater(() -> {
            sort();
            if (added != null) {
                getSelectionModel().clearSelection();
                getSelectionModel().select(added);
            }
        });
    }

    private void redraw() {
        getColumns().get(0).setVisible(false);
        getColumns().get(0).setVisible(true);
    }

    void onTransactionDetails() {
        getSelectedTransaction().ifPresent(t -> {
            var childTransactions = cache().getTransactionDetails(t);

            if (mode == Mode.ACCOUNT) {
                if (transactionDetailsCallback == null) {
                    return;
                }
                new TransactionDetailsDialog(childTransactions, t.getAmount(), false)
                    .showAndWait()
                    .ifPresent(list -> transactionDetailsCallback.handleTransactionDetails(t, list));
            } else {
                new TransactionDetailsDialog(childTransactions, BigDecimal.ZERO, true).showAndWait();
            }
        });
    }

    private void toggleTransactionCheck() {
        getSelectedTransaction().ifPresent(t -> onCheckTransaction(List.of(t), !t.getChecked()));
    }

    void onCheckTransactions(boolean check) {
        var process = getSelectionModel().getSelectedItems().stream()
            .filter(t -> t.getChecked() != check)
            .collect(Collectors.toList());

        onCheckTransaction(process, check);
    }
}
