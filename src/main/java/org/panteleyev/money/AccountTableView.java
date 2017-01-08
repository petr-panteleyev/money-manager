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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.Transaction;

public class AccountTableView extends TreeTableView<AccountTreeItem> {
    private static class BalanceCell extends TreeTableCell<AccountTreeItem, Account> {
        private Predicate<Transaction> filter;
        private final boolean total;

        public BalanceCell(boolean total, Predicate<Transaction> filter) {
            this.total = total;
            this.filter = filter;
        }

        public BalanceCell(Predicate<Transaction> filter) {
            this(false, filter);
        }

        @Override
        protected void updateItem(Account account, boolean empty) {
            super.updateItem(account, empty);
            this.setAlignment(Pos.CENTER_RIGHT);
            if (empty || account == null) {
                setText("");
            } else {
                List<Transaction> transactions = MoneyDAO.getInstance().getTransactions(account);

                BigDecimal sum = transactions.stream()
                    .filter(filter)
                    .map(t -> {
                        BigDecimal amount = t.getAmount();
                        if (account.getId().equals(t.getAccountCreditedId())) {
                            // handle conversion rate
                            BigDecimal rate = t.getRate();
                            if (rate.compareTo(BigDecimal.ZERO) != 0) {
                                if (t.getRateDirection() == 0) {
                                    amount = amount.divide(rate, BigDecimal.ROUND_HALF_UP);
                                } else {
                                    amount = amount.multiply(rate);
                                }
                            }
                        } else {
                            amount = amount.negate();
                        }

                        return amount;
                    })
                    .reduce(total? account.getOpeningBalance() : BigDecimal.ZERO, BigDecimal::add);

                setText(sum.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                if (sum.signum() < 0) {
                    styleProperty().set("-fx-text-fill: #ff0000;");
                } else {
                    styleProperty().set("-fx-text-fill: #000000;");
                }
            }
        }
    }

    private final TreeItem<AccountTreeItem> root = new TreeItem();

    private final TreeItem<AccountTreeItem> balanceRoot = new TreeItem<>(new AccountTreeItem("Balance", "000"));
    private final TreeItem<AccountTreeItem> banksSubTree = new TreeItem<>(new AccountTreeItem("Banks", "--- banks---"));
    private final TreeItem<AccountTreeItem> debtsSubTree = new TreeItem<>(new AccountTreeItem("Debts", "Credits, credit cards, etc."));
    private final TreeItem<AccountTreeItem> expIncRoot = new TreeItem<>(new AccountTreeItem("Income and Expenses", "Income - Expenses"));
    private final TreeItem<AccountTreeItem> incomeSubTree
        = new TreeItem<>(new AccountTreeItem("Incomes", "Incomes"));
    private final TreeItem<AccountTreeItem> expenseSubTree
        = new TreeItem<>(new AccountTreeItem("Expenses", "Expenses"));

    private final SimpleBooleanProperty preloadingProperty = new SimpleBooleanProperty();

    private final SimpleMapProperty<Integer, Account> accountsProperty =
            new SimpleMapProperty<>();

    // tree update globals
    private TreeItem<AccountTreeItem> categoryTreeItem = null;

    // Account tree context menu items
    private CheckMenuItem showDeactivatedAccountsMenuItem = new CheckMenuItem("Show deactivated accounts");

    public AccountTableView() {
        setContextMenu(createContextMenu());

        setRoot(root);
        setShowRoot(false);

        preloadingProperty.bind(MoneyDAO.getInstance().preloadingProperty());
        accountsProperty.bind(MoneyDAO.getInstance().accountsProperty());

        TreeTableColumn<AccountTreeItem, String> nameColumn =
            new TreeTableColumn<>("Name");
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));

        TreeTableColumn<AccountTreeItem, String> commentColumn =
            new TreeTableColumn<>("Comment");
        commentColumn.setCellValueFactory(new TreeItemPropertyValueFactory("comment"));

        TreeTableColumn<AccountTreeItem, Account> approvedColumn =
            new TreeTableColumn<>("Approved");
        approvedColumn.setCellValueFactory(new TreeItemPropertyValueFactory("account"));
        approvedColumn.setCellFactory(x -> new BalanceCell(true, t -> t.isChecked()));

        TreeTableColumn<AccountTreeItem, Account> balanceColumn =
            new TreeTableColumn<>("Balance");
        balanceColumn.setCellValueFactory(new TreeItemPropertyValueFactory("account"));
        balanceColumn.setCellFactory(x -> new BalanceCell(true, t -> true));

        TreeTableColumn<AccountTreeItem, Account> waitingColumn =
            new TreeTableColumn<>("Waiting");
        waitingColumn.setCellValueFactory(new TreeItemPropertyValueFactory("account"));
        waitingColumn.setCellFactory(x -> new BalanceCell(t -> !t.isChecked()));

        getColumns().addAll(
            nameColumn,
            commentColumn,
            approvedColumn,
            balanceColumn,
            waitingColumn
        );

        root.getChildren().addAll(
            balanceRoot,
            expIncRoot
        );

        balanceRoot.getChildren().addAll(
            banksSubTree,
            debtsSubTree
        );

        expIncRoot.getChildren().addAll(
            incomeSubTree,
            expenseSubTree
        );

        balanceRoot.setExpanded(true);
        banksSubTree.setExpanded(true);
        debtsSubTree.setExpanded(true);
        expIncRoot.setExpanded(true);

        accountsProperty.addListener(((x,y,z) -> {
            if (!preloadingProperty.get()) {
                Platform.runLater(this::initAccountTree);
            }
        }));

        preloadingProperty.addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                Platform.runLater(this::initAccountTree);
            }
        });
    }

    public void clear() {
        banksSubTree.getChildren().clear();
        debtsSubTree.getChildren().clear();
        incomeSubTree.getChildren().clear();
        expenseSubTree.getChildren().clear();
    }

    private void initSubtree(TreeItem rootItem, int categoryTypeId) {
        categoryTreeItem = null;

        MoneyDAO dao = MoneyDAO.getInstance();

        dao.getAccountsByType(categoryTypeId)
            .stream()
            .filter(a -> a.isEnabled() || (!a.isEnabled() && Options.getShowDeactivatedAccounts()))
            .sorted(new Account.AccountCategoryNameComparator()).forEach(a -> {
                if (categoryTreeItem == null || !a.getCategoryId().equals(categoryTreeItem.getValue().getId())) {
                    Category category = dao.getCategory(a.getCategoryId()).get();
                    categoryTreeItem = new TreeItem(new AccountTreeItem(category));
                    categoryTreeItem.setExpanded(category.isExpanded());

                    categoryTreeItem.expandedProperty().addListener((x, y, newValue) -> {
                        MoneyDAO.getInstance().updateCategory(category.expand(newValue));
                    });

                    rootItem.getChildren().add(categoryTreeItem);
                }

                categoryTreeItem.getChildren().add(new TreeItem(new AccountTreeItem(a)));
            });
    }

    public void initAccountTree() {
        clear();
        initSubtree(banksSubTree, CategoryType.BANKS_AND_CASH_ID);
        initSubtree(debtsSubTree, CategoryType.DEBTS_ID);
        initSubtree(incomeSubTree, CategoryType.INCOMES_ID);
        initSubtree(expenseSubTree, CategoryType.EXPENSES_ID);
    }

    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();

        showDeactivatedAccountsMenuItem.setOnAction(e -> onShowDeactivatedAccounts());
        showDeactivatedAccountsMenuItem.setSelected(Options.getShowDeactivatedAccounts());
        menu.getItems().add(showDeactivatedAccountsMenuItem);

        return menu;
    }

    private void onShowDeactivatedAccounts() {
        Options.setShowDeactivatedAccounts(showDeactivatedAccountsMenuItem.isSelected());
        if (MoneyDAO.isOpen()) {
            initAccountTree();
        }
    }
}