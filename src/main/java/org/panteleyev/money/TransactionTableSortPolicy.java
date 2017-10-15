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
package org.panteleyev.money;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.panteleyev.money.persistence.SplitTransaction;
import org.panteleyev.money.persistence.Transaction;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class TransactionTableSortPolicy implements Callback<TableView<Transaction>, Boolean> {
    @Override
    public Boolean call(TableView<Transaction> tableView) {
        tableView.getSortOrder().stream()
                .filter(column -> column.getSortType() != null)
                .findFirst()
                .ifPresent(column -> {
                    ObservableList<Transaction> items = tableView.getItems();

                    Comparator<Transaction> comparator =
                            ((TableColumn<Transaction, Transaction>) column).getComparator()
                                    .thenComparingInt(Transaction::getId);

                    if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                        comparator = comparator.reversed();
                    }

                    // Check if table items include at least one split transaction
                    boolean hasSplit = items.stream()
                            .anyMatch(t -> t instanceof SplitTransaction);

                    if (hasSplit) {
                        List<Transaction> sortedItems = items.stream()
                                .filter(t -> t.getGroupId() == 0 || t instanceof SplitTransaction)
                                .sorted(comparator)
                                .map(t -> {
                                    if (t instanceof SplitTransaction) {
                                        return Stream.concat(Stream.of(t),
                                                items.stream()
                                                        .filter(gm -> !(gm instanceof SplitTransaction) && gm
                                                                .getGroupId() == t.getGroupId())
                                                        .sorted(Comparator.comparingInt(Transaction::getId)));
                                    } else {
                                        return Stream.of(t);
                                    }
                                })
                                .reduce(Stream.empty(), Stream::concat)
                                .collect(Collectors.toList());

                        tableView.getItems().setAll(sortedItems);
                    } else {
                        FXCollections.sort(items, comparator);
                    }
                });

        return true;
    }
}
