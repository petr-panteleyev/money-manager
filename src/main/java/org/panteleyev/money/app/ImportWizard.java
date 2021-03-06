/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
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
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.money.MoneyApplication;
import org.panteleyev.money.app.options.Options;
import org.panteleyev.money.xml.Import;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.CLOSE;
import static javafx.scene.control.ButtonType.NEXT;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.ButtonFactory.radioButton;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.Constants.FILTER_ALL_FILES;
import static org.panteleyev.money.app.Constants.FILTER_XML_FILES;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_FULL_DUMP;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_FULL_DUMP_IMPORT_CHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_FULL_DUMP_IMPORT_WARNING;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_IMPORT_FILE_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_PARTIAL_IMPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_IMPORT;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class ImportWizard extends BaseDialog<Object> {
    private final ValidationSupport validation = new ValidationSupport();

    private final StartPage startPage = new StartPage();
    private final ProgressPage progressPage = new ProgressPage();

    private static class StartPage extends GridPane {
        private final ToggleGroup btnGroup = new ToggleGroup();
        private final RadioButton fullDumpRadio = radioButton(fxString(UI, I18N_MISC_FULL_DUMP), btnGroup);
        final TextField fileNameEdit = createFileNameEdit();
        final CheckBox warningCheck = createWarningCheckBox();

        String getFileName() {
            return fileNameEdit.getText();
        }

        boolean getFullDump() {
            return fullDumpRadio.isSelected();
        }

        StartPage() {
            getStyleClass().add(Styles.GRID_PANE);


            var partialImportRadio = radioButton(fxString(UI, I18N_MISC_PARTIAL_IMPORT), btnGroup, true);

            var warningLabel = createWarningLabel();

            addRow(0, fileNameEdit, button("...", x -> onBrowse()));
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
            field.setPromptText(fxString(UI, I18N_MISC_IMPORT_FILE_NAME));
            field.setPrefColumnCount(40);
            return field;
        }

        private Label createWarningLabel() {
            var label = label(fxString(UI, I18N_MISC_FULL_DUMP_IMPORT_WARNING));
            label.setWrapText(true);
            label.visibleProperty().bind(fullDumpRadio.selectedProperty());
            return label;
        }

        private CheckBox createWarningCheckBox() {
            var checkBox = newCheckBox(UI, I18N_MISC_FULL_DUMP_IMPORT_CHECK);
            checkBox.getStyleClass().add(Styles.BOLD_TEXT);
            checkBox.visibleProperty().bind(fullDumpRadio.selectedProperty());
            return checkBox;
        }

        private void onBrowse() {
            var chooser = new FileChooser();
            chooser.setTitle(fxString(UI, I18N_WORD_IMPORT));
            Options.getLastExportDir().ifPresent(chooser::setInitialDirectory);
            chooser.getExtensionFilters().addAll(FILTER_XML_FILES, FILTER_ALL_FILES);

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
                    } else {
                        getDao().importRecords(imp, progress);
                    }
                    progress.accept("\n");
                    getDao().preload(progress);
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
        super(options().getDialogCssFileUrl());
        setTitle(fxString(UI, I18N_WORD_IMPORT));

        getDialogPane().getButtonTypes().addAll(NEXT, CANCEL);

        getDialogPane().setContent(new StackPane(progressPage, startPage));
        progressPage.setVisible(false);

        getButton(NEXT).ifPresent(nextButton -> {
            nextButton.setText(fxString(UI, I18N_WORD_IMPORT));
            nextButton.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();

                startPage.setVisible(false);
                progressPage.setVisible(true);

                getDialogPane().getButtonTypes().remove(CANCEL);
                getDialogPane().getButtonTypes().remove(NEXT);
                getDialogPane().getButtonTypes().add(CLOSE);

                getButton(CLOSE).ifPresent(b ->
                    b.disableProperty().bind(progressPage.inProgressProperty));

                progressPage.start(startPage.getFileName(), startPage.getFullDump());
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
            ValidationResult.fromErrorIf(control, null, startPage.getFullDump() && !value));
        validation.initInitialDecoration();
    }
}
