/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.TabFactory.tab;
import static org.panteleyev.money.app.Constants.FILTER_RAIF_ONLINE_BROKER_DEALS;
import static org.panteleyev.money.app.Constants.FILTER_SBER_ONLINE_BROKER_DEALS;
import static org.panteleyev.money.app.Constants.FILTER_SBER_ONLINE_BROKER_DEALS_HTML;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;

public class InvestmentWindowController extends BaseController {
    public InvestmentWindowController() {

        var tabPane = new TabPane();

        var self = new BorderPane(
                tabPane,
                createMenu(), null, null, null
        );


        tabPane.getTabs().addAll(
                tab("Сделки", new InvestmentDealsPane()),
                tab("Портфель", new InvestmentSummaryPane())
        );

        setupWindow(self);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Инвестиции";
    }

    private MenuBar createMenu() {
        return menuBar(
                menu("Файл",
                        menuItem("Загрузить сделки", _ -> onLoadInvestmentDeals()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)
                ),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private void onLoadInvestmentDeals() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Сделки");
        fileChooser.getExtensionFilters().addAll(
//                FILTER_SBER_ONLINE_BROKER_DEALS,
                FILTER_SBER_ONLINE_BROKER_DEALS_HTML,
                FILTER_RAIF_ONLINE_BROKER_DEALS);

        var selectedFiles = fileChooser.showOpenMultipleDialog(this.getStage());
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        for (var selected : selectedFiles) {
            try (var inputStream = new FileInputStream(selected)) {
                var filter = fileChooser.getSelectedExtensionFilter();

                List<InvestmentDeal> investments;

                if (Objects.equals(filter, FILTER_SBER_ONLINE_BROKER_DEALS)) {
                    investments = new SberbankBrokerReportParser().parse(inputStream);
                } else if (Objects.equals(filter, FILTER_SBER_ONLINE_BROKER_DEALS_HTML)) {
                    investments = new SberbankBrokerHtmlReportParser().parse(inputStream);
                } else if (Objects.equals(filter, FILTER_RAIF_ONLINE_BROKER_DEALS)) {
                    var fileName = selected.getName();
                    var underscoreIndex = fileName.indexOf('_');

                    var accountName = "";
                    if (underscoreIndex != -1) {
                        accountName = fileName.substring(0, underscoreIndex);
                    }

                    investments = new RaiffeisenBrokerReportParser().parse(accountName, inputStream);
                } else {
                    investments = List.of();
                }
                dao().insertInvestments(investments);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
