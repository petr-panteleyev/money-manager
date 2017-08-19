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
import javafx.geometry.VPos
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.ContactType
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import org.panteleyev.utilities.fx.BaseDialog
import java.util.ResourceBundle
import java.util.UUID

class ContactDialog(val contact: Contact?) : BaseDialog<Contact>(MainWindowController.CSS_PATH) {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val typeChoiceBox = ChoiceBox<ContactType>()
    private val nameField = TextField()
    private val phoneField = TextField()
    private val mobileField = TextField()
    private val emailField = TextField()
    private val webField = TextField()
    private val commentEdit = TextArea()
    private val streetField = TextField()
    private val cityField = TextField()
    private val countryField = TextField()
    private val zipField = TextField()

    init {
        title = rb.getString("contact.Dialog.Title")

        dialogPane.content = GridPane().apply {
            styleClass.add(Styles.GRID_PANE)

            var index = 0
            addRow(index++, Label(rb.getString("label.Type")), typeChoiceBox)
            addRow(index++, Label(rb.getString("label.Name")), nameField)
            addRow(index++, Label(rb.getString("label.Phone")), phoneField)
            addRow(index++, Label(rb.getString("label.Mobile")), mobileField)
            addRow(index++, Label(rb.getString("label.Email")), emailField)
            addRow(index++, Label("URL:"), webField)
            addRow(index++, Label(rb.getString("label.Street")), streetField)
            addRow(index++, Label(rb.getString("label.City")), cityField)
            addRow(index++, Label(rb.getString("label.Country")), countryField)
            addRow(index++, Label(rb.getString("label.ZIP")), zipField)
            addRow(index, Label(rb.getString("label.Comment")), commentEdit)

            val topAlignmentConstraints = RowConstraints()
            topAlignmentConstraints.valignment = VPos.TOP
            for (i in 0..index - 1) {
                rowConstraints.add(RowConstraints())
            }
            rowConstraints.add(topAlignmentConstraints)
        }

        nameField.prefColumnCount = 20

        typeChoiceBox.items = FXCollections.observableArrayList(*ContactType.values())
        typeChoiceBox.converter = object : ReadOnlyStringConverter<ContactType>() {
            override fun toString(type: ContactType): String {
                return type.typeName
            }
        }

        if (contact != null) {
            typeChoiceBox.selectionModel.select(contact.type)

            nameField.text = contact.name
            phoneField.text = contact.phone
            mobileField.text = contact.mobile
            emailField.text = contact.email
            webField.text = contact.web
            commentEdit.text = contact.comment
            streetField.text = contact.street
            cityField.text = contact.city
            countryField.text = contact.country
            zipField.text = contact.zip
        } else {
            typeChoiceBox.selectionModel.select(0)
        }

        setResultConverter { b: ButtonType ->
            if (b == ButtonType.OK) {
                return@setResultConverter Contact(contact?.id ?: 0,
                        typeId = typeChoiceBox.selectionModel.selectedItem.id,
                        name = nameField.text,
                        phone = phoneField.text,
                        mobile = mobileField.text,
                        email = emailField.text,
                        web = webField.text,
                        comment = commentEdit.text,
                        street = streetField.text,
                        city = cityField.text,
                        country = countryField.text,
                        zip = zipField.text,
                        guid = contact?.guid ?: UUID.randomUUID().toString(),
                        modified = System.currentTimeMillis()
                )
            } else {
                return@setResultConverter null
            }
        }

        createDefaultButtons(rb)
        Platform.runLater { createValidationSupport() }
    }

    private fun createValidationSupport() {
        validation.registerValidator(nameField) {
            control: Control, value: String ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty())
        }
        validation.initInitialDecoration()
    }

}