/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.icons;

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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.panteleyev.money.BaseController;
import org.panteleyev.money.MainWindowController;
import org.panteleyev.money.model.Icon;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.commons.fx.FXFactory.newMenu;
import static org.panteleyev.commons.fx.FXFactory.newMenuBar;
import static org.panteleyev.commons.fx.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

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
    private final SortedList<Icon> sortedList = new SortedList<>(iconList, Comparator.comparing(Icon::getName));

    private final FlowPane iconFlow = new FlowPane();
    private final SelectionModel<Icon> selectionModel = new IconSelectionModel();

    public IconWindowController() {
        super(new Stage(), MainWindowController.CSS_PATH.toString());

        var menuBar = newMenuBar(
            newMenu(RB, "menu.File",
                newMenuItem(RB, "menu.File.Upload",
                    new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN),
                    event -> onUpload()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.File.Close", event -> onClose())),
            createHelpMenu(RB)
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
        return RB.getString("string.icons");
    }

    private SelectionModel<Icon> getSelectionModel() {
        return selectionModel;
    }

    private void updateIconFlow() {
        iconFlow.getChildren().setAll(
            sortedList.stream()
                .map(icon -> new IconCell(icon, this))
                .collect(Collectors.toList())
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
                getDao().insertIcon(icon);
                iconList.add(icon);
            } else {
                errors.add(icon.getName() + RB.getString("string.duplicate"));
            }
        }

        if (!errors.isEmpty()) {
            var content = String.join("\n", errors);
            new DuplicateAlert(RB.getString("string.filesIgnored") + "\n\n" + content).showAndWait();
        }
    }

    private boolean isUnique(Icon icon) {
        return cache().getIcons().stream().noneMatch(existing -> Arrays.equals(icon.getBytes(), existing.getBytes()));
    }

    private Icon readIcon(File file, List<String> errors) {
        try (var inputStream = new FileInputStream(file)) {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length > Icon.ICON_BYTE_LENGTH) {
                errors.add(file.getName() + RB.getString("string.tooBig"));
                return null;
            }

            try (var byteInputStream = new ByteArrayInputStream(bytes)) {
                var image = new Image(byteInputStream, Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
                var timestamp = System.currentTimeMillis();
                return new Icon(UUID.randomUUID(), file.getName(), bytes, image, timestamp, timestamp);
            }
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
        fileChooser.setTitle(RB.getString("string.upload"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG images", "*.png"),
            new FileChooser.ExtensionFilter("GIF images", "*.gif"));

        var selected = fileChooser.showOpenMultipleDialog(null);
        if (selected != null) {
            uploadIcons(selected);
        }
    }
}
