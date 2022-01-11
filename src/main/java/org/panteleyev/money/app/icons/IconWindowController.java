/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.icons;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.model.Icon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.icons.IconManager.ICON_BYTE_LENGTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_UPLOAD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_UPLOAD_DIPLICATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_UPLOAD_FILES_SKIPPED;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_UPLOAD_TOO_BIG;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ICONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_UPLOAD;

public final class IconWindowController extends BaseController {

    private class IconSelectionModel extends SingleSelectionModel<Icon> {
        @Override
        protected Icon getModelItem(int index) {
            return index >= 0 && index < sortedList.size() ? sortedList.get(index) : null;
        }

        @Override
        protected int getItemCount() {
            return sortedList.size();
        }
    }

    private static class DuplicateAlert extends Alert {
        DuplicateAlert(String contentText) {
            super(Alert.AlertType.WARNING, contentText, ButtonType.CLOSE);
            getDialogPane().setContent(new TextArea(contentText));
        }
    }

    private final ObservableList<Icon> iconList = FXCollections.observableArrayList();
    private final SortedList<Icon> sortedList = new SortedList<>(iconList, Comparator.comparing(Icon::name));

    private final FlowPane iconFlow = new FlowPane();
    private final SelectionModel<Icon> selectionModel = new IconSelectionModel();

    public IconWindowController() {
        super(new Stage(), settings().getMainCssFilePath());

        var menuBar = menuBar(
            newMenu(fxString(UI, I18N_MENU_FILE),
                menuItem(fxString(UI, I18N_MENU_ITEM_UPLOAD), SHORTCUT_U,
                    event -> onUpload()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CLOSE), event -> onClose())),
            createHelpMenu()
        );

        iconFlow.setVgap(20);
        iconFlow.setHgap(20);
        iconFlow.setPadding(new Insets(20));

        iconList.setAll(cache().getIcons());
        iconList.addListener((ListChangeListener<Icon>) change -> updateIconFlow());

        var root = new BorderPane();
        root.setPrefSize(600.0, 400.0);

        var scrollPane = new ScrollPane(iconFlow);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        root.setTop(menuBar);
        root.setCenter(scrollPane);

        scrollPane.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.LEFT) {
                getSelectionModel().selectPrevious();
                drawSelection();
            }
            if (keyEvent.getCode() == KeyCode.RIGHT) {
                getSelectionModel().selectNext();
                drawSelection();
            }
        });

        setupWindow(root);
        setupDragAndDrop();

        updateIconFlow();
        selectionModel.clearSelection();
        drawSelection();
    }

    @Override
    public String getTitle() {
        return fxString(UI, I18N_WORD_ICONS);
    }

    private SelectionModel<Icon> getSelectionModel() {
        return selectionModel;
    }

    private void updateIconFlow() {
        iconFlow.getChildren().setAll(
            sortedList.stream()
                .map(icon -> new IconCell(icon, this))
                .toList()
        );
    }

    private void drawSelection() {
        var selected = selectionModel.getSelectedItem();

        for (var cell : iconFlow.getChildren()) {
            cell.getStyleClass().remove("selectedIconCell");
            if (((IconCell) cell).getIcon() == selected) {
                cell.getStyleClass().add("selectedIconCell");
            }
        }
    }

    private void setupDragAndDrop() {
        iconFlow.setOnDragOver(event -> {
            if (event.getGestureSource() != iconFlow && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        iconFlow.setOnDragDropped(event -> {
            var dragBoard = event.getDragboard();
            if (dragBoard.hasFiles()) {
                uploadIcons(dragBoard.getFiles());
            }

            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void uploadIcons(List<File> files) {
        var errors = new ArrayList<String>();

        for (var file : files) {
            var icon = readIcon(file, errors);
            if (icon == null) {
                continue;
            }

            if (isUnique(icon)) {
                dao().insertIcon(icon);
                iconList.add(icon);
            } else {
                errors.add(icon.name() + UI.getString(I18N_MISC_UPLOAD_DIPLICATE));
            }
        }

        if (!errors.isEmpty()) {
            var content = String.join("\n", errors);
            new DuplicateAlert(UI.getString(I18N_MISC_UPLOAD_FILES_SKIPPED) + "\n\n" + content).showAndWait();
        }
    }

    private boolean isUnique(Icon icon) {
        return cache().getIcons().stream().noneMatch(existing -> Arrays.equals(icon.bytes(), existing.bytes()));
    }

    private Icon readIcon(File file, List<String> errors) {
        try (var inputStream = new FileInputStream(file)) {
            var bytes = inputStream.readAllBytes();
            if (bytes.length > ICON_BYTE_LENGTH) {
                errors.add(file.getName() + UI.getString(I18N_MISC_UPLOAD_TOO_BIG));
                return null;
            }

            var timestamp = System.currentTimeMillis();
            return new Icon(UUID.randomUUID(), file.getName(), bytes, timestamp, timestamp);
        } catch (IOException ex) {
            return null;
        }
    }

    void onSelect(IconCell cell) {
        selectionModel.select(cell.getIcon());
        drawSelection();
    }

    private void onUpload() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(fxString(UI, I18N_WORD_UPLOAD));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG images", "*.png"),
            new FileChooser.ExtensionFilter("GIF images", "*.gif"));

        var selected = fileChooser.showOpenMultipleDialog(getStage());
        if (selected != null) {
            uploadIcons(selected);
        }
    }
}
