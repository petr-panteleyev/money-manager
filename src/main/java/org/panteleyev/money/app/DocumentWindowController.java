/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.panteleyev.money.app.cells.DocumentContactNameCell;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.MoneyApplication.showDocument;
import static org.panteleyev.money.app.Constants.ALL_TYPES_STRING;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_O;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_S;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DOCUMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_OPEN;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SAVE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SIZE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

public class DocumentWindowController extends BaseController {
    private final MoneyRecord documentOwner;
    private final ComboBox<DocumentType> typeBox = comboBox(DocumentType.values(),
            b -> b.withDefaultString(ALL_TYPES_STRING)
                    .withStringConverter(Bundles::translate)
    );

    private final FilteredList<MoneyDocument> filteredList = cache().getDocuments().filtered(x -> true);
    private final SortedList<MoneyDocument> sortedList =
            filteredList.sorted(Comparator.comparing(MoneyDocument::fileName));
    private final TableView<MoneyDocument> table = new TableView<>(sortedList);

    public DocumentWindowController(MoneyRecord documentOwner) {
        this.documentOwner = documentOwner;
        if (documentOwner != null) {
            filteredList.setPredicate(doc -> doc.ownerUuid().equals(documentOwner.uuid()));
        }

        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();
        var menuBar = menuBar(
                newMenu(fxString(UI, I18N_MENU_FILE),
                        menuItem(fxString(UI, I18N_WORD_CLOSE), event -> onClose())),
                newMenu(fxString(UI, I18N_MENU_EDIT),
                        menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                                event -> onAddDocument()),
                        menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E,
                                event -> onEditDocument(), disableBinding),
                        new SeparatorMenuItem(),
                        menuItem(fxString(UI, I18N_WORD_OPEN), SHORTCUT_O, event -> onOpenDocument()),
                        menuItem(fxString(UI, I18N_WORD_SAVE, ELLIPSIS), SHORTCUT_S, event -> onDownload())
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context Menu
        table.setContextMenu(new ContextMenu(
                menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                        event -> onAddDocument()),
                menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E,
                        event -> onEditDocument(), disableBinding),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_WORD_OPEN), SHORTCUT_O, event -> onOpenDocument()),
                menuItem(fxString(UI, I18N_WORD_SAVE, ELLIPSIS), SHORTCUT_S, event -> onDownload())
        ));

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn(fxString(UI, I18N_WORD_COUNTERPARTY), b ->
                        b.withPropertyCallback(MoneyDocument::contactUuid)
                                .withCellFactory(x -> new DocumentContactNameCell())
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableColumn(fxString(UI, I18N_WORD_FILE), b ->
                        b.withPropertyCallback(MoneyDocument::fileName)
                                .withWidthBinding(w.multiply(0.2))
                ),
                tableColumn(fxString(UI, I18N_WORD_TYPE), b ->
                        b.withPropertyCallback(doc -> Bundles.translate(doc.documentType()))
                                .withWidthBinding(w.multiply(0.1))
                ),
                tableColumn(fxString(UI, I18N_WORD_SIZE), b ->
                        b.withPropertyCallback(doc -> Integer.toString(doc.size()))
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn(fxString(UI, I18N_WORD_DESCRIPTION), b ->
                        b.withPropertyCallback(MoneyDocument::description)
                                .withWidthBinding(w.multiply(0.4))
                ),
                tableColumn(fxString(UI, I18N_WORD_DATE), b ->
                        b.withPropertyCallback(MoneyDocument::date)
                                .withWidthBinding(w.multiply(0.15))
                )
        ));

        var root = new BorderPane(table, menuBar, null, null, null);
        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    public DocumentWindowController() {
        this(null);
    }

    public boolean thisOwner(MoneyRecord documentOwner) {
        return this.documentOwner == documentOwner;
    }

    @Override
    public String getTitle() {
        var name = UI.getString(I18N_WORD_DOCUMENTS);
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

    private void onAddDocument() {
        var d = new DocumentDialog(this, documentOwner, settings().getDialogCssFileUrl(), null);
        d.showAndWait().ifPresent(document -> dao().insertDocument(document, d.getBytes()));
    }

    private void onEditDocument() {
        getSelectedDocument()
                .flatMap(document -> new DocumentDialog(this, documentOwner, settings().getDialogCssFileUrl(), document)
                        .showAndWait())
                .ifPresent(updated -> dao().updateDocument(updated));
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
}
