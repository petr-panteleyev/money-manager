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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

class TransactionTableView extends TreeTableView<TransactionTreeItem> implements Styles {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private static class TransactionRow extends TreeTableRow<TransactionTreeItem> {
        @Override
        protected void updateItem(TransactionTreeItem item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().remove(GROUP_CELL);
            if (!empty) {
                if (item.isGroupProperty().get()) {
                    getStyleClass().add(GROUP_CELL);
                }
            }
        }
    }

    private static class SumCell extends TreeTableCell<TransactionTreeItem, BigDecimal> {
        @Override
        protected void updateItem(final BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            this.setAlignment(Pos.CENTER_RIGHT);
            if (empty || item == null) {
                setText("");
            } else {
                Optional.ofNullable(getTreeTableRow())
                        .map(TreeTableRow::getTreeItem)
                        .map(TreeItem::getValue)
                        .ifPresent(treeItem -> {
                            getStyleClass().add(treeItem.getStyle());

                            String format = Optional.ofNullable(treeItem.getTransaction())
                                    .filter(t -> t.getGroupId() != 0)
                                    .map(t -> "(%s)")
                                    .orElse("%s");

                            setText(String.format(format, item.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                        });
            }
        }
    }

    private static class DayCell extends TreeTableCell<TransactionTreeItem, TransactionTreeItem> {
        private final boolean fullDate;

        DayCell(boolean fullDate) {
            this.fullDate = fullDate;
        }

        @Override
        protected void updateItem(TransactionTreeItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText("");
            } else {
                if (!item.isGroupProperty().get()) {
                    Transaction t = item.getTransaction();

                    String imageUrl;
                    switch (t.getAccountCreditedType()) {
                        case EXPENSES:
                        case DEBTS:
                            imageUrl = "/org/panteleyev/money/res/red-circle-16.png";
                            break;

                        case BANKS_AND_CASH:
                            switch (t.getAccountDebitedType()) {
                                case INCOMES:
                                    imageUrl = "/org/panteleyev/money/res/blue-circle-16.png";
                                    break;

                                case BANKS_AND_CASH:
                                    imageUrl = "/org/panteleyev/money/res/green-circle-16.png";
                                    break;

                                default:
                                    imageUrl = "/org/panteleyev/money/res/gray-circle-16.png";
                                    break;
                            }
                            break;

                        default:
                            imageUrl = "/org/panteleyev/money/res/gray-circle-16.png";
                            break;
                    }

                    ImageView iv = new ImageView(imageUrl);
                    iv.setFitHeight(8);
                    iv.setFitWidth(8);
                    setGraphic(iv);
                } else {
                    setGraphic(null);
                }

                if (fullDate) {
                    setText(String.format("%02d.%02d.%04d",
                        item.dayProperty().get(),
                        item.monthProperty().get(),
                        item.yearProperty().get()));
                } else {
                    setText(Integer.toString(item.dayProperty().get()));
                }
            }
        }
    }

    private final TreeItem<TransactionTreeItem> root = new TreeItem<>();

    private BiConsumer<TransactionGroup.Builder, List<Transaction>> addGroupConsumer = (t, l) -> {};
    private Consumer<List<Transaction>> ungroupConsumer = (l) -> {};
    private BiConsumer<List<Transaction>, Boolean> checkTransactionConsumer = (t, c) -> {};
    private BiConsumer<TransactionGroup, Boolean> expandGroupConsumer = (g, e) -> {};

    // Menu items
    private MenuItem ctxGroupMenuItem = new MenuItem(BUNDLE.getString("menu.Edit.Group"));
    private MenuItem ctxUngroupMenuItem = new MenuItem(BUNDLE.getString("menu.Edit.Ungroup"));

    TransactionTableView(boolean fullDate) {
        this.setRoot(root);
        this.showRootProperty().set(false);

        this.setRowFactory(x -> new TransactionRow());

        TreeTableColumn<TransactionTreeItem, TransactionTreeItem> dayColumn = new TreeTableColumn<>(BUNDLE.getString("column.Day"));
        dayColumn.setCellValueFactory((CellDataFeatures<TransactionTreeItem, TransactionTreeItem> p) -> new SimpleObjectProperty<>(p.getValue().getValue()));
        dayColumn.setCellFactory(x -> new DayCell(fullDate));
        dayColumn.setSortable(true);

        if (fullDate) {
            dayColumn.comparatorProperty().set((o1, o2) -> {
                int res = o1.yearProperty().get() - o2.yearProperty().get();
                if (res != 0) {
                    return res;
                }

                res = o1.monthProperty().get() - o2.monthProperty().get();
                if (res != 0) {
                    return res;
                }

                return o1.dayProperty().get() - o2.dayProperty().get();
            });
        } else {
            dayColumn.comparatorProperty().set(Comparator.comparingInt(o -> o.dayProperty().get()));
        }

        TreeTableColumn<TransactionTreeItem, String> typeColumn = new TreeTableColumn<>(BUNDLE.getString("column.Type"));
        typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));

        TreeTableColumn<TransactionTreeItem, String> accountFromColumn = new TreeTableColumn<>(BUNDLE.getString("column.Account.Debited"));
        accountFromColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("accountDebited"));

        TreeTableColumn<TransactionTreeItem, String> accountCreditedColumn = new TreeTableColumn<>(BUNDLE.getString("column.Account.Credited"));
        accountCreditedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("accountCredited"));

        TreeTableColumn<TransactionTreeItem, String> contactColumn = new TreeTableColumn<>(BUNDLE.getString("column.Payer.Payee"));
        contactColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("contact"));

        TreeTableColumn<TransactionTreeItem, String> commentColumn = new TreeTableColumn<>(BUNDLE.getString("column.Comment"));
        commentColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("comment"));
        commentColumn.setSortable(false);

        TreeTableColumn<TransactionTreeItem, BigDecimal> sumColumn = new TreeTableColumn<>(BUNDLE.getString("column.Sum"));
        sumColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("sum"));
        sumColumn.setCellFactory(x -> new SumCell());

        TreeTableColumn<TransactionTreeItem, CheckBox> approvedColumn = new TreeTableColumn<>("");

        approvedColumn.setCellValueFactory(p -> {
            CheckBox cb = new CheckBox();

            final TransactionTreeItem value = p.getValue().getValue();
            if (value.isGroupProperty().get()) {
                List<Transaction> transactions = value.getChildren().stream()
                        .map(TransactionTreeItem::getTransaction)
                        .collect(Collectors.toList());

                boolean groupApproved = transactions.stream()
                        .map(Transaction::isChecked)
                        .reduce(true, (x,y)-> x && y);
                cb.setSelected(groupApproved);

                cb.setOnAction(event ->
                        onCheckTransaction(transactions, cb.isSelected()));
            } else {
                cb.setSelected(value.getTransaction().isChecked());
                cb.setOnAction(event ->
                        onCheckTransaction(Collections.singletonList(value.getTransaction()), cb.isSelected()));
            }

            return new ReadOnlyObjectWrapper<>(cb);
        });

        getColumns().addAll(dayColumn,
            typeColumn,
            accountFromColumn,
            accountCreditedColumn,
            contactColumn,
            commentColumn,
            sumColumn,
            approvedColumn
        );

        getSortOrder().addAll(dayColumn);
        dayColumn.setSortType(TreeTableColumn.SortType.ASCENDING);

        // Column width. Temporary solution, there should be a better option.
        dayColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        typeColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        accountFromColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        accountCreditedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1));
        contactColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.2));
        commentColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.35));
        sumColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        approvedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));

        // No full date means we are in the transaction editing enabled mode
        if (!fullDate) {
            getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Context menu
            ctxGroupMenuItem.setOnAction(this::onGroup);
            ctxUngroupMenuItem.setOnAction(this::onUngroup);

            ContextMenu ctxMenu = new ContextMenu(ctxGroupMenuItem, ctxUngroupMenuItem);

            ctxMenu.setOnShowing(this::onShowingContextMenu);
            setContextMenu(ctxMenu);
        }
    }

    private void onShowingContextMenu(WindowEvent windowEvent) {
        boolean disableGroup;
        boolean disableUngroup;

        List<TreeItem<TransactionTreeItem>> selectedItems = getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            disableGroup = true;
            disableUngroup = true;
        } else {
            if (selectedItems.size() == 1) {
                disableGroup = true;
                TransactionTreeItem item = selectedItems.get(0).getValue();
                disableUngroup = !item.isGroupProperty().get();
            } else {
                disableUngroup = true;

                // Check if we don't have groups selected
                boolean hasGroups = selectedItems.stream().anyMatch(x -> x.getValue().isGroupProperty().get());
                if (hasGroups) {
                    disableGroup = true;
                } else {
                    // List of transactions
                    List<Transaction> transactions = selectedItems.stream()
                            .map(x -> x.getValue().getTransaction())
                            .collect(Collectors.toList());

                    // All transactions must have same day and debited account
                    int day = transactions.get(0).getDay();
                    Integer debitedId = transactions.get(0).getAccountDebitedId();

                    disableGroup = transactions.stream().anyMatch(x -> x.getDay() != day || !x.getAccountDebitedId().equals(debitedId));
                }
            }
        }

        ctxGroupMenuItem.setDisable(disableGroup);
        ctxUngroupMenuItem.setDisable(disableUngroup);
    }

    void clear() {
        root.getChildren().clear();
    }

    void addRecords(List<Transaction> transactions) {
        // Add transactions with no group
        transactions.stream()
            .filter(x -> x.getGroupId() == 0)
            .forEach(x -> root.getChildren().add(new TreeItem<>(new TransactionTreeItem(x))));

        // Add groups
        Map<Integer, List<Transaction>> transactionsByGroup = transactions.stream()
            .filter(x -> x.getGroupId() != 0)
            .collect(groupingBy(Transaction::getGroupId));

        transactionsByGroup.keySet().forEach(groupId -> {
            TransactionGroup group = MoneyDAO.getInstance().getTransactionGroup(groupId).get();
            TransactionTreeItem groupItem = new TransactionTreeItem(group, transactionsByGroup.get(groupId));
            TreeItem<TransactionTreeItem> groupTreeItem = new TreeItem<>(groupItem);
            groupTreeItem.setExpanded(group.isExpanded());

            groupTreeItem.expandedProperty().addListener((observable, oldValue, newValue) ->
                    expandGroupConsumer.accept(group, newValue));

            groupItem.getChildren()
                .forEach(t -> groupTreeItem.getChildren().add(new TreeItem<>(t)));

            root.getChildren().add(groupTreeItem);
        });
    }

    Optional<Transaction> getSelectedTransaction() {
        TreeItem<TransactionTreeItem> treeItem = getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return Optional.empty();
        } else {
            TransactionTreeItem item = treeItem.getValue();
            if (item.isGroupProperty().get()) {
                return Optional.empty();
            } else {
                return Optional.of(item.getTransaction());
            }
        }
    }

    // Method assumes that all checks are done in onShowingContextMenu method
    private void onGroup(ActionEvent event) {
        // List of selected transactions
        List<Transaction> transactions = getSelectionModel().getSelectedItems().stream()
                .map(x -> x.getValue().getTransaction())
                .collect(Collectors.toList());

        Transaction first = transactions.get(0);

        TransactionGroup.Builder builder = new TransactionGroup.Builder()
                .day(first.getDay())
                .month(first.getMonth())
                .year(first.getYear());

        addGroupConsumer.accept(builder, transactions);
    }

    // Method assumes that all checks are done in onShowingContextMenu method
    private void onUngroup(ActionEvent event) {
        List<Transaction> transactions = getSelectionModel().getSelectedItem().getValue().getChildren()
                .stream()
                .map(TransactionTreeItem::getTransaction)
                .collect(Collectors.toList());

        ungroupConsumer.accept(transactions);
    }

    private void onCheckTransaction(List<Transaction> t, boolean checked) {
        checkTransactionConsumer.accept(t, checked);
    }

    void setOnAddGroup(BiConsumer<TransactionGroup.Builder, List<Transaction>> bc) {
        addGroupConsumer = bc;
    }

    void setOnDeleteGroup(Consumer<List<Transaction>> c) {
        ungroupConsumer = c;
    }

    void setOnCheckTransaction(BiConsumer<List<Transaction>, Boolean> c) {
        checkTransactionConsumer = c;
    }

    void setOnExpandGroup(BiConsumer<TransactionGroup, Boolean> c) {
        expandGroupConsumer = c;
    }
}
