/*
 Copyright © 2020-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.dialogs.ReportFileDialog;
import org.panteleyev.money.app.filters.TransactionFilterBox;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.TreeTableFactory.treeItem;
import static org.panteleyev.fx.TreeTableFactory.treeTableColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.Styles.CREDIT;
import static org.panteleyev.money.app.Styles.DEBIT;
import static org.panteleyev.money.app.TemplateEngine.templateEngine;

class IncomesAndExpensesWindowController extends BaseController {
    private static class TreeNode {
        private final String text;
        private final BigDecimal amount;

        public TreeNode(String text, BigDecimal amount) {
            this.text = text;
            this.amount = amount;
        }

        public String getText() {
            return text;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public boolean isExpenseRootNode() {
            return false;
        }

        public boolean isIncomeRootNode() {
            return false;
        }
    }

    private static class ExpenseRootNode extends TreeNode {
        public ExpenseRootNode(String text, BigDecimal amount) {
            super(text, amount);
        }

        @Override
        public boolean isExpenseRootNode() {
            return true;
        }
    }

    private static class IncomeRootNode extends TreeNode {
        public IncomeRootNode(String text, BigDecimal amount) {
            super(text, amount);
        }

        @Override
        public boolean isIncomeRootNode() {
            return true;
        }
    }

    private static class NodeTextCell extends TreeTableCell<TreeNode, TreeNode> {
        @Override
        protected void updateItem(TreeNode treeNode, boolean empty) {
            super.updateItem(treeNode, empty);
            setText("");
            if (empty || treeNode == null) {
                return;
            }
            setText(treeNode.getText());
        }
    }

    private static class NodeAmountCell extends TreeTableCell<TreeNode, TreeNode> {
        @Override
        protected void updateItem(TreeNode treeNode, boolean empty) {
            super.updateItem(treeNode, empty);
            getStyleClass().removeAll(DEBIT, CREDIT);
            setAlignment(Pos.CENTER_RIGHT);
            setText("");

            if (empty || treeNode == null || treeNode.getAmount() == null) {
                return;
            }

            setText(treeNode.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            if (treeNode.isExpenseRootNode()) {
                getStyleClass().add(DEBIT);
            }
            if (treeNode.isIncomeRootNode()) {
                getStyleClass().add(CREDIT);
            }
        }
    }

    private final TreeItem<TreeNode> root = new TreeItem<>();
    private final TreeTableView<TreeNode> reportTable = new TreeTableView<>(root);
    private final TransactionFilterBox filterBox = new TransactionFilterBox(true, true);

    private final Label incomeValueText = new Label();
    private final Label expenseValueText = new Label();
    private final Label balanceValueText = new Label();

    private final TreeItem<TreeNode> expenseRoot = treeItem(true);
    private final TreeItem<TreeNode> incomeRoot = treeItem(true);

    public IncomesAndExpensesWindowController() {
        setupReportTable();

        incomeValueText.getStyleClass().add(DEBIT);
        expenseValueText.getStyleClass().add(CREDIT);

        var toolBar = hBox(5.0,
                filterBox,
                button("Сбросить фильтр", _ -> filterBox.reset())
        );

        var statusBar = createStatusBar();

        var root = new BorderPane(new BorderPane(reportTable, toolBar, null, statusBar, null),
                createMenuBar(), null, null, null);

        BorderPane.setMargin(toolBar, BIG_INSETS);
        BorderPane.setMargin(statusBar, BIG_INSETS);

        filterBox.predicateProperty().addListener((_, _, _) -> onRefresh());

        filterBox.reset();

        setupWindow(root);
        settings().loadStageDimensions(this);

//        onRefresh();
    }

    @Override
    public String getTitle() {
        return "Доходы и расходы";
    }

    private MenuBar createMenuBar() {
        var menuBar = menuBar(
                menu("Файл",
                        menuItem("Отчет...", _ -> onReport()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        return menuBar;
    }

    private Node createStatusBar() {
        return hBox(5.0,
                label("Расходы:"), expenseValueText,
                label("Доходы:"), incomeValueText,
                label("Баланс:"), balanceValueText
        );
    }

    private void onRefresh() {
        expenseRoot.getChildren().clear();
        var expenseSum = calculateTotal(CategoryType.EXPENSES, expenseRoot);
        expenseRoot.setValue(new ExpenseRootNode("Расходы", expenseSum));

        incomeRoot.getChildren().clear();
        var incomeSum = calculateTotal(CategoryType.INCOMES, incomeRoot);
        incomeRoot.setValue(new IncomeRootNode("Доходы", incomeSum));

        expenseValueText.setText(expenseSum.setScale(2, RoundingMode.HALF_UP).toString());
        incomeValueText.setText(incomeSum.setScale(2, RoundingMode.HALF_UP).toString());

        var balance = incomeSum.subtract(expenseSum);
        balanceValueText.getStyleClass().removeAll(DEBIT, CREDIT);
        if (balance.signum() < 0) {
            balanceValueText.getStyleClass().add(DEBIT);
        } else {
            balanceValueText.getStyleClass().add(CREDIT);
        }
        balanceValueText.setText(balance.setScale(2, RoundingMode.HALF_UP).toString());

        reportTable.setRoot(root);
    }

    private BigDecimal calculateTotal(CategoryType type, TreeItem<TreeNode> root) {
        var catSum = new HashMap<UUID, BigDecimal>();
        var accSum = new HashMap<UUID, BigDecimal>();

        Function<Transaction, UUID> catUuidFunc = type == CategoryType.EXPENSES ?
                Transaction::accountCreditedCategoryUuid : Transaction::accountDebitedCategoryUuid;
        Function<Transaction, UUID> accUuidFunc = type == CategoryType.EXPENSES ?
                Transaction::accountCreditedUuid : Transaction::accountDebitedUuid;

        cache().getTransactions().stream()
                .filter(filterBox.predicateProperty().get())
                .filter(t -> cache().getCategory(catUuidFunc.apply(t))
                        .map(Category::type).orElseThrow() == type)
                .peek(t -> {
                    // TODO: currency conversion rates
                    catSum.compute(catUuidFunc.apply(t),
                            (_, sum) -> sum == null ? t.amount() : sum.add(t.amount()));
                    accSum.compute(accUuidFunc.apply(t),
                            (_, sum) -> sum == null ? t.amount() : sum.add(t.amount()));
                })
                .collect(groupingBy(t -> cache().getCategory(catUuidFunc.apply(t)).orElseThrow(),
                        groupingBy(t -> cache().getAccount(accUuidFunc.apply(t)).orElseThrow(),
                                groupingBy(t -> Optional.ofNullable(t.contactUuid()).flatMap(id -> cache().getContact(id))
                                        .map(Contact::name).orElse("")))))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .forEach(categoryMapEntry -> {
                    var category = categoryMapEntry.getKey();
                    var categoryRoot =
                            treeItem(new TreeNode(category.name(), catSum.get(category.uuid())), true);
                    root.getChildren().add(categoryRoot);

                    categoryMapEntry.getValue().entrySet().stream()
                            .sorted(Comparator.comparing(accountMapEntry -> accountMapEntry.getKey().name()))
                            .forEach(accountMapEntry -> {
                                var account = accountMapEntry.getKey();
                                var accountRoot =
                                        treeItem(new TreeNode(account.name(), accSum.get(account.uuid())), false);
                                categoryRoot.getChildren().add(accountRoot);

                                accountMapEntry.getValue().entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .forEach(contactMapEntry -> {
                                            var name = contactMapEntry.getKey();
                                            var sum = contactMapEntry.getValue().stream()
                                                    .map(Transaction::amount)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                                            accountRoot.getChildren().add(treeItem(new TreeNode(name, sum), false));
                                        });
                            });
                });

        return catSum.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void setupReportTable() {
        root.getChildren().setAll(List.of(expenseRoot, incomeRoot));
        reportTable.setShowRoot(false);

        var w = reportTable.widthProperty().subtract(20);
        reportTable.getColumns().addAll(List.of(
                treeTableColumn("", _ -> new NodeTextCell(), w.multiply(0.85)),
                treeTableColumn("", _ -> new NodeAmountCell(), w.multiply(0.15))
        ));
    }

    private void onReport() {
        new ReportFileDialog().show(getStage(), ReportType.INCOMES_AND_EXPENSES).ifPresent(selected -> {
            var dataModel = new HashMap<String, Object>();
            dataModel.put("expensesSum", expenseValueText.getText());
            dataModel.put("incomesSum", incomeValueText.getText());
            dataModel.put("balanceSum", balanceValueText.getText());

            dataModel.put("expenses", expenseRoot.getChildren().stream()
                    .map(IncomesAndExpensesWindowController::getItemModel)
                    .toList()
            );

            dataModel.put("incomes", incomeRoot.getChildren().stream()
                    .map(IncomesAndExpensesWindowController::getItemModel)
                    .toList()
            );

            try (var w = new FileWriter(selected)) {
                templateEngine().process(TemplateEngine.Template.INCOMES_AND_EXPENSES, dataModel, w);
                settings().update(opt -> opt.setLastReportDir(selected.getParent()));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

    }

    private static Map<String, Object> getItemModel(TreeItem<TreeNode> item) {
        var map = new LinkedHashMap<String, Object>();
        map.put("text", item.getValue().getText());
        map.put("amount", item.getValue().getAmount()
                .setScale(2, RoundingMode.HALF_UP).toString());

        var items = item.getChildren().stream()
                .map(IncomesAndExpensesWindowController::getItemModel)
                .toList();

        if (!items.isEmpty()) {
            map.put("items", items);
        }

        return map;
    }
}
