/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Icon;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CATEGORY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

final class CategoryDialog extends BaseDialog<Category> {
    private final ValidationSupport validation = new ValidationSupport();

    private final ComboBox<CategoryType> typeComboBox = comboBox(CategoryType.values());
    private final TextField nameEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();

    CategoryDialog(Controller owner, URL css, Category category) {
        super(owner, css);

        setTitle(fxString(UI, I18N_WORD_CATEGORY));

        getDialogPane().setContent(gridPane(
            List.of(
                gridRow(label(fxString(UI, I18N_WORD_TYPE, COLON)), typeComboBox, iconComboBox),
                gridRow(label(fxString(UI, I18N_WORD_ENTITY_NAME, COLON)), gridCell(nameEdit, 2, 1)),
                gridRow(label(fxString(UI, I18N_WORD_COMMENT, COLON)), gridCell(commentEdit, 2, 1))),
            b -> b.withStyle(Styles.GRID_PANE))
        );

        nameEdit.setPrefColumnCount(20);
        typeComboBox.getSelectionModel().select(0);

        IconManager.setupComboBox(iconComboBox);

        if (category != null) {
            Arrays.stream(CategoryType.values())
                .filter(t -> t == category.type())
                .findFirst()
                .ifPresent(t -> typeComboBox.getSelectionModel().select(t));

            nameEdit.setText(category.name());
            commentEdit.setText(category.comment());
            iconComboBox.getSelectionModel().select(cache().getIcon(category.iconUuid()).orElse(EMPTY_ICON));
        } else {
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            long now = System.currentTimeMillis();

            var builder = new Category.Builder(category)
                .name(nameEdit.getText())
                .comment(commentEdit.getText())
                .type(typeComboBox.getSelectionModel().getSelectedItem())
                .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().uuid())
                .modified(now);

            if (category == null) {
                builder.uuid(UUID.randomUUID())
                    .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.initInitialDecoration();
    }

    ComboBox<CategoryType> getTypeComboBox() {
        return typeComboBox;
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    TextField getCommentEdit() {
        return commentEdit;
    }
}
