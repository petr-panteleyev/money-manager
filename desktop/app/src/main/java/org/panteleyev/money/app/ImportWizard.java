/*
 Copyright © 2017-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.MoneyApplication;
import org.panteleyev.money.app.dialogs.ExportFileFialog;
import org.panteleyev.money.desktop.export.Import;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.CLOSE;
import static javafx.scene.control.ButtonType.NEXT;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;

final class ImportWizard extends BaseDialog<Object> {
    private final ValidationSupport validation = new ValidationSupport();

    private final StartPage startPage = new StartPage(getOwner());
    private final ProgressPage progressPage = new ProgressPage();

    private static class StartPage extends GridPane {
        private final Window owner;
        final TextField fileNameEdit = createFileNameEdit();
        final CheckBox warningCheck = createWarningCheckBox();

        String getFileName() {
            return fileNameEdit.getText();
        }

        StartPage(Window owner) {
            this.owner = owner;

            getStyleClass().add(Styles.GRID_PANE);

            var warningLabel = createWarningLabel();

            addRow(0, fileNameEdit, button("...", _ -> onBrowse()));
            addRow(1, warningLabel);
            addRow(2, warningCheck);

            GridPane.setColumnSpan(warningLabel, 2);
            GridPane.setColumnSpan(warningCheck, 2);
        }

        private TextField createFileNameEdit() {
            var field = new TextField();
            field.setPromptText("Имя файла для импорта");
            field.setPrefColumnCount(40);
            return field;
        }

        private Label createWarningLabel() {
            var label = label("""
                    Внимание!
                    Импортирование полного дампа полностью уничтожит
                    все существующие записи в базе.""");
            label.setWrapText(true);
            return label;
        }

        private CheckBox createWarningCheckBox() {
            var checkBox = new CheckBox("Да, я уверен!");
            checkBox.getStyleClass().add(Styles.BOLD_TEXT);
            return checkBox;
        }

        private void onBrowse() {
            var fileName = new ExportFileFialog().showImportDialog(owner)
                    .map(File::getAbsolutePath)
                    .orElse("");

            fileNameEdit.setText(fileName);
        }
    }

    private static class ProgressPage extends BorderPane {
        final SimpleBooleanProperty inProgressProperty = new SimpleBooleanProperty();

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

        void start(String fileName) {
            CompletableFuture.runAsync(() -> {
                var file = new File(fileName);
                if (!file.exists()) {
                    throw new RuntimeException("File not found");
                }

                progress.accept("Валидация... ");
                try (var input = new FileInputStream(file)) {
                    Import.validate(input);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                progress.accept("выполнено\n\n");

                progress.accept("Чтение файла... ");
                try (var input = new FileInputStream(file)) {
                    var imp = Import.doImport(input);
                    progress.accept("выполнено\n\n");
                    dao().importFullDump(imp, progress);
                    progress.accept("\n");
                    dao().preload(Platform::runLater, progress);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }).handle((_, t) -> {
                if (t != null) {
                    MoneyApplication.uncaughtException(t.getCause() != null ? t.getCause() : t);
                }

                inProgressProperty.set(false);
                return null;
            });
        }
    }

    ImportWizard(Controller owner) {
        super(owner, settings().getDialogCssFileUrl());
        setTitle("Импорт");

        getDialogPane().getButtonTypes().addAll(NEXT, CANCEL);

        getDialogPane().setContent(new StackPane(progressPage, startPage));
        progressPage.setVisible(false);

        getButton(NEXT).ifPresent(nextButton -> {
            nextButton.setText("Импорт");
            nextButton.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();

                startPage.setVisible(false);
                progressPage.setVisible(true);

                getDialogPane().getButtonTypes().remove(CANCEL);
                getDialogPane().getButtonTypes().remove(NEXT);
                getDialogPane().getButtonTypes().add(CLOSE);

                getButton(CLOSE).ifPresent(b ->
                        b.disableProperty().bind(progressPage.inProgressProperty));

                progressPage.start(startPage.getFileName());
            });
            nextButton.disableProperty().bind(validation.invalidProperty());
        });

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(startPage.fileNameEdit, (Control control, String value) -> {
            var invalid = value.isEmpty() || !new File(value).exists();
            return ValidationResult.fromErrorIf(control, null, invalid);
        });
        validation.registerValidator(startPage.warningCheck, (Control control, Boolean value) ->
                ValidationResult.fromErrorIf(control, null, !value));
        validation.initInitialDecoration();
    }
}
