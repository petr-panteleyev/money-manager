/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
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

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_DELETE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ARE_YOU_SURE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CREDITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEBITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_NEXT_PAYMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_PERIODIC_PAYMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DAY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_MONTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_RECURRENCE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

final class PeriodicPaymentWindowController extends BaseController {
    private final TableView<PeriodicPayment> tableView = new TableView<>(
            cache().getPeriodicPayments().sorted(
                    Comparator.comparing(PeriodicPayment::calculateNextDate)
            )
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
        return UI.getString(I18N_MISC_PERIODIC_PAYMENTS);
    }

    private MenuBar createMainMenu() {
        var disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();
        var menuBar = menuBar(
                newMenu(fxString(UI, I18N_MENU_FILE),
                        menuItem(fxString(UI, I18N_WORD_CLOSE), event -> onClose())
                ),
                newMenu(fxString(UI, I18N_MENU_EDIT),
                        menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                                event -> onNewPeriodicPayment()),
                        menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E,
                                event -> onEditPeriodicPayment(), disableBinding),
                        new SeparatorMenuItem(),
                        menuItem(fxString(UI, I18N_MENU_ITEM_DELETE, ELLIPSIS), SHORTCUT_DELETE,
                                event -> onDeletePeriodicPayment(), disableBinding)
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
                tableColumn(fxString(UI, I18N_WORD_ENTITY_NAME),
                        b -> b.withPropertyCallback(PeriodicPayment::name)
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn(fxString(UI, I18N_WORD_SUM),
                        b -> b.withCellFactory(x -> new PeriodicPaymentSumCell())
                                .withWidthBinding(w.multiply(0.03))
                ),
                tableColumn(fxString(UI, I18N_WORD_TYPE),
                        b -> b.withPropertyCallback(p -> Bundles.translate(p.paymentType()))
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableColumn(fxString(UI, I18N_WORD_RECURRENCE),
                        b -> b.withPropertyCallback(p -> Bundles.translate(p.recurrenceType()))
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableColumn(fxString(UI, I18N_WORD_DAY),
                        b -> b.withPropertyCallback(PeriodicPayment::dayOfMonth)
                                .withWidthBinding(w.multiply(0.02))
                ),
                tableObjectColumn(fxString(UI, I18N_WORD_MONTH),
                        b -> b.withCellFactory(x -> new PeriodicPaymentMonthCell())
                                .withWidthBinding(w.multiply(0.05))
                ),
                tableObjectColumn(fxString(UI, I18N_MISC_DEBITED_ACCOUNT),
                        b -> b.withCellFactory(x -> new PeriodicPaymentDebitedAccountCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn(fxString(UI, I18N_MISC_CREDITED_ACCOUNT),
                        b -> b.withCellFactory(x -> new PeriodicPaymentCreditedAccountCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn(fxString(UI, I18N_WORD_COUNTERPARTY),
                        b -> b.withCellFactory(x -> new PeriodicPaymentContactCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableObjectColumn(fxString(UI, I18N_MISC_NEXT_PAYMENT), b ->
                        b.withCellFactory(x -> new PeriodicPaymentNextDateCell(settings().getPeriodicPaymentDayDelta()))
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn(fxString(UI, I18N_WORD_COMMENT),
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

    private void onNewPeriodicPayment() {
        new PeriodicPaymentDialog(this, settings().getDialogCssFileUrl(), null, cache()).showAndWait()
                .ifPresent(payment -> {
                    dao().insertPeriodicPayment(payment);
                    tableView.scrollTo(payment);
                    tableView.getSelectionModel().select(payment);
                });
    }

    private void onEditPeriodicPayment() {
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

    private void onDeletePeriodicPayment() {
        getSelectedPeriodicPayment().ifPresent(periodicPayment ->
                new Alert(
                        Alert.AlertType.CONFIRMATION,
                        fxString(UI, I18N_MISC_ARE_YOU_SURE),
                        ButtonType.OK,
                        ButtonType.CANCEL
                ).showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(b -> dao().deletePeriodicPayment(periodicPayment)));
    }
}
