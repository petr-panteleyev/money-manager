/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import static org.panteleyev.commons.fx.FXFactory.newMenuItem;
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

        dayColumn = new TableColumn<>(RB.getString("column.Day"));
        dayColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new SimpleObjectProperty<>(p.getValue()));
        dayColumn.setCellFactory(x -> new TransactionDayCell(mode.isFullDate()));
        dayColumn.setSortable(true);

        if (mode.isFullDate()) {
            dayColumn.comparatorProperty().set(MoneyDAO.COMPARE_TRANSACTION_BY_DATE);
        } else {
            dayColumn.comparatorProperty().set(MoneyDAO.COMPARE_TRANSACTION_BY_DAY);
        }

        var typeColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Type"));
        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        typeColumn.setCellFactory(x -> new TransactionTypeCell());
        typeColumn.setComparator(Comparator.comparingInt((Transaction t) -> t.getTransactionType().getId())
            .thenComparing(dayColumn.getComparator()));
        typeColumn.setSortable(true);

        var accountFromColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Account.Debited"));
        accountFromColumn.setCellFactory(x -> new TransactionDebitedAccountCell());
        accountFromColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));

        accountFromColumn.setComparator(Comparator.comparing(Transaction::getAccountDebitedUuid)
            .thenComparing(dayColumn.getComparator()));

        typeColumn.setSortable(true);

        var accountCreditedColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Account.Credited"));
        accountCreditedColumn.setCellFactory(x -> new TransactionCreditedAccountCell());
        accountCreditedColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        accountCreditedColumn.setSortable(false);

        var contactColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Payer.Payee"));
        contactColumn.setCellFactory(x -> new TransactionContactCell());
        contactColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        contactColumn.setSortable(false);

        var commentColumn = new TableColumn<Transaction, String>(RB.getString("column.Comment"));
        commentColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, String> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getComment()));
        commentColumn.setSortable(false);

        var sumColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Sum"));
        sumColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        sumColumn.setCellFactory(x -> new TransactionSumCell());
        sumColumn.setComparator(Comparator.comparing(Transaction::getSignedAmount));
        sumColumn.setSortable(true);

        var approvedColumn = new TableColumn<Transaction, Transaction>("");
        approvedColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        approvedColumn.setCellFactory(x ->  {
            var cell = new TransactionCheckCell();
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    toggleTransactionCheck();
                }
            });
            return cell;
        });
        approvedColumn.setSortable(false);

        getColumns().setAll(List.of(dayColumn,
            typeColumn,
            accountFromColumn,
            accountCreditedColumn,
            contactColumn,
            commentColumn,
            sumColumn,
            approvedColumn
        ));

        getSortOrder().add(dayColumn);
        dayColumn.setSortType(TableColumn.SortType.ASCENDING);

        // Column width. Temporary solution, there should be a better option.
        dayColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        typeColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        accountFromColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        accountCreditedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        contactColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.2));
        commentColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.35));
        sumColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        approvedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));

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
