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

package org.panteleyev.money.statements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.cells.LocalDateCell;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;
import static org.panteleyev.money.MainWindowController.RB;

class StatementView extends BorderPane {
    private final TableView<StatementRecord> tableView = new TableView<>();

    private Consumer<StatementRecord> recordSelectedCallback = x -> {
    };

    StatementView() {
        TableColumn<StatementRecord, LocalDate> actualDateColumn = new TableColumn<>(RB.getString("column.Date"));
        actualDateColumn.setCellFactory(x -> new LocalDateCell<>());
        actualDateColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, LocalDate> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getActual()));

        TableColumn<StatementRecord, LocalDate> executionDateColumn = new TableColumn<>(RB.getString("column.ExecutionDate"));
        executionDateColumn.setCellFactory(x -> new LocalDateCell<>());
        executionDateColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, LocalDate> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getExecution()));

        TableColumn<StatementRecord, String> descriptionColumn = new TableColumn<>(RB.getString("column.Description"));
        descriptionColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        TableColumn<StatementRecord, String> counterPartyColumn = new TableColumn<>(RB.getString("column.Payer.Payee"));
        counterPartyColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getCounterParty()));

        TableColumn<StatementRecord, String> placeColumn = new TableColumn<>(RB.getString("column.Place"));
        placeColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getPlace()));

        TableColumn<StatementRecord, BigDecimal> amountColumn = new TableColumn<>(RB.getString("column.Sum"));
        amountColumn.setCellValueFactory((TableColumn.CellDataFeatures<StatementRecord, BigDecimal> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getAmountDecimal().orElse(BigDecimal.ZERO)));


        actualDateColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        executionDateColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.05));
        descriptionColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.5));
        counterPartyColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.15));
        placeColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.15));
        amountColumn.prefWidthProperty().bind(widthProperty().subtract(20).multiply(0.10));

        //noinspection unchecked
        tableView.getColumns().addAll(actualDateColumn,
                executionDateColumn,
                descriptionColumn,
                counterPartyColumn,
                placeColumn,
                amountColumn
        );

        setCenter(tableView);

        tableView.getSelectionModel().selectedItemProperty().addListener((x, y, newValue) -> {
            recordSelectedCallback.accept(newValue);
        });
    }

    void setRecordSelectedCallback(Consumer<StatementRecord> callback) {
        this.recordSelectedCallback = callback;
    }

    void setStatement(Statement statement) {
        tableView.getItems().clear();
        tableView.getItems().addAll(statement.getRecords());
    }
}
