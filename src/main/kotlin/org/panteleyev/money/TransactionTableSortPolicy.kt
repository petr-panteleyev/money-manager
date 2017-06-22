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

import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.util.Callback
import org.panteleyev.money.persistence.SplitTransaction
import org.panteleyev.money.persistence.Transaction
import java.util.Comparator

@Suppress("UNCHECKED_CAST")
class TransactionTableSortPolicy : Callback<TableView<Transaction>, Boolean> {
    override fun call(tableView: TableView<Transaction>): Boolean {
        tableView.sortOrder.find { it.sortType != null }?.let { column ->
            val items = tableView.items
            var comparator = (column as TableColumn<Transaction, Transaction>).comparator
                    .thenComparingInt({ it.id })

            if (column.sortType == TableColumn.SortType.DESCENDING) {
                comparator = comparator.reversed()
            }

            if (items.any { it is SplitTransaction }) {
                val sortedItems = items.filter { it.groupId == 0 || it is SplitTransaction }
                        .sortedWith(comparator)
                        .map<Transaction, List<Transaction>> { t ->
                            if (t is SplitTransaction) {
                                val group = items.filter { gm -> gm !is SplitTransaction && gm.groupId == t.groupId }
                                        .sortedWith(Comparator.comparingInt { it.id })
                                val total : MutableList<Transaction> = mutableListOf(t)
                                total.addAll(group)
                                total
                            } else {
                                listOf(t)
                            }
                        }
                        .flatten()

                tableView.items.clear()
                sortedItems.forEach { tableView.items.add(it) }
            } else {
                FXCollections.sort(items, comparator)
            }
        }

        return true
    }
}
