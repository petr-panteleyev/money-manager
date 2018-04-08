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
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.panteleyev.money.cells.TransactionContactCell;
import org.panteleyev.money.cells.TransactionCreditedAccountCell;
import org.panteleyev.money.cells.TransactionDayCell;
import org.panteleyev.money.cells.TransactionDebitedAccountCell;
import org.panteleyev.money.cells.TransactionRow;
import org.panteleyev.money.cells.TransactionSumCell;
import org.panteleyev.money.cells.TransactionTypeCell;
import org.panteleyev.money.persistence.SplitTransaction;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.xml.Export;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TransactionTableView extends TableView<Transaction> {
    private BiConsumer<TransactionGroup, List<Transaction>> addGroupConsumer = (x, y) -> {
    };
    private Consumer<List<Transaction>> ungroupConsumer = x -> {
    };
    private BiConsumer<List<Transaction>, Boolean> checkTransactionConsumer = (x, y) -> {
    };

    // Menu items
    private final MenuItem ctxGroupMenuItem = new MenuItem(RB.getString("menu.Edit.Group"));
    private final MenuItem ctxUngroupMenuItem = new MenuItem(RB.getString("menu.Edit.Ungroup"));

    // Columns
    private final TableColumn<Transaction, Transaction> dayColumn;

    // Transaction filter
    private Predicate<Transaction> transactionFilter = x -> false;

    // List size property
    private SimpleIntegerProperty listSizeProperty = new SimpleIntegerProperty(0);

    public TransactionTableView(boolean fullDate) {
        setRowFactory(x -> new TransactionRow());

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dayColumn = new TableColumn<>(RB.getString("column.Day"));
        dayColumn.setCellValueFactory((TableColumn.CellDataFeatures<Transaction, Transaction> p) ->
                new SimpleObjectProperty<>(p.getValue()));
        dayColumn.setCellFactory(x -> new TransactionDayCell(fullDate));
        dayColumn.setSortable(true);

        if (fullDate) {
            dayColumn.comparatorProperty().set((o1, o2) -> {
                int res = o1.getYear() - o2.getYear();
                if (res != 0) {
                    return res;
                } else {
                    res = o1.getMonth() - o2.getMonth();
                    if (res != 0) {
                        return res;
                    } else {
                        return o1.getDay() - o2.getDay();
                    }
                }
            });
        } else {
            dayColumn.comparatorProperty().set(Comparator.comparingInt(Transaction::getDay));
        }

        setSortPolicy(new TransactionTableSortPolicy());

        var typeColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Type"));
        typeColumn.setCellValueFactory(
                (TableColumn.CellDataFeatures<Transaction, Transaction> p) -> {
                    Transaction v = (p.getValue() instanceof SplitTransaction || p.getValue().getGroupId() == 0) ?
                            p.getValue() : null;

                    return new ReadOnlyObjectWrapper<>(v);
                }
        );
        typeColumn.setCellFactory(x -> new TransactionTypeCell());
        typeColumn.setComparator(Comparator.comparingInt((Transaction t) -> t.getTransactionType().getId())
                .thenComparing(dayColumn.getComparator()));
        typeColumn.setSortable(true);

        var accountFromColumn = new TableColumn<Transaction, Transaction>(RB.getString("column.Account.Debited"));
        accountFromColumn.setCellFactory(x -> new TransactionDebitedAccountCell());
        accountFromColumn.setCellValueFactory(
                (TableColumn.CellDataFeatures<Transaction, Transaction> p) -> {
                    Transaction v = p.getValue() instanceof SplitTransaction || p.getValue().getGroupId() == 0 ?
                            p.getValue() : null;

                    return new ReadOnlyObjectWrapper<>(v);
                });

        accountFromColumn.setComparator(Comparator.comparingInt(Transaction::getAccountDebitedId)
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

        var approvedColumn = new TableColumn<Transaction, CheckBox>("");

        approvedColumn.setCellValueFactory(p -> {
            var cb = new CheckBox();

            var value = p.getValue();
            if (value instanceof SplitTransaction) {
                var transactions = getTransactionsByGroup(value.getGroupId());
                cb.setSelected(transactions.stream().allMatch(Transaction::getChecked));
                cb.setOnAction(event -> onCheckTransaction(transactions, cb.isSelected()));
            } else {
                cb.setDisable(value.getGroupId() != 0);
                cb.setSelected(value.getChecked());
                cb.setOnAction(event -> onCheckTransaction(Collections.singletonList(value), cb.isSelected()));
            }

            return new ReadOnlyObjectWrapper<>(cb);
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
        var exportMenuItem = new MenuItem(RB.getString("menu.Context.Export"));
        exportMenuItem.setOnAction(event -> onExportTransactions());

        var ctxMenu = new ContextMenu(exportMenuItem);
        ctxMenu.setOnShowing(event -> onShowingContextMenu());

        // No full date means we are in the transaction editing enabled mode
        if (!fullDate) {
            // Context menu
            ctxGroupMenuItem.setOnAction(event -> onGroup());
            ctxUngroupMenuItem.setOnAction(event -> onUngroup());

            ctxMenu.getItems().addAll(
                    new SeparatorMenuItem(),
                    ctxGroupMenuItem,
                    ctxUngroupMenuItem
            );
        }

        setContextMenu(ctxMenu);

        getDao().transactions().addListener(this::transactionListener);
        getDao().transactionGroups().addListener(this::transactionGroupListener);
    }

    TableColumn<Transaction, Transaction> getDayColumn() {
        return dayColumn;
    }

    ReadOnlyIntegerProperty listSizeProperty() {
        return listSizeProperty;
    }

    private void onShowingContextMenu() {
        boolean disableGroup;
        boolean disableUngroup;

        List<Transaction> selectedItems = getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            disableGroup = true;
            disableUngroup = true;
        } else {
            if (selectedItems.size() == 1) {
                disableGroup = true;
                var item = selectedItems.get(0);
                disableUngroup = !(item instanceof SplitTransaction);
            } else {
                disableUngroup = true;

                // Check if we don't have groups or group members selected
                var hasGroups = selectedItems.stream().anyMatch(t -> t.getGroupId() != 0);

                if (hasGroups) {
                    disableGroup = true;
                } else {
                    // All transactions must have same day and debited account
                    int day = selectedItems.get(0).getDay();
                    int debitedId = selectedItems.get(0).getAccountDebitedId();

                    disableGroup = selectedItems.stream()
                            .anyMatch(t -> t.getDay() != day || t.getAccountDebitedId() != debitedId);
                }
            }
        }

        ctxGroupMenuItem.setDisable(disableGroup);
        ctxUngroupMenuItem.setDisable(disableUngroup);
    }

    void clear() {
        getItems().clear();
    }

    private void addRecords(List<Transaction> transactions) {
        // Add transactions with no group
        transactions.stream()
                .filter(t -> t.getGroupId() == 0)
                .forEach(t -> getItems().add(t));


        var transactionsByGroup = transactions.stream()
                .filter(t -> t.getGroupId() != 0)
                .collect(Collectors.groupingBy(Transaction::getGroupId));

        transactionsByGroup.forEach((groupId, group) -> {
            if (!group.isEmpty()) {
                SplitTransaction split = new SplitTransaction(groupId, group);
                getItems().add(split);
                getItems().addAll(group);
            }
        });

        listSizeProperty.set(getItems().size());
    }

    public void setTransactionFilter(Predicate<Transaction> filter) {
        transactionFilter = filter;

        getSelectionModel().clearSelection();
        clear();

        addRecords(getDao().getTransactions().stream().filter(filter).collect(Collectors.toList()));
        sort();
    }

    int getSelectedTransactionCount() {
        return getSelectionModel().getSelectedIndices().size();
    }

    Optional<Transaction> getSelectedTransaction() {
        var t = getSelectionModel().getSelectedItem();
        if (t instanceof SplitTransaction) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(t);
        }
    }

    // Method assumes that all checks are done in onShowingContextMenu method
    private void onGroup() {
        // List of selected transactions
        var transactions = getSelectionModel().getSelectedItems();

        var first = transactions.get(0);

        var tg = new TransactionGroup(0, first.getDay(), first.getMonth(), first.getYear(),
                false, UUID.randomUUID().toString(), System.currentTimeMillis());

        addGroupConsumer.accept(tg, new ArrayList<>(transactions));
    }

    // Method assumes that all checks are done in onShowingContextMenu method
    private void onUngroup() {
        ungroupConsumer.accept(getTransactionsByGroup(getSelectionModel().getSelectedItem().getGroupId()));
    }

    private void onCheckTransaction(List<Transaction> t, boolean checked) {
        checkTransactionConsumer.accept(t, checked);
    }

    void setOnAddGroup(BiConsumer<TransactionGroup, List<Transaction>> bc) {
        addGroupConsumer = bc;
    }

    void setOnDeleteGroup(Consumer<List<Transaction>> c) {
        ungroupConsumer = c;
    }

    public void setOnCheckTransaction(BiConsumer<List<Transaction>, Boolean> c) {
        checkTransactionConsumer = c;
    }

    private List<Transaction> getTransactionsByGroup(int groupId) {
        return getItems().stream()
                .filter(t -> t.getGroupId() == groupId && !(t instanceof SplitTransaction))
                .collect(Collectors.toList());
    }

    private void onExportTransactions() {
        var toExport = getSelectionModel().getSelectedItems().stream()
                .filter(t -> !(t instanceof SplitTransaction))
                .collect(Collectors.toList());

        if (!toExport.isEmpty()) {
            var fileChooser = new FileChooser();
            fileChooser.setTitle("Export to file");
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
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
    }

    private void transactionListener(MapChangeListener.Change<? extends Integer, ? extends Transaction> change) {
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

    private void transactionGroupListener(MapChangeListener.Change<? extends Integer, ? extends TransactionGroup> change) {
        if (change.wasRemoved()) {
            // find appropriate group
            getItems().stream()
                    .filter(t -> t.getGroupId() == change.getValueRemoved().getId() && t instanceof SplitTransaction)
                    .findFirst()
                    .ifPresent(t -> getItems().remove(t));
        }

        if (change.wasAdded()) {
            // check if this list has transactions from this group
            var group = getItems().stream()
                    .filter(t -> t.getGroupId() == change.getValueAdded().getId() && !(t instanceof SplitTransaction))
                    .collect(Collectors.toList());
            if (!group.isEmpty()) {
                var split = new SplitTransaction(change.getValueAdded().getId(), group);
                getItems().add(split);
                Platform.runLater(this::sort);
            }
        }

        listSizeProperty.set(getItems().size());
    }
}
