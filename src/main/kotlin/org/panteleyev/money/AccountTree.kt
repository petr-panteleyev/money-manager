/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money

import javafx.application.Platform
import javafx.collections.MapChangeListener
import javafx.geometry.Insets
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.panteleyev.money.cells.AccountBalanceCell
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionFilter
import java.util.EnumMap
import java.util.ResourceBundle
import java.util.function.Predicate

class AccountTree : BorderPane() {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val tableView = TreeTableView<AccountTreeItem>()

    // Filters
    private val accountFilterBox = ChoiceBox<Any>()
    private val transactionFilterBox = ChoiceBox<Any>()

    private val showDeactivatedAccountsMenuItem = CheckMenuItem("Show deactivated accounts")

    private val root = TreeItem<AccountTreeItem>()
    private val balanceRoot: TreeItem<AccountTreeItem>
    private val expIncRoot: TreeItem<AccountTreeItem>

    private val banksSubTree: TreeItem<AccountTreeItem>
    private val debtsSubTree: TreeItem<AccountTreeItem>
    private val portfolioSubTree: TreeItem<AccountTreeItem>
    private val assetsSubTree: TreeItem<AccountTreeItem>
    private val incomeSubTree: TreeItem<AccountTreeItem>
    private val expenseSubTree: TreeItem<AccountTreeItem>

    private val subRoots = EnumMap<CategoryType, TreeItem<AccountTreeItem>>(CategoryType::class.java)

    // tree update globals
    private var categoryTreeItem: TreeItem<AccountTreeItem>? = null

    // Listeners
    private var accountSelectedConsumer: (Account?) -> Unit = {  }
    private var transactionFilterConsumer: (Predicate<Transaction>) -> Unit = {  }

    private val accountListener = MapChangeListener<Int, Account> {
        Platform.runLater { initAccountTree() }
    }
    private val transactionListener = MapChangeListener<Int, Transaction> {
        Platform.runLater { tableView.refresh() }
    }

    init {
        CategoryType.values().forEach {
            subRoots.put(it, TreeItem(AccountTreeItem(it.typeName, it.comment)))
        }

        banksSubTree = subRoots[CategoryType.BANKS_AND_CASH]!!
        debtsSubTree = subRoots[CategoryType.DEBTS]!!
        portfolioSubTree = subRoots[CategoryType.PORTFOLIO]!!
        assetsSubTree = subRoots[CategoryType.ASSETS]!!
        incomeSubTree = subRoots[CategoryType.INCOMES]!!
        expenseSubTree = subRoots[CategoryType.EXPENSES]!!

        // Table
        val nameColumn = TreeTableColumn<AccountTreeItem, String>(rb.getString("column.Name"))
        val commentColumn = TreeTableColumn<AccountTreeItem, String>(rb.getString("column.Comment"))
        val approvedColumn = TreeTableColumn<AccountTreeItem, Account>(rb.getString("column.Approved"))
        val balanceColumn = TreeTableColumn<AccountTreeItem, Account>(rb.getString("column.Balance"))
        val waitingColumn = TreeTableColumn<AccountTreeItem, Account>(rb.getString("column.Waiting"))

        tableView.columns.setAll(nameColumn, commentColumn, approvedColumn, balanceColumn, waitingColumn)

        // Context menu
        val item = MenuItem(rb.getString("menu.Edit.newAccount"))
        item.setOnAction { onNewAccount() }

        tableView.contextMenu = ContextMenu(item, SeparatorMenuItem(), showDeactivatedAccountsMenuItem)
        tableView.isShowRoot = false

        // Tool box
        val hBox = HBox(5.0, accountFilterBox, transactionFilterBox)

        top = hBox
        center = tableView

        BorderPane.setMargin(hBox, Insets(5.0, 5.0, 5.0, 5.0))

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        initAccountFilterBox(rb)

        balanceRoot = TreeItem(AccountTreeItem(rb.getString("account.Tree.Balance"), ""))
        expIncRoot = TreeItem(AccountTreeItem(rb.getString("account.Tree.IncomesExpenses"), "Income - Expenses"))

        nameColumn.cellValueFactory = TreeItemPropertyValueFactory<AccountTreeItem, String>("name")
        commentColumn.cellValueFactory = TreeItemPropertyValueFactory<AccountTreeItem, String>("comment")
        approvedColumn.cellValueFactory = TreeItemPropertyValueFactory<AccountTreeItem, Account>("account")
        approvedColumn.setCellFactory { AccountBalanceCell(true, Predicate<Transaction> { it.checked }) }
        balanceColumn.cellValueFactory = TreeItemPropertyValueFactory<AccountTreeItem, Account>("account")
        balanceColumn.setCellFactory { AccountBalanceCell(true, Predicate<Transaction> { true }) }
        waitingColumn.cellValueFactory = TreeItemPropertyValueFactory<AccountTreeItem, Account>("account")
        waitingColumn.setCellFactory { AccountBalanceCell(Predicate<Transaction> { t -> !t.checked }) }

        nameColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.3))
        commentColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.4))
        approvedColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1))
        balanceColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1))
        waitingColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1))

        initTreeSkeleton()

        balanceRoot.isExpanded = true
        banksSubTree.isExpanded = true
        debtsSubTree.isExpanded = true
        expIncRoot.isExpanded = true

        showDeactivatedAccountsMenuItem.isSelected = Options.showDeactivatedAccounts

        tableView.selectionModel.selectedItemProperty()
                .addListener { _, _, newItem -> onTreeItemSelected(newItem) }

        accountFilterBox.selectionModel.selectedIndexProperty()
                .addListener { _, _, newValue -> onAccountFilterSelected(newValue as Int) }

        transactionFilterBox.selectionModel.selectedItemProperty()
                .addListener { _, _, newValue -> onTransactionFilterSelected(newValue) }


        MoneyDAO.accounts().addListener(accountListener)
        MoneyDAO.transactions().addListener(transactionListener)

        MoneyDAO.preloadingProperty().addListener { _, _, newValue ->
            if (!newValue) {
                Platform.runLater { this.initTransactionFilterBox() }
                Platform.runLater { this.initAccountTree() }
            }
        }

    }

    fun clear() {
        banksSubTree.children.clear()
        portfolioSubTree.children.clear()
        assetsSubTree.children.clear()
        debtsSubTree.children.clear()
        incomeSubTree.children.clear()
        expenseSubTree.children.clear()
    }

    private fun initAccountFilterBox(rb: ResourceBundle) {
        accountFilterBox.items.setAll(
                rb.getString("text.All.Accounts"),
                Separator(),
                rb.getString("account.Tree.Balance"),
                rb.getString("account.Tree.IncomesExpenses"),
                Separator()
        )

        CategoryType.values().forEach {
            accountFilterBox.items.add(it.typeName)
        }

        accountFilterBox.selectionModel.clearAndSelect(0)
    }

    private fun initTransactionFilterBox() {
        transactionFilterBox.items.setAll(
                TransactionFilter.ALL,
                Separator(),
                TransactionFilter.CURRENT_YEAR,
                TransactionFilter.CURRENT_MONTH,
                TransactionFilter.CURRENT_WEEK,
                Separator(),
                TransactionFilter.LAST_YEAR,
                TransactionFilter.LAST_QUARTER,
                TransactionFilter.LAST_MONTH,
                Separator()
        )

        for (i in TransactionFilter.JANUARY.ordinal..TransactionFilter.DECEMBER.ordinal) {
            transactionFilterBox.items.add(TransactionFilter.values()[i])
        }

        transactionFilterBox.items.add(Separator())

        MoneyDAO.getTransactions()
                .map { it.year }
                .distinct()
                .sorted()
                .forEach { transactionFilterBox.items.add(it) }

        transactionFilterBox.selectionModel.clearAndSelect(0)
    }

    private fun initSubtree(categoryType: CategoryType) {
        val rootItem = subRoots[categoryType]!!
        rootItem.children.clear()

        categoryTreeItem = null

        MoneyDAO.getAccountsByType(categoryType)
                .filter { it.enabled || (!it.enabled && Options.showDeactivatedAccounts) }
                .sortedWith(Account.AccountCategoryNameComparator())
                .forEach {
                    if (categoryTreeItem == null || it.categoryId != categoryTreeItem!!.value.id) {
                        MoneyDAO.getCategory(it.categoryId)?.let { category ->
                            categoryTreeItem = TreeItem(AccountTreeItem(category))
                            categoryTreeItem!!.isExpanded = category.expanded

                            categoryTreeItem?.expandedProperty()?.addListener {
                                _, _, newValue -> MoneyDAO.updateCategory(category.expand(newValue!!))
                            }

                            rootItem.children.add(categoryTreeItem)
                        }
                    }

                    categoryTreeItem!!.children.add(TreeItem(AccountTreeItem(it)))
                }
    }

    private fun initTreeSkeleton() {
        root.children.setAll(balanceRoot, expIncRoot)
        balanceRoot.children.setAll(banksSubTree, portfolioSubTree, assetsSubTree, debtsSubTree)
        expIncRoot.children.setAll(incomeSubTree, expenseSubTree)
        tableView.root = root
    }

    private fun initAccountTree() {
        CategoryType.values().forEach { initSubtree(it) }
    }

    private fun onAccountFilterSelected(newValue: Int?) {
        when (newValue) {
            0 -> initTreeSkeleton()
            2 -> tableView.setRoot(balanceRoot)
            3 -> tableView.setRoot(expIncRoot)
            else -> {
                val type = CategoryType.values()[newValue!! - 5]
                tableView.setRoot(subRoots[type])
            }
        }
    }

    private fun onTransactionFilterSelected(newValue: Any?) {
        if (newValue is TransactionFilter) {
            transactionFilterConsumer(newValue.predicate)
        } else {
            if (newValue is Int) {
                transactionFilterConsumer(Predicate<Transaction> { it.year == newValue })
            }
        }
    }

    private fun onShowDeactivatedAccounts() {
        Options.showDeactivatedAccounts = showDeactivatedAccountsMenuItem.isSelected
        if (MoneyDAO.open) {
            initAccountTree()
        }
    }

    private fun onNewAccount() {
        var initialCategory: Category? = null

        val selectedItem = tableView.selectionModel.selectedItem
        if (selectedItem != null) {
            val account = selectedItem.value.accountProperty().value
            initialCategory = if (account != null)
                MoneyDAO.getCategory(account.categoryId)
            else
                selectedItem.value.categoryProperty().value
        }

        AccountDialog(initialCategory).showAndWait().ifPresent {
            MoneyDAO.insertAccount(it.copy(id = MoneyDAO.generatePrimaryKey(Account::class)))
        }
    }

    fun setOnAccountSelected(consumer: (Account?) -> Unit) {
        accountSelectedConsumer = consumer
    }

    fun setOnTransactionFilterSelected(consumer: (Predicate<Transaction>) -> Unit) {
        transactionFilterConsumer = consumer
    }

    private fun onTreeItemSelected(item: TreeItem<AccountTreeItem>?) {
        accountSelectedConsumer(item?.value?.accountProperty()?.value)
    }

}