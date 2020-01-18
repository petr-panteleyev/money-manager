/*
 * Copyright (c) 2019, 2020, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.cells.LocalDateCell;
import org.panteleyev.money.cells.StatementRow;
import org.panteleyev.money.cells.StatementSumCell;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementRecord;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.MainWindowController.RB;

public class StatementView extends BorderPane {
    private final TableView<StatementRecord> tableView = new TableView<>();
    private Consumer<StatementRecord> newTransactionCallback = x -> { };
    private Consumer<StatementRecord> recordSelectedCallback = x -> { };

    StatementView() {
        tableView.setRowFactory(x -> new StatementRow());

        var w = tableView.widthProperty().subtract(20);
        tableView.getColumns().addAll(List.of(
            newTableColumn(RB, "column.Date", x -> new LocalDateCell<>(),
                StatementRecord::getActual, w.multiply(0.05)),
            newTableColumn(RB, "column.ExecutionDate", x -> new LocalDateCell<>(),
                StatementRecord::getExecution, w.multiply(0.05)),
            newTableColumn(RB, "Description", null, StatementRecord::getDescription, w.multiply(0.5)),
            newTableColumn(RB, "Counterparty", null, StatementRecord::getCounterParty, w.multiply(0.15)),
            newTableColumn(RB, "column.Place", null, StatementRecord::getPlace, w.multiply(0.10)),
            newTableColumn(RB, "Country", null, StatementRecord::getCountry, w.multiply(0.05)),
            newTableColumn(RB, "column.Sum", x -> new StatementSumCell(), w.multiply(0.1))
        ));

        createMenu();

        setCenter(tableView);

        tableView.getSelectionModel().selectedItemProperty().addListener((x, y, newValue) ->
            recordSelectedCallback.accept(newValue));
    }

    Optional<StatementRecord> getSelectedRecord() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    void setSelectedRecord(StatementRecord record) {
        tableView.getSelectionModel().select(record);
    }

    private void createMenu() {
        var menu = new ContextMenu();

        menu.getItems().addAll(newMenuItem(RB, "menu.Edit.Add",
            new KeyCodeCombination(KeyCode.INSERT), x -> onAddTransaction()));

        tableView.setContextMenu(menu);
    }

    void setRecordSelectedCallback(Consumer<StatementRecord> callback) {
        Objects.requireNonNull(callback);
        this.recordSelectedCallback = callback;
    }

    void setNewTransactionCallback(Consumer<StatementRecord> callback) {
        Objects.requireNonNull(callback);
        this.newTransactionCallback = callback;
    }

    public void setStatement(Statement statement) {
        tableView.getItems().clear();
        tableView.getItems().addAll(statement.getRecords());
    }

    void clear() {
        tableView.getItems().clear();
    }

    private void onAddTransaction() {
        getSelectedRecord().ifPresent(st -> newTransactionCallback.accept(st));
    }
}
