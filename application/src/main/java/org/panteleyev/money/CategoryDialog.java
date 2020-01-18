/*
 * Copyright (c) 2017, 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.icons.IconManager;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Icon;
import java.util.List;
import java.util.UUID;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.Constants.COLON;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.persistence.DataCache.cache;

final class CategoryDialog extends BaseDialog<Category> {
    private final ChoiceBox<CategoryType> typeComboBox = new ChoiceBox<>();
    private final TextField nameEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();

    CategoryDialog(Controller owner, Category category) {
        super(owner, MainWindowController.CSS_PATH);

        setTitle(RB.getString("Category"));

        var pane = new GridPane();

        int index = 0;
        pane.getStyleClass().add(Styles.GRID_PANE);
        pane.addRow(index++, newLabel(RB, "Type", COLON), typeComboBox, iconComboBox);
        pane.addRow(index++, newLabel(RB, "label.Name"), nameEdit);
        pane.addRow(index, newLabel(RB, "Comment", COLON), commentEdit);

        GridPane.setColumnSpan(nameEdit, 2);
        GridPane.setColumnSpan(commentEdit, 2);

        nameEdit.setPrefColumnCount(20);

        var list = List.of(CategoryType.values());
        typeComboBox.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) {
            typeComboBox.getSelectionModel().select(0);
        }

        IconManager.setupComboBox(iconComboBox);

        if (category != null) {
            list.stream()
                .filter(t -> t == category.getType())
                .findFirst()
                .ifPresent(t -> typeComboBox.getSelectionModel().select(t));

            nameEdit.setText(category.getName());
            commentEdit.setText(category.getComment());
            iconComboBox.getSelectionModel().select(cache().getIcon(category.getIconUuid()).orElse(EMPTY_ICON));
        } else {
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
        }

        typeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryType type) {
                return type.getTypeName();
            }

            @Override
            public CategoryType fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                long now = System.currentTimeMillis();

                var builder = new Category.Builder(category)
                    .name(nameEdit.getText())
                    .comment(commentEdit.getText())
                    .catTypeId(typeComboBox.getSelectionModel().getSelectedItem().getId())
                    .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().getUuid())
                    .modified(now);

                if (category == null) {
                    builder.guid(UUID.randomUUID())
                        .created(now);
                }

                return builder.build();
            } else {
                return null;
            }
        });

        getDialogPane().setContent(pane);
        createDefaultButtons(RB);

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.initInitialDecoration();
    }

    ChoiceBox<CategoryType> getTypeComboBox() {
        return typeComboBox;
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    TextField getCommentEdit() {
        return commentEdit;
    }
}
