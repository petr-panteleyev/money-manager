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
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.xml.Import
import org.panteleyev.utilities.fx.BaseDialog
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.CompletableFuture

class ImportWizard : BaseDialog<Any>(MainWindowController.CSS_PATH) {
    private val startPage = StartPage()
    private val progressPage = ProgressPage()

    private class StartPage : GridPane() {
        private val btnGroup = ToggleGroup()
        private val fullDumpRadio = RadioButton(MainWindowController.RB.getString("label.FullDump")).apply {
            toggleGroup = btnGroup
        }
        private val partialImportRadio = RadioButton(MainWindowController.RB.getString("label.PartialImport")).apply {
            toggleGroup = btnGroup
            isSelected = true
        }
        internal val fileNameEdit = TextField().apply {
            promptText = MainWindowController.RB.getString("prompt.ImportFileName")
            prefColumnCount = 40
        }
        private val browseButton = Button("...").apply {
            onAction = EventHandler { onBrowse() }
        }
        private val warningLabel = Label(MainWindowController.RB.getString("label.FullDumpImportWarning")).apply {
            isWrapText = true
            visibleProperty().bind(fullDumpRadio.selectedProperty())
        }
        internal val warningCheck = CheckBox(MainWindowController.RB.getString("check.FullDumpImport")).apply {
            styleClass.add(Styles.BOLD_TEXT)
            visibleProperty().bind(fullDumpRadio.selectedProperty())
        }

        val fileName
            get() = fileNameEdit.text
        val fullDump
            get() = fullDumpRadio.isSelected

        init {
            styleClass.add(Styles.GRID_PANE)

            addRow(0, fileNameEdit, browseButton)
            addRow(1, partialImportRadio)
            addRow(2, fullDumpRadio)

            addRow(3, warningLabel)
            addRow(4, warningCheck)

            GridPane.setColumnSpan(partialImportRadio, 2)
            GridPane.setColumnSpan(fullDumpRadio, 2)
            GridPane.setColumnSpan(warningLabel, 2)
            GridPane.setColumnSpan(warningCheck, 2)
        }

        private fun onBrowse() {
            val selected = FileChooser().apply {
                title = MainWindowController.RB.getString("word.Import")
                extensionFilters.addAll(
                        FileChooser.ExtensionFilter("XML Files", "*.xml"),
                        FileChooser.ExtensionFilter("All Files", "*.*")
                )
            }.showOpenDialog(null)

            fileNameEdit.text = selected?.absolutePath ?: ""
        }
    }

    private class ProgressPage : BorderPane() {
        val inProgressProperty = SimpleBooleanProperty()

        private val textArea = TextArea().apply {
            isEditable = false
            prefColumnCount = 40
            prefRowCount = 21
        }

        private val progress: (txt: String) -> Unit = {
            Platform.runLater { textArea.appendText(it) }
        }

        init {
            center = textArea
            inProgressProperty.set(true)
        }

        fun start(fileName: String, fullDump: Boolean) {
            CompletableFuture.runAsync {
                val file = File(fileName)
                if (!file.exists()) {
                    throw IOException("File not found")
                }

                progress("Reading file... ")
                val imp = Import.import(FileInputStream(file))
                progress("done\n\n")

                if (fullDump) {
                    MoneyDAO.importFullDump(imp, progress)
                    progress("\n")
                    MoneyDAO.preload(progress)
                } else {
                    MoneyDAO.importRecords(imp, progress)
                    progress("\n")
                    MoneyDAO.preload(progress)
                }
            }.handle { _: Void?, t: Throwable? ->
                t?.let {
                    MoneyApplication.uncaughtException(t.cause ?: t)
                }

                inProgressProperty.set(false)
            }
        }
    }

    init {
        title = MainWindowController.RB.getString("word.Import")

        dialogPane.buttonTypes.addAll(
                ButtonType.NEXT,
                ButtonType.CANCEL
        )

        val nextButton = dialogPane.lookupButton(ButtonType.NEXT) as Button
        nextButton.text = MainWindowController.RB.getString("word.Import")

        dialogPane.content = StackPane(progressPage, startPage)
        progressPage.isVisible = false

        nextButton.addEventFilter(ActionEvent.ACTION, { event ->
            event.consume()

            startPage.isVisible = false
            progressPage.isVisible = true

            dialogPane.buttonTypes.remove(ButtonType.CANCEL)
            dialogPane.buttonTypes.remove(ButtonType.NEXT)
            dialogPane.buttonTypes.add(ButtonType.CLOSE)

            dialogPane.lookupButton(ButtonType.CLOSE).disableProperty().bind(progressPage.inProgressProperty)

            progressPage.start(startPage.fileName, startPage.fullDump)
        })

        nextButton.disableProperty().bind(validation.invalidProperty())

        Platform.runLater { createValidationSupport() }
    }

    private fun createValidationSupport() {
        validation.registerValidator(startPage.fileNameEdit) { control: Control, value: String ->
            val invalid = value.isEmpty() || !File(value).exists()
            ValidationResult.fromErrorIf(control, null, invalid)
        }
        validation.registerValidator(startPage.warningCheck) { control: Control, value: Boolean ->
            ValidationResult.fromErrorIf(control, null, startPage.fullDump && !value)
        }
        validation.initInitialDecoration()
    }
}