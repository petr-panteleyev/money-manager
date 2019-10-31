/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.xml.Import;
import org.panteleyev.commons.fx.BaseDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class ImportWizard extends BaseDialog {
    private final StartPage startPage = new StartPage();
    private final ProgressPage progressPage = new ProgressPage();

    private static class StartPage extends GridPane {
        private ToggleGroup btnGroup = new ToggleGroup();
        private RadioButton fullDumpRadio = new RadioButton(RB.getString("label.FullDump"));
        private RadioButton partialImportRadio = new RadioButton(RB.getString("label.PartialImport"));
        TextField fileNameEdit = createFileNameEdit();
        private Button browseButton = createBrowseButton();
        private Label warningLabel = createWarningLabel();
        CheckBox warningCheck = createWarningCheckBox();

        String getFileName() {
            return fileNameEdit.getText();
        }

        boolean getFullDump() {
            return fullDumpRadio.isSelected();
        }

        StartPage() {
            getStyleClass().add(Styles.GRID_PANE);

            fullDumpRadio.setToggleGroup(btnGroup);
            partialImportRadio.setToggleGroup(btnGroup);
            partialImportRadio.setSelected(true);


            addRow(0, fileNameEdit, browseButton);
            addRow(1, partialImportRadio);
            addRow(2, fullDumpRadio);

            addRow(3, warningLabel);
            addRow(4, warningCheck);

            GridPane.setColumnSpan(partialImportRadio, 2);
            GridPane.setColumnSpan(fullDumpRadio, 2);
            GridPane.setColumnSpan(warningLabel, 2);
            GridPane.setColumnSpan(warningCheck, 2);
        }

        private TextField createFileNameEdit() {
            var field = new TextField();
            field.setPromptText(RB.getString("prompt.ImportFileName"));
            field.setPrefColumnCount(40);
            return field;
        }

        private Button createBrowseButton() {
            var btn = new Button("...");
            btn.setOnAction(event -> onBrowse());
            return btn;
        }

        private Label createWarningLabel() {
            var label = new Label(RB.getString("label.FullDumpImportWarning"));
            label.setWrapText(true);
            label.visibleProperty().bind(fullDumpRadio.selectedProperty());
            return label;
        }

        private CheckBox createWarningCheckBox() {
            var checkBox = new CheckBox(RB.getString("check.FullDumpImport"));
            checkBox.getStyleClass().add(Styles.BOLD_TEXT);
            checkBox.visibleProperty().bind(fullDumpRadio.selectedProperty());
            return checkBox;
        }

        private void onBrowse() {
            var chooser = new FileChooser();
            chooser.setTitle(RB.getString("word.Import"));
            Options.getLastExportDir().ifPresent(chooser::setInitialDirectory);
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            var selected = chooser.showOpenDialog(null);

            fileNameEdit.setText(selected != null ? selected.getAbsolutePath() : "");
        }
    }

    private static class ProgressPage extends BorderPane {
        SimpleBooleanProperty inProgressProperty = new SimpleBooleanProperty();

        private final TextArea textArea = createTextArea();

        private final Consumer<String> progress = text -> Platform.runLater(() -> textArea.appendText(text));

        ProgressPage() {
            setCenter(textArea);
            inProgressProperty.set(true);
        }

        private TextArea createTextArea() {
            var textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setPrefColumnCount(40);
            textArea.setPrefRowCount(21);
            return textArea;
        }

        void start(String fileName, boolean fullDump) {
            CompletableFuture.runAsync(() -> {
                var file = new File(fileName);
                if (!file.exists()) {
                    throw new RuntimeException("File not found");
                }

                progress.accept("Reading file... ");
                try (var input = new FileInputStream(file)) {
                    var imp = Import.doImport(input);
                    progress.accept("done\n\n");

                    if (fullDump) {
                        getDao().importFullDump(imp, progress);
                        progress.accept("\n");
                        getDao().preload(progress);
                    } else {
                        getDao().importRecords(imp, progress);
                        progress.accept("\n");
                        getDao().preload(progress);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).handle((x, t) -> {
                if (t != null) {
                    MoneyApplication.uncaughtException(t.getCause() != null ? t.getCause() : t);
                }

                inProgressProperty.set(false);
                return null;
            });
        }
    }

    ImportWizard() {
        super(MainWindowController.CSS_PATH);
        setTitle(RB.getString("word.Import"));

        getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.CANCEL);

        var nextButton = (Button) getDialogPane().lookupButton(ButtonType.NEXT);
        nextButton.setText(RB.getString("word.Import"));

        getDialogPane().setContent(new StackPane(progressPage, startPage));
        progressPage.setVisible(false);

        nextButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();

            startPage.setVisible(false);
            progressPage.setVisible(true);

            getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
            getDialogPane().getButtonTypes().remove(ButtonType.NEXT);
            getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            getDialogPane().lookupButton(ButtonType.CLOSE).disableProperty().bind(progressPage.inProgressProperty);

            progressPage.start(startPage.getFileName(), startPage.getFullDump());
        });

        nextButton.disableProperty().bind(validation.invalidProperty());

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(startPage.fileNameEdit, (Control control, String value) -> {
            var invalid = value.isEmpty() || !new File(value).exists();
            return ValidationResult.fromErrorIf(control, null, invalid);
        });
        validation.registerValidator(startPage.warningCheck, (Control control, Boolean value) ->
            ValidationResult.fromErrorIf(control, null, startPage.getFullDump() && !value));
        validation.initInitialDecoration();
    }
}
