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
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.MapChangeListener
import javafx.scene.control.CheckBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.FileChooser
import org.panteleyev.money.cells.TransactionContactCell
import org.panteleyev.money.cells.TransactionCreditedAccountCell
import org.panteleyev.money.cells.TransactionDayCell
import org.panteleyev.money.cells.TransactionDebitedAccountCell
import org.panteleyev.money.cells.TransactionRow
import org.panteleyev.money.cells.TransactionSumCell
import org.panteleyev.money.cells.TransactionTypeCell
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.SplitTransaction
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import org.panteleyev.money.xml.Export
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.ResourceBundle
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

internal class TransactionTableView(fullDate: Boolean) : TableView<Transaction>() {

    private var addGroupConsumer: (TransactionGroup, List<Transaction>) -> Unit = { _, _ -> }
    private var ungroupConsumer: (List<Transaction>) -> Unit = { }
    private var checkTransactionConsumer: (List<Transaction>, Boolean) -> Unit = { _, _ -> }

    // Menu items
    private val ctxGroupMenuItem = MenuItem(BUNDLE.getString("menu.Edit.Group"))
    private val ctxUngroupMenuItem = MenuItem(BUNDLE.getString("menu.Edit.Ungroup"))

    // Columns
    val dayColumn: TableColumn<Transaction, Transaction>

    // Transaction filter
    private var transactionFilter = Predicate<Transaction> { false }

    // List size property
    private val listSizeProperty = SimpleIntegerProperty(0)

    init {
        setRowFactory { TransactionRow() }
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        dayColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Day"))
        dayColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Transaction, Transaction> -> SimpleObjectProperty(p.value) }
        dayColumn.setCellFactory { TransactionDayCell(fullDate) }
        dayColumn.isSortable = true

        if (fullDate) {
            dayColumn.comparatorProperty().set(Comparator<Transaction> { o1, o2 ->
                var res = o1.year - o2.year
                if (res != 0) {
                    res
                } else {
                    res = o1.month - o2.month
                    if (res != 0) {
                        res
                    } else {
                        o1.day - o2.day
                    }
                }
            })
        } else {
            dayColumn.comparatorProperty().set(Comparator.comparingInt<Transaction>({ it.day }))
        }

        sortPolicy = TransactionTableSortPolicy()

        val typeColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Type"))
        typeColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Transaction, Transaction> ->
            val v = if (p.value is SplitTransaction || p.value.groupId == 0)
                p.value
            else
                null

            ReadOnlyObjectWrapper<Transaction>(v)
        }
        typeColumn.setCellFactory { TransactionTypeCell() }

        typeColumn.comparator = Comparator.comparingInt { t: Transaction -> t.transactionType.id }
                .thenComparing(dayColumn.comparator)
        typeColumn.isSortable = true

        val accountFromColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Account.Debited"))
        accountFromColumn.setCellFactory { TransactionDebitedAccountCell() }
        accountFromColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Transaction, Transaction> ->
            val v = if (p.value is SplitTransaction || p.value.groupId == 0)
                p.value
            else
                null

            ReadOnlyObjectWrapper<Transaction>(v)
        }
        accountFromColumn.comparator = Comparator.comparingInt<Transaction>({ it.accountDebitedId })
                .thenComparing(dayColumn.comparator)
        typeColumn.isSortable = true

        val accountCreditedColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Account.Credited"))
        accountCreditedColumn.setCellFactory { TransactionCreditedAccountCell() }
        accountCreditedColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Transaction, Transaction> ->
            ReadOnlyObjectWrapper(p.value)
        }
        accountCreditedColumn.isSortable = false

        val contactColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Payer.Payee"))
        contactColumn.setCellFactory { TransactionContactCell() }
        contactColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Transaction, Transaction> ->
            ReadOnlyObjectWrapper(p.value)
        }
        contactColumn.isSortable = false

        val commentColumn = TableColumn<Transaction, String>(BUNDLE.getString("column.Comment"))
        commentColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Transaction, String> ->
            ReadOnlyObjectWrapper(p.value.comment)
        }
        commentColumn.isSortable = false

        val sumColumn = TableColumn<Transaction, Transaction>(BUNDLE.getString("column.Sum"))
        sumColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Transaction, Transaction> ->
            ReadOnlyObjectWrapper(p.value)
        }
        sumColumn.setCellFactory { TransactionSumCell() }
        sumColumn.setComparator({ t1, t2 -> t1.signedAmount.compareTo(t2.signedAmount) })
        sumColumn.isSortable = true

        val approvedColumn = TableColumn<Transaction, CheckBox>("")

        approvedColumn.setCellValueFactory { p ->
            val cb = CheckBox()

            val value = p.value
            if (value is SplitTransaction) {
                val transactions = getTransactionsByGroup(value.groupId)
                cb.isSelected = transactions.all { it.checked }
                cb.setOnAction { onCheckTransaction(transactions, cb.isSelected) }
            } else {
                cb.isDisable = value.groupId != 0
                cb.isSelected = value.checked
                cb.setOnAction { onCheckTransaction(listOf(value), cb.isSelected) }
            }

            ReadOnlyObjectWrapper(cb)
        }
        approvedColumn.isSortable = false

        columns.setAll(Arrays.asList(dayColumn,
                typeColumn,
                accountFromColumn,
                accountCreditedColumn,
                contactColumn,
                commentColumn,
                sumColumn,
                approvedColumn
        ))

        sortOrder.add(dayColumn)
        dayColumn.sortType = TableColumn.SortType.ASCENDING

        // Column width. Temporary solution, there should be a better option.
        dayColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05))
        typeColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1))
        accountFromColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1))
        accountCreditedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.1))
        contactColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.2))
        commentColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.35))
        sumColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05))
        approvedColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05))

        selectionModel.selectionMode = SelectionMode.MULTIPLE
        val exportMenuItem = MenuItem(BUNDLE.getString("menu.Context.Export")).apply {
            setOnAction { onExportTransactions() }
        }

        contextMenu = ContextMenu(exportMenuItem).apply {
            setOnShowing { onShowingContextMenu() }
        }

        // No full date means we are in the transaction editing enabled mode
        if (!fullDate) {
            // Context menu
            ctxGroupMenuItem.setOnAction { onGroup() }
            ctxUngroupMenuItem.setOnAction { onUngroup() }

            contextMenu.items.addAll(
                    SeparatorMenuItem(),
                    ctxGroupMenuItem,
                    ctxUngroupMenuItem
            )
        }

        with(MoneyDAO) {
            transactions().addListener(MapChangeListener<Int, Transaction> { transactionListener(it) })
            transactionGroups().addListener(MapChangeListener<Int, TransactionGroup> { transactionGroupListener(it) })
        }
    }

    fun listSizeProperty(): ReadOnlyIntegerProperty {
        return listSizeProperty
    }

    private fun onShowingContextMenu() {
        val disableGroup: Boolean
        val disableUngroup: Boolean

        val selectedItems = selectionModel.selectedItems
        if (selectedItems.isEmpty()) {
            disableGroup = true
            disableUngroup = true
        } else {
            if (selectedItems.size == 1) {
                disableGroup = true
                val item = selectedItems[0]
                disableUngroup = item !is SplitTransaction
            } else {
                disableUngroup = true

                // Check if we don't have groups or group members selected
                val hasGroups = selectedItems.any { it.groupId != 0 }
                if (hasGroups) {
                    disableGroup = true
                } else {
                    // All transactions must have same day and debited account
                    val day = selectedItems[0].day
                    val debitedId = selectedItems[0].accountDebitedId

                    disableGroup = selectedItems.any { it.day != day || it.accountDebitedId != debitedId }
                }
            }
        }

        ctxGroupMenuItem.isDisable = disableGroup
        ctxUngroupMenuItem.isDisable = disableUngroup
    }

    fun clear() {
        items.clear()
    }

    private fun addRecords(transactions: List<Transaction>) {
        // Add transactions with no group
        transactions.filter { it.groupId == 0 }
                .forEach { items.add(it) }

        // Add groups
        val transactionsByGroup = transactions
                .filter { it.groupId != 0 }
                .groupBy { it.groupId }

        transactionsByGroup.keys.forEach { groupId ->
            val group = transactionsByGroup[groupId]
            if (group != null && !group.isEmpty()) {
                val split = SplitTransaction(groupId, group)
                items.add(split)
                items.addAll(group)
            }
        }

        listSizeProperty.set(items.size)
    }

    fun setTransactionFilter(filter: Predicate<Transaction>) {
        transactionFilter = filter

        selectionModel.clearSelection()
        clear()

        addRecords(MoneyDAO.getTransactions()
                .filter { filter.test(it) })
        sort()
    }

    val selectedTransactionCount: Int
        get() = selectionModel.selectedIndices.size

    val selectedTransaction: Transaction?
        get() {
            val transaction = selectionModel.selectedItem
            return if (transaction is SplitTransaction)
                null
            else
                transaction
        }

    // Method assumes that all checks are done in onShowingContextMenu method
    private fun onGroup() {
        // List of selected transactions
        val transactions = selectionModel.selectedItems

        val first = transactions[0]

        val tg = TransactionGroup(id = 0,
                day = first.day,
                month = first.month,
                year = first.year,
                expanded = false,
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )

        addGroupConsumer(tg, ArrayList(transactions))
    }

    // Method assumes that all checks are done in onShowingContextMenu method
    private fun onUngroup() {
        ungroupConsumer(getTransactionsByGroup(selectionModel.selectedItem.groupId))
    }

    private fun onCheckTransaction(t: List<Transaction>, checked: Boolean) {
        checkTransactionConsumer(t, checked)
    }

    fun setOnAddGroup(bc: (TransactionGroup, List<Transaction>) -> Unit) {
        addGroupConsumer = bc
    }

    fun setOnDeleteGroup(c: (List<Transaction>) -> Unit) {
        ungroupConsumer = c
    }

    fun setOnCheckTransaction(c: (List<Transaction>, Boolean) -> Unit) {
        checkTransactionConsumer = c
    }

    private fun getTransactionsByGroup(groupId: Int): List<Transaction> {
        return items.filter { it.groupId == groupId && it !is SplitTransaction }
    }

    private fun onExportTransactions() {
        val toExport = selectionModel.selectedItems
                .filterNot { it is SplitTransaction }

        if (!toExport.isEmpty()) {
            val selected = FileChooser().apply {
                title = "Export to file"
                extensionFilters.addAll(
                        FileChooser.ExtensionFilter("XML Files", "*.xml"),
                        FileChooser.ExtensionFilter("All Files", "*.*")
                )
            }.showSaveDialog(null)

            selected?.let {
                CompletableFuture.runAsync {
                    FileOutputStream(selected).use {
                        Export().withTransactions(toExport, withDeps = true)
                                .export(it)
                    }
                }
            }
        }
    }

    private fun transactionListener(change: MapChangeListener.Change<out Int, out Transaction>) {
        val added = if (change.wasAdded() && transactionFilter.test(change.valueAdded))
            change.valueAdded
        else
            null

        val index = if (change.wasRemoved())
            items.indexOf(change.valueRemoved)
        else
            -1

        if (index != -1) {
            if (added != null) {
                items[index] = added
            } else {
                items.removeAt(index)
            }
        } else {
            if (added != null) {
                items.add(added)
            }
        }

        listSizeProperty.set(items.size)

        Platform.runLater {
            sort()
            if (added != null) {
                selectionModel.clearSelection()
                selectionModel.select(added)
            }
        }
    }

    private fun transactionGroupListener(change: MapChangeListener.Change<out Int, out TransactionGroup>) {
        if (change.wasRemoved()) {
            // find appropriate group
            items.filter { it.groupId == change.valueRemoved.id && it is SplitTransaction }
                    .firstOrNull()
                    ?.let { items.remove(it) }
        }

        if (change.wasAdded()) {
            // check if this list has transactions from this group
            val group = items.filter { it.groupId == change.valueAdded.id && it !is SplitTransaction }
            if (!group.isEmpty()) {
                val split = SplitTransaction(change.valueAdded.id, group)
                items.add(split)
                Platform.runLater { this.sort() }
            }
        }

        listSizeProperty.set(items.size)
    }

    companion object {
        private val BUNDLE = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)
    }
}
