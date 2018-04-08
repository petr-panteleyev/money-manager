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

package org.panteleyev.money.statements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.MainWindowController;
import org.panteleyev.money.cells.LocalDateCell;
import org.panteleyev.money.cells.StatementSumCell;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import static org.panteleyev.money.MainWindowController.RB;

public class StatementView extends BorderPane {
    private final TableView<StatementRecord> tableView = new TableView<>();
    private Consumer<StatementRecord> newTransactionCallback = x -> {
    };

    private Consumer<StatementRecord> recordSelectedCallback = x -> {
    };

    StatementView() {
        var actualDateColumn = new TableColumn<StatementRecord, LocalDate>(RB.getString("column.Date"));
        actualDateColumn.setCellFactory(x -> new LocalDateCell<>());
        actualDateColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, LocalDate> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getActual()));

        var executionDateColumn = new TableColumn<StatementRecord, LocalDate>(RB.getString("column.ExecutionDate"));
        executionDateColumn.setCellFactory(x -> new LocalDateCell<>());
        executionDateColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, LocalDate> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getExecution()));

        var descriptionColumn = new TableColumn<StatementRecord, String>(RB.getString("column.Description"));
        descriptionColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        var counterPartyColumn = new TableColumn<StatementRecord, String>(RB.getString("column.Payer.Payee"));
        counterPartyColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getCounterParty()));

        var placeColumn = new TableColumn<StatementRecord, String>(RB.getString("column.Place"));
        placeColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getPlace()));

        var countryColumn = new TableColumn<StatementRecord, String>(RB.getString("column.Country"));
        countryColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getCountry()));

        var amountColumn = new TableColumn<StatementRecord, StatementRecord>(RB.getString("column.Sum"));
        amountColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, StatementRecord> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue()));
        amountColumn.setCellFactory(x -> new StatementSumCell());

        actualDateColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        executionDateColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        descriptionColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.5));
        counterPartyColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.15));
        placeColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.10));
        countryColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        amountColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.10));

        //noinspection unchecked
        tableView.getColumns().addAll(actualDateColumn,
                executionDateColumn,
                descriptionColumn,
                counterPartyColumn,
                placeColumn,
                countryColumn,
                amountColumn
        );

        createMenu();

        setCenter(tableView);

        tableView.getSelectionModel().selectedItemProperty().addListener((x, y, newValue) ->
                recordSelectedCallback.accept(newValue));
    }

    private Optional<StatementRecord> getSelectedRecord() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void createMenu() {
        var menu = new ContextMenu();

        var addMenuItem = new MenuItem(MainWindowController.RB.getString("menu.Edit.Add"));
        addMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.INSERT));
        addMenuItem.setOnAction(event -> onAddTransaction());

        menu.getItems().addAll(addMenuItem);

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
