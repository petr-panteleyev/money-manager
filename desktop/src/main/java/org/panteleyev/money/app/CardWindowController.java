/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.cells.CardAccountCell;
import org.panteleyev.money.app.cells.CardCategoryCell;
import org.panteleyev.money.app.cells.CardExpirationDateCell;
import org.panteleyev.money.app.cells.CardNumberCell;
import org.panteleyev.money.app.filters.CardNumberFilterBox;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.MenuFactory.checkMenuItem;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Predicates.activeCard;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_H;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

final class CardWindowController extends BaseController {
    private final CardNumberFilterBox cardNumberFilterBox = new CardNumberFilterBox();
    private final PredicateProperty<Card> showDeactivatedCards =
            new PredicateProperty<>(settings().getShowDeactivatedCards() ? a -> true : activeCard(true));

    private final PredicateProperty<Card> filterProperty =
            PredicateProperty.and(List.of(
                    cardNumberFilterBox.predicateProperty(),
                    showDeactivatedCards
            ));

    private final FilteredList<Card> filteredList = cache().getCards().filtered(filterProperty.get());
    private final SortedList<Card> sortedList = filteredList.sorted();
    private final TableView<Card> table = new TableView<>(sortedList);

    CardWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCard, this::onEditCard, e -> {},
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        setupTable();

        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(searchAction(this::onSearch))
                ),
                newMenu("Вид",
                        checkMenuItem("Показывать неактивные карты",
                                settings().getShowDeactivatedCards(), SHORTCUT_H,
                                event -> {
                                    var selected = ((CheckMenuItem) event.getSource()).isSelected();
                                    settings().update(opt -> opt.setShowDeactivatedCards(selected));
                                    showDeactivatedCards.set(selected ? a -> true : activeCard(true));
                                }
                        ),
                        menuItem("Сбросить фильтр", SHORTCUT_ALT_C, event -> resetFilter())),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        var pane = new BorderPane(table,
                fxNode(
                        hBox(List.of(cardNumberFilterBox.getTextField() /*, typeBox*/), b -> {
                            b.setSpacing(BIG_SPACING);
                            b.setAlignment(Pos.CENTER_LEFT);
                        }),
                        b -> BorderPane.setMargin(b, new Insets(5.0, 5.0, 5.0, 5.0))
                ),
                null, null, null);

        var self = new BorderPane(pane, menuBar, null, null, null);
        self.setPrefSize(600.0, 400.0);

        filteredList.predicateProperty().bind(filterProperty);
        sortedList.comparatorProperty().bind(table.comparatorProperty());

        setupWindow(self);
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Card> getSelectedCard() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return "Карты";
    }

    private void setupTable() {
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableObjectColumn("Номер", b ->
                        b.withCellFactory(x -> new CardNumberCell()).withWidthBinding(w.multiply(0.2))
                                .withComparator(Comparator.comparing(Card::number))),
                tableObjectColumn("Категория", b ->
                        b.withCellFactory(x -> new CardCategoryCell()).withWidthBinding(w.multiply(0.1))
                                .withComparator((c1, c2) -> {
                                            var name1 = cache().getAccount(c1.accountUuid())
                                                    .map(Account::categoryUuid)
                                                    .flatMap(uuid -> cache().getCategory(uuid))
                                                    .map(Category::name)
                                                    .orElse("");
                                            var name2 = cache().getAccount(c2.accountUuid())
                                                    .map(Account::categoryUuid)
                                                    .flatMap(uuid -> cache().getCategory(uuid))
                                                    .map(Category::name)
                                                    .orElse("");
                                            return name1.compareTo(name2);
                                        }
                                )),
                tableObjectColumn("Счёт", b ->
                        b.withCellFactory(x -> new CardAccountCell()).withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("До", b ->
                        b.withCellFactory(x -> new CardExpirationDateCell(settings().getAccountClosingDayDelta()))
                                .withWidthBinding(w.multiply(0.1))
                                .withComparator(Comparator.comparing(Card::expiration))),
                tableColumn("Комментарий", b ->
                        b.withPropertyCallback(Card::comment).withWidthBinding(w.multiply(0.4)))
        ));
    }

    private void resetFilter() {
        cardNumberFilterBox.getTextField().setText("");
    }

    private void onCreateCard(ActionEvent ignored) {
        new CardDialog(this, null).showAndWait()
                .ifPresent(card -> dao().insertCard(card));
    }

    private void onEditCard(ActionEvent ignored) {
        getSelectedCard().flatMap(selected -> new CardDialog(this, selected).showAndWait())
                .ifPresent(card -> dao().updateCard(card));
    }

    private void onSearch(ActionEvent ignored) {
        cardNumberFilterBox.getTextField().requestFocus();
    }
}
