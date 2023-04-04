/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.cells.PeriodicPaymentContactCell;
import org.panteleyev.money.app.cells.PeriodicPaymentCreditedAccountCell;
import org.panteleyev.money.app.cells.PeriodicPaymentDebitedAccountCell;
import org.panteleyev.money.app.cells.PeriodicPaymentMonthCell;
import org.panteleyev.money.app.cells.PeriodicPaymentNextDateCell;
import org.panteleyev.money.app.cells.PeriodicPaymentSumCell;
import org.panteleyev.money.app.dialogs.PeriodicPaymentDialog;
import org.panteleyev.money.model.PeriodicPayment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;

final class PeriodicPaymentWindowController extends BaseController {
    private final TableView<PeriodicPayment> tableView = new TableView<>(
            cache().getPeriodicPayments().sorted(
                    Comparator.comparing(PeriodicPayment::calculateNextDate)
            )
    );

    private final CrudActionsHolder crudActionsHolder = new CrudActionsHolder(
            this::onCreatePeriodicPayment, this::onEditPeriodicPayment, this::onDeletePeriodicPayment,
            tableView.getSelectionModel().selectedItemProperty().isNull()
    );

    PeriodicPaymentWindowController() {
        setupTableColumns();

        // Toolbox
        var toolBox = hBox(5.0);
        toolBox.setAlignment(Pos.CENTER_LEFT);

        var self = new BorderPane(
                new BorderPane(tableView, toolBox, null, null, null),
                createMainMenu(), null, null, null
        );

        setupWindow(self);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Периодические платежи";
    }

    private MenuBar createMainMenu() {
        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(crudActionsHolder.getDeleteAction())
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));
        return menuBar;
    }

    private void setupTableColumns() {
        var w = tableView.widthProperty().subtract(20);

        tableView.getColumns().setAll(List.of(
                tableColumn("Название",
                        b -> b.withPropertyCallback(PeriodicPayment::name)
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn("Сумма",
                        b -> b.withCellFactory(x -> new PeriodicPaymentSumCell())
                                .withWidthBinding(w.multiply(0.03))
                ),
                tableColumn("Тип",
                        b -> b.withPropertyCallback(p -> Bundles.translate(p.paymentType()))
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableColumn("Периодичность",
                        b -> b.withPropertyCallback(p -> Bundles.translate(p.recurrenceType()))
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableColumn("День",
                        b -> b.withPropertyCallback(PeriodicPayment::dayOfMonth)
                                .withWidthBinding(w.multiply(0.02))
                ),
                tableObjectColumn("Месяц",
                        b -> b.withCellFactory(x -> new PeriodicPaymentMonthCell())
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableObjectColumn("Исходный счет",
                        b -> b.withCellFactory(x -> new PeriodicPaymentDebitedAccountCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn("Счет получателя",
                        b -> b.withCellFactory(x -> new PeriodicPaymentCreditedAccountCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn("Контрагент",
                        b -> b.withCellFactory(x -> new PeriodicPaymentContactCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn("Следующий\nплатёж", b ->
                        b.withCellFactory(x -> new PeriodicPaymentNextDateCell(settings().getPeriodicPaymentDayDelta()))
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn("Комментарий",
                        b -> b.withPropertyCallback(PeriodicPayment::comment)
                                .withWidthBinding(w.multiply(0.35))
                )
        ));
    }

    private Optional<PeriodicPayment> getSelectedPeriodicPayment() {
        return Optional.ofNullable(
                tableView.getSelectionModel().getSelectedItem()
        );
    }

    private void onCreatePeriodicPayment(ActionEvent event) {
        new PeriodicPaymentDialog(this, settings().getDialogCssFileUrl(), null, cache()).showAndWait()
                .ifPresent(payment -> {
                    dao().insertPeriodicPayment(payment);
                    tableView.scrollTo(payment);
                    tableView.getSelectionModel().select(payment);
                });
    }

    private void onEditPeriodicPayment(ActionEvent event) {
        getSelectedPeriodicPayment().flatMap(periodicPayment ->
                        new PeriodicPaymentDialog(
                                this,
                                settings().getDialogCssFileUrl(),
                                periodicPayment, cache()
                        ).showAndWait())
                .ifPresent(payment -> {
                    dao().updatePeriodicPayment(payment);
                    tableView.scrollTo(payment);
                    tableView.getSelectionModel().select(payment);
                });
    }

    private void onDeletePeriodicPayment(ActionEvent event) {
        getSelectedPeriodicPayment().ifPresent(periodicPayment ->
                new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Вы уверены?",
                        ButtonType.OK,
                        ButtonType.CANCEL
                ).showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(b -> dao().deletePeriodicPayment(periodicPayment)));
    }
}
