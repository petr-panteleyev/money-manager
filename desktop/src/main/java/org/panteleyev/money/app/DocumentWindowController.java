/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.cells.DocumentContactNameCell;
import org.panteleyev.money.app.filters.ContactFilterBox;
import org.panteleyev.money.app.filters.DocumentTypeFilterBox;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.RawStatementData;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.money.MoneyApplication.showDocument;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_O;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_R;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_S;

public class DocumentWindowController extends BaseController {
    private final MoneyRecord documentOwner;

    private final FilteredList<MoneyDocument> filteredList = cache().getDocuments().filtered(x -> true);
    private final SortedList<MoneyDocument> sortedList =
            filteredList.sorted(Comparator.comparing(MoneyDocument::fileName));
    private final TableView<MoneyDocument> table = new TableView<>(sortedList);

    public DocumentWindowController(MoneyRecord documentOwner) {
        this.documentOwner = documentOwner;

        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateDocument, this::onEditDocument, event -> {},
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        var contactFilterBox = new ContactFilterBox();
        var documentTypeFilterBox = new DocumentTypeFilterBox();

        if (documentOwner != null) {
            filteredList.predicateProperty().bind(
                    PredicateProperty.and(List.of(
                            contactFilterBox.documentPredicateProperty(),
                            documentTypeFilterBox.predicateProperty(),
                            new PredicateProperty<>(doc -> doc.ownerUuid().equals(documentOwner.uuid()))
                    ))
            );
        } else {
            filteredList.predicateProperty().bind(
                    PredicateProperty.and(List.of(
                            contactFilterBox.documentPredicateProperty(),
                            documentTypeFilterBox.predicateProperty()
                    ))
            );
        }

        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        menuItem("Открыть", SHORTCUT_O, event -> onOpenDocument()),
                        menuItem("Сохранить...", SHORTCUT_S, event -> onDownload()),
                        new SeparatorMenuItem(),
                        menuItem("Обработать выписку", SHORTCUT_R,
                                event -> onOpenStatement())
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createMenuItem(crudActionsHolder.getCreateAction()),
                createMenuItem(crudActionsHolder.getUpdateAction()),
                new SeparatorMenuItem(),
                menuItem("Открыть", SHORTCUT_O, event -> onOpenDocument()),
                menuItem("Сохранить...", SHORTCUT_S, event -> onDownload()),
                new SeparatorMenuItem(),
                menuItem("Обработать выписку", event -> onOpenStatement())
        ));

        // Toolbar
        var toolBar = hBox(5.0, contactFilterBox.getTextField(), documentTypeFilterBox.getNode());
        toolBar.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(toolBar, new Insets(5.0, 5.0, 5.0, 5.0));

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn("Контрагент", b ->
                        b.withPropertyCallback(MoneyDocument::contactUuid)
                                .withCellFactory(x -> new DocumentContactNameCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableColumn("Файл", b ->
                        b.withPropertyCallback(MoneyDocument::fileName)
                                .withWidthBinding(w.multiply(0.2))
                ),
                tableColumn("Тип", b ->
                        b.withPropertyCallback(doc -> Bundles.translate(doc.documentType()))
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableColumn("Размер", b ->
                        b.withPropertyCallback(doc -> Integer.toString(doc.size()))
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn("Описание", b ->
                        b.withPropertyCallback(MoneyDocument::description)
                                .withWidthBinding(w.multiply(0.4))
                ),
                tableColumn("Дата", b ->
                        b.withPropertyCallback(MoneyDocument::date)
                                .withWidthBinding(w.multiply(0.15))
                )
        ));

        table.setOnDragOver(this::onDragOver);
        table.setOnDragDropped(this::onDragDropped);

        var root = new BorderPane(
                new BorderPane(table, toolBar, null, null, null),
                menuBar, null, null, null);
        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    public boolean thisOwner(MoneyRecord documentOwner) {
        return this.documentOwner == documentOwner;
    }

    @Override
    public String getTitle() {
        var name = "Документы";
        if (documentOwner instanceof Named named) {
            name = name + " - " + named.name();
        } else if (documentOwner instanceof Transaction transaction) {
            name = name + " - " + transaction.uuid();
        }
        return name;
    }

    private Optional<MoneyDocument> getSelectedDocument() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    public void onCreateDocument(ActionEvent event) {
        var d = new DocumentDialog(this, documentOwner, settings().getDialogCssFileUrl(), null);
        d.showAndWait().ifPresent(documents -> {
            for (DocumentWithBytes(MoneyDocument document, byte[] bytes) : documents) {
                dao().insertDocument(document, bytes);
            }
        });
    }

    private void onEditDocument(ActionEvent event) {
        getSelectedDocument()
                .flatMap(document -> new DocumentDialog(this, documentOwner, settings().getDialogCssFileUrl(), document)
                        .showAndWait())
                .ifPresent(updated ->  {
                    for (var doc: updated) {
                        dao().updateDocument(doc.document());
                    }
                });
    }

    private void onOpenDocument() {
        getSelectedDocument().ifPresent(document -> {
            var bytes = dao().getDocumentBytes(document);
            try {
                // Calculate extension
                var suffix = ".tmp";
                var dotIndex = document.fileName().lastIndexOf(".");
                if (dotIndex != -1) {
                    suffix = document.fileName().substring(dotIndex);
                }

                var tempFile = File.createTempFile("money-manager", suffix);
                Files.write(tempFile.toPath(), bytes);
                showDocument(tempFile.getAbsolutePath());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void onDownload() {
        getSelectedDocument().ifPresent(document -> {
            var d = new FileChooser();
            d.setInitialFileName(document.fileName());
            var selected = d.showSaveDialog(getStage());
            if (selected != null) {
                var bytes = dao().getDocumentBytes(document);
                try {
                    Files.write(selected.toPath(), bytes);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        });
    }

    private void onDragOver(DragEvent event) {
        var dragBoard = event.getDragboard();
        if (dragBoard.hasFiles() && dragBoard.getFiles().size() > 0) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        var success = false;
        var dragBoard = event.getDragboard();
        if (dragBoard.hasFiles()) {
            var files = dragBoard.getFiles();
            if (files.size() > 0) {
                success = true;
                var d = new DocumentDialog(this, documentOwner, settings().getDialogCssFileUrl(), null, files);
                d.showAndWait().ifPresent(documents -> {
                    for (DocumentWithBytes(MoneyDocument document, byte[] bytes) : documents) {
                        dao().insertDocument(document, bytes);
                    }
                });
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void onOpenStatement() {
        getSelectedDocument().ifPresent(doc -> {
            if (doc.documentType() != DocumentType.STATEMENT) {
                return;
            }

            var data = new RawStatementData(dao().getDocumentBytes(doc));
            var controller = getController(StatementWindowController.class);
            controller.setStatement(doc.fileName(), data);
        });
    }
}
