/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.panteleyev.money.AccountFilterSelectionBox;
import org.panteleyev.money.TransactionFilterBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.AccountFilter;
import org.panteleyev.money.persistence.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class ChartsTab extends BorderPane {
    private static final int PIE_CHART_SIZE = 10;  // meaningful items

    private final AccountFilterSelectionBox selectionBox = new AccountFilterSelectionBox();
    private final TransactionFilterBox transactionFilterBox = new TransactionFilterBox(true, true);
    private final PieChart pieChart = new PieChart();

    public ChartsTab() {
        HBox topPanel = new HBox(5.0);
        Button button = new Button(RB.getString("button.Refresh"));
        button.setOnAction(event -> updateChart());

        topPanel.getChildren().addAll(selectionBox, transactionFilterBox, button);

        setTop(topPanel);
        BorderPane.setMargin(topPanel, new Insets(5.0, 5.0, 5.0, 5.0));

        setCenter(pieChart);

        pieChart.legendVisibleProperty().set(false);
    }

    private void updateChart() {
        Predicate<Account> accountFilter = selectionBox.getAccountFilter()
                .and(AccountFilter.ENABLED.predicate());

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        Predicate<Transaction> transactionFilter = transactionFilterBox.getTransactionFilter();

        List<Pair<String, BigDecimal>> list = getDao().getAccounts(accountFilter).stream()
                .map(a -> new Pair<>(a.getName(), a.calculateBalance(true, transactionFilter).abs()))
                .filter(p -> BigDecimal.ZERO.compareTo(p.getValue()) != 0)
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .collect(Collectors.toList());

        list.stream().limit(PIE_CHART_SIZE)
                .forEach(p -> data.add(new PieChart.Data(p.getKey(), p.getValue().doubleValue())));

        list.stream().skip(PIE_CHART_SIZE)
                .map(Pair::getValue)
                .reduce(BigDecimal::add)
                .ifPresent(t -> data.add(new PieChart.Data(RB.getString("pie.Chart.Other"), t.doubleValue())));

        pieChart.setData(data);
    }
}
