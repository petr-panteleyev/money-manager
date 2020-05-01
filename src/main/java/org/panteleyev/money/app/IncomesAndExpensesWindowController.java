package org.panteleyev.money.app;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
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
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static org.panteleyev.fx.ButtonFactory.newButton;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TreeTableFactory.newTreeItem;
import static org.panteleyev.fx.TreeTableFactory.newTreeTableColumn;
import static org.panteleyev.money.app.Constants.COLON;
import static org.panteleyev.money.app.Constants.ELLIPSIS;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Styles.GREEN_TEXT;
import static org.panteleyev.money.app.Styles.RED_TEXT;
import static org.panteleyev.money.app.TemplateEngine.templateEngine;
import static org.panteleyev.money.persistence.DataCache.cache;

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
            getStyleClass().removeAll(RED_TEXT, GREEN_TEXT);
            setAlignment(Pos.CENTER_RIGHT);
            setText("");

            if (empty || treeNode == null || treeNode.getAmount() == null) {
                return;
            }

            setText(treeNode.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            if (treeNode.isExpenseRootNode()) {
                getStyleClass().add(RED_TEXT);
            }
            if (treeNode.isIncomeRootNode()) {
                getStyleClass().add(GREEN_TEXT);
            }
        }
    }

    private final TreeItem<TreeNode> root = new TreeItem<>();
    private final TreeTableView<TreeNode> reportTable = new TreeTableView<>(root);
    private final TransactionFilterBox filterBox = new TransactionFilterBox(true, true);

    private final Label incomeValueText = new Label();
    private final Label expenseValueText = new Label();
    private final Label balanceValueText = new Label();

    private final TreeItem<TreeNode> expenseRoot = newTreeItem(true);
    private final TreeItem<TreeNode> incomeRoot = newTreeItem(true);

    public IncomesAndExpensesWindowController() {
        setupReportTable();

        incomeValueText.getStyleClass().add(GREEN_TEXT);
        expenseValueText.getStyleClass().add(RED_TEXT);

        var toolBar = new HBox(5.0,
            filterBox,
            newButton(RB, "Reset_Filter", x -> filterBox.reset())
        );

        var statusBar = createStatusBar();

        var root = new BorderPane(new BorderPane(reportTable, toolBar, null, statusBar, null),
            createMenuBar(), null, null, null);

        BorderPane.setMargin(toolBar, new Insets(5.0, 5.0, 5.0, 5.0));
        BorderPane.setMargin(statusBar, new Insets(5.0, 5.0, 5.0, 5.0));

        filterBox.predicateProperty().addListener((x, y, newValue) -> onRefresh());

        filterBox.reset();

        setupWindow(root);
        Options.loadStageDimensions(getClass(), getStage());

//        onRefresh();
    }

    @Override
    public String getTitle() {
        return RB.getString("Incomes_and_Expenses");
    }

    private MenuBar createMenuBar() {
        return newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Report", ELLIPSIS, event -> onReport()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "Close", event -> onClose())),
            createWindowMenu(),
            createHelpMenu()
        );
    }

    private Node createStatusBar() {
        return new HBox(5.0,
            newLabel(RB, "Expenses", COLON),
            expenseValueText,
            newLabel(RB, "Incomes", COLON),
            incomeValueText,
            newLabel(RB, "Balance", COLON),
            balanceValueText);
    }

    private void onRefresh() {
        expenseRoot.getChildren().clear();
        var expenseSum = calculateTotal(CategoryType.EXPENSES, expenseRoot);
        expenseRoot.setValue(new ExpenseRootNode(RB.getString("Expenses"), expenseSum));

        incomeRoot.getChildren().clear();
        var incomeSum = calculateTotal(CategoryType.INCOMES, incomeRoot);
        incomeRoot.setValue(new IncomeRootNode(RB.getString("Incomes"), incomeSum));

        expenseValueText.setText(expenseSum.setScale(2, RoundingMode.HALF_UP).toString());
        incomeValueText.setText(incomeSum.setScale(2, RoundingMode.HALF_UP).toString());

        var balance = incomeSum.subtract(expenseSum);
        balanceValueText.getStyleClass().removeAll(RED_TEXT, GREEN_TEXT);
        if (balance.signum() < 0) {
            balanceValueText.getStyleClass().add(RED_TEXT);
        } else {
            balanceValueText.getStyleClass().add(GREEN_TEXT);
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
                    (x, sum) -> sum == null ? t.amount() : sum.add(t.amount()));
                accSum.compute(accUuidFunc.apply(t),
                    (x, sum) -> sum == null ? t.amount() : sum.add(t.amount()));
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
                    newTreeItem(new TreeNode(category.name(), catSum.get(category.uuid())), true);
                root.getChildren().add(categoryRoot);

                categoryMapEntry.getValue().entrySet().stream()
                    .sorted(Comparator.comparing(accountMapEntry -> accountMapEntry.getKey().name()))
                    .forEach(accountMapEntry -> {
                        var account = accountMapEntry.getKey();
                        var accountRoot =
                            newTreeItem(new TreeNode(account.name(), accSum.get(account.uuid())), false);
                        categoryRoot.getChildren().add(accountRoot);

                        accountMapEntry.getValue().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(contactMapEntry -> {
                                var name = contactMapEntry.getKey();
                                var sum = contactMapEntry.getValue().stream()
                                    .map(Transaction::amount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                                accountRoot.getChildren().add(newTreeItem(new TreeNode(name, sum), false));
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
            newTreeTableColumn("", x -> new NodeTextCell(), w.multiply(0.85)),
            newTreeTableColumn("", x -> new NodeAmountCell(), w.multiply(0.15))
        ));
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(RB.getString("Report"));
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("IncomesAndExpenses"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        var dataModel = new HashMap<String, Object>();
        dataModel.put("expensesSum", expenseValueText.getText());
        dataModel.put("incomesSum", incomeValueText.getText());
        dataModel.put("balanceSum", balanceValueText.getText());

        dataModel.put("expenses", expenseRoot.getChildren().stream()
            .map(IncomesAndExpensesWindowController::getItemModel)
            .collect(Collectors.toList())
        );

        dataModel.put("incomes", incomeRoot.getChildren().stream()
            .map(IncomesAndExpensesWindowController::getItemModel)
            .collect(Collectors.toList())
        );

        try (var w = new FileWriter(selected)) {
            templateEngine().process(TemplateEngine.Template.INCOMES_AND_EXPENSES, dataModel, w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Map<String, Object> getItemModel(TreeItem<TreeNode> item) {
        var map = new LinkedHashMap<String, Object>();
        map.put("text", item.getValue().getText());
        map.put("amount", item.getValue().getAmount()
            .setScale(2, RoundingMode.HALF_UP).toString());

        var items = item.getChildren().stream()
            .map(IncomesAndExpensesWindowController::getItemModel)
            .collect(Collectors.toList());

        if (!items.isEmpty()) {
            map.put("items", items);
        }

        return map;
    }
}
