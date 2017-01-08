/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;

public class CategoryDialog extends BaseDialog<Category.Builder> implements Initializable {
    private static final String FXML_PATH = "/org/panteleyev/money/CategoryDialog.fxml";

    @FXML private ChoiceBox<CategoryType>   typeComboBox;
    @FXML private TextField                 nameEdit;
    @FXML private TextField                 commentEdit;

    private final Category                  category;

    private final MoneyDAO dao;

    CategoryDialog(Category category) {
        super(FXML_PATH, MainWindowController.UI_BUNDLE_PATH);

        this.category = category;
        dao = MoneyDAO.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        setTitle(rb.getString("category.Dialog.Title"));

        final Collection<CategoryType> list = dao.getCategoryTypes();
        typeComboBox.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) {
            typeComboBox.getSelectionModel().select(0);
        }

        if (category != null) {
            Optional<CategoryType> type = list.stream()
                .filter(x -> x.getId().equals(category.getCatTypeId()))
                .findFirst();
            type.ifPresent(categoryType -> typeComboBox.getSelectionModel().select(categoryType));
            nameEdit.setText(category.getName());
            commentEdit.setText(category.getComment());
        }

        typeComboBox.setConverter(new StringConverter<CategoryType>() {
            @Override
            public String toString(CategoryType object) {
                return object.getTranslatedName();
            }

            @Override
            public CategoryType fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                return new Category.Builder(this.category)
                    .name(nameEdit.getText())
                    .comment(commentEdit.getText())
                    .typeId(typeComboBox.getSelectionModel().getSelectedItem().getId());
            } else {
                return null;
            }
        });

        createDefaultButtons();

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.initInitialDecoration();
    }

}
