/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.util.StringConverter
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.utilities.fx.BaseDialog
import java.util.Arrays
import java.util.ResourceBundle

class CategoryDialog(val category : Category?) : BaseDialog<Category>(MainWindowController.DIALOGS_CSS) {

    private val typeComboBox = ChoiceBox<CategoryType>()
    private val nameEdit = TextField()
    private val commentEdit = TextField()

    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    init {
        dialogPane.stylesheets.add(MainWindowController.DIALOGS_CSS)

        title = rb.getString("category.Dialog.Title")

        val pane = GridPane()

        var index = 0
        with (pane) {
            pane.styleClass.add(Styles.GRID_PANE)
            addRow(index++, Label(rb.getString("label.Type")), typeComboBox)
            addRow(index++, Label(rb.getString("label.Name")), nameEdit)
            addRow(index, Label(rb.getString("label.Comment")), commentEdit)
        }

        nameEdit.prefColumnCount = 20

        val list = Arrays.asList(*CategoryType.values())
        typeComboBox.items = FXCollections.observableArrayList(list)
        if (!list.isEmpty()) {
            typeComboBox.selectionModel.select(0)
        }

        if (category != null) {
            list.find { it == category.type }?.let { typeComboBox.selectionModel.select(it) }
            nameEdit.text = category.name
            commentEdit.text = category.comment
        }

        typeComboBox.converter = object : StringConverter<CategoryType>() {
            override fun toString(obj: CategoryType): String = obj.typeName
            override fun fromString(string: String): CategoryType = throw UnsupportedOperationException("Not supported yet.")
        }

        setResultConverter { b: ButtonType ->
            if (b == ButtonType.OK) {
                return@setResultConverter category?.copy(name = nameEdit.text, comment = commentEdit.text, catTypeId = typeComboBox.selectionModel.selectedItem.id)
                        ?:Category(0, name = nameEdit.text, comment = commentEdit.text, catTypeId = typeComboBox.selectionModel.selectedItem.id, expanded = false)
            } else {
                return@setResultConverter null
            }
        }

        dialogPane.content = pane
        createDefaultButtons(rb)

        Platform.runLater { createValidationSupport() }
    }

    private fun createValidationSupport() {
        validation.registerValidator(nameEdit) { control: Control, value: String ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty()) }
        validation.initInitialDecoration()
    }
}