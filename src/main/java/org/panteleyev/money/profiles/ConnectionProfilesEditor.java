/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.profiles;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.MainWindowController;
import org.panteleyev.money.Styles;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.ssh.SshManager;
import org.panteleyev.utilities.fx.BaseDialog;
import java.sql.SQLException;
import java.util.Optional;
import static org.panteleyev.money.MainWindowController.RB;

public class ConnectionProfilesEditor extends BaseDialog {
    private static int counter = 0;

    private final ValidationSupport profileNameValidation = new ValidationSupport();

    private final ListView<ConnectionProfile> profileListView = initProfileListView();
    private TextField profileNameEdit = new TextField();
    private TCPEditor tcpEditor = new TCPEditor();
    private EncryptionKeyEditor encryptionKeyEditor = new EncryptionKeyEditor();
    private Label testStatusLabel = new Label();

    private static final Validator<String> INTEGER_VALIDATOR = (Control control, String text) -> {
        var invalid = false;
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(text);
        } catch (Exception ex) {
            invalid = true;
        }
        return ValidationResult.fromErrorIf(control, null, invalid);
    };

    public ConnectionProfilesEditor() {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("text.Profiles"));

        var newButtonType = new ButtonType(RB.getString("button.NewProfile"), ButtonBar.ButtonData.LEFT);
        var deleteButtonType = new ButtonType(RB.getString("button.Delete"), ButtonBar.ButtonData.LEFT);
        var testButtonType = new ButtonType(RB.getString("button.TestConnection"), ButtonBar.ButtonData.BIG_GAP);
        var saveButtonType = new ButtonType(RB.getString("button.Save"), ButtonBar.ButtonData.SMALL_GAP);

        var dp = getDialogPane();

        dp.getButtonTypes().addAll(newButtonType, deleteButtonType, testButtonType, saveButtonType, ButtonType.CLOSE);

        dp.lookupButton(newButtonType).addEventFilter(ActionEvent.ACTION, this::onNewButton);

        var deleteButton = (Button) getDialogPane().lookupButton(deleteButtonType);
        deleteButton.disableProperty().bind(profileListView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.addEventFilter(ActionEvent.ACTION, this::newDeleteButton);

        var saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(profileNameValidation.invalidProperty()
                .or(encryptionKeyEditor.getValidation().invalidProperty()));
        saveButton.addEventFilter(ActionEvent.ACTION, this::onSaveButton);

        var initButton = tcpEditor.getCreateSchemaButton();
        initButton.disableProperty().bind(validation.invalidProperty());
        initButton.addEventFilter(ActionEvent.ACTION, this::onInitButton);

        var testButton = (Button) getDialogPane().lookupButton(testButtonType);
        testButton.disableProperty().bind(validation.invalidProperty());
        testButton.addEventFilter(ActionEvent.ACTION, this::onTestButton);

        ((Button) getDialogPane().lookupButton(ButtonType.CLOSE)).setText(RB.getString("button.Close"));

        var root = new BorderPane();
        root.setLeft(initLeftPane());
        root.setCenter(initCenterPane());

        BorderPane.setMargin(root.getCenter(), new Insets(0.0, 0.0, 5.0, 10.0));

        getDialogPane().setContent(root);

        Platform.runLater(this::createValidationSupport);
    }

    private Optional<ConnectionProfile> getSelectedProfile() {
        return Optional.ofNullable(profileListView.getSelectionModel().getSelectedItem());
    }

    private ListView<ConnectionProfile> initProfileListView() {
        var listView = new ListView<ConnectionProfile>();

        listView.setCellFactory(param -> {
            TextFieldListCell<ConnectionProfile> cell = new TextFieldListCell<>();
            cell.setConverter(new ReadOnlyStringConverter<>() {
                @Override
                public String toString(ConnectionProfile profile) {
                    return profile.getName() != null ? profile.getName() : "";
                }
            });
            return cell;
        });

        listView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> onProfileSelected(newValue));

        listView.addEventFilter(MouseEvent.ANY, event -> {
            if (profileNameValidation.isInvalid()) {
                event.consume();
            }
        });

        listView.setItems(FXCollections.observableArrayList(ConnectionProfileManager.getAll()));
        return listView;
    }

    private void newDeleteButton(ActionEvent event) {
        event.consume();
        getSelectedProfile().ifPresent(selected ->
                new Alert(Alert.AlertType.CONFIRMATION, RB.getString("text.AreYouSure"), ButtonType.OK, ButtonType.CANCEL)
                        .showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(b -> {
                            ConnectionProfileManager.deleteProfile(selected);
                            ConnectionProfileManager.saveProfiles();
                            profileListView.getItems().remove(selected);
                        }));
    }

    private void onSaveButton(ActionEvent event) {
        event.consume();

        int index = profileListView.getSelectionModel().getSelectedIndex();

        var prof = buildConnectionProfile();

        profileListView.getItems().set(index, prof);
        profileListView.getSelectionModel().select(prof);

        ConnectionProfileManager.setProfiles(profileListView.getItems());
        ConnectionProfileManager.saveProfiles();
    }

    private ConnectionProfile buildConnectionProfile() {
        return new ConnectionProfile(
                profileNameEdit.getText(),
                tcpEditor.getDataBaseHost(),
                tcpEditor.getDataBasePort(),
                tcpEditor.getDataBaseUser(),
                tcpEditor.getDataBasePassword(),
                tcpEditor.getSchema(),
                encryptionKeyEditor.getEncryptionKey(),
                tcpEditor.getSshSession()
        );
    }

    private void onInitButton(ActionEvent event) {
        event.consume();

        new Alert(Alert.AlertType.CONFIRMATION, RB.getString("text.AreYouSure"), ButtonType.OK, ButtonType.CANCEL)
                .showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(b -> {
                    var profile = buildConnectionProfile();
                    var ds = profile.buildDataSource();

                    Exception ex = MoneyDAO.initDatabase(ds, profile.getSchema());
                    if (ex != null) {
                        testFail(ex.getMessage());
                    } else {
                        testSuccess();
                    }
                });
    }

    private void onTestButton(ActionEvent event) {
        event.consume();

        var profile = buildConnectionProfile();
        SshManager.setupTunnel(profile);

        var TEST_QUERY = "SHOW TABLES FROM " + profile.getSchema();

        var ds = profile.buildDataSource();

        try (var conn = ds.getConnection(); var st = conn.createStatement()) {
            st.execute(TEST_QUERY);
            testSuccess();
        } catch (SQLException ex) {
            testFail(ex.getMessage());
        }
    }

    private void testSuccess() {
        Platform.runLater(() -> {
            testStatusLabel.setText("Success");
            testStatusLabel.getStyleClass().remove(Styles.RED_TEXT);
            testStatusLabel.getStyleClass().add(Styles.GREEN_TEXT);
        });
    }

    private void testFail(String txt) {
        Platform.runLater(() -> {
            testStatusLabel.setText(txt != null ? txt : "");
            testStatusLabel.getStyleClass().remove(Styles.GREEN_TEXT);
            testStatusLabel.getStyleClass().add(Styles.RED_TEXT);
        });
    }

    private void onProfileSelected(ConnectionProfile profile) {
        if (profile != null) {
            profileNameEdit.setText("");               // enforce validation
            profileNameEdit.setText(profile.getName());
            tcpEditor.setDataBaseHost(profile.getDataBaseHost());
            tcpEditor.setDataBasePort(profile.getDataBasePort());
            tcpEditor.setDataBaseUser(profile.getDataBaseUser());
            tcpEditor.setDataBasePassword(profile.getDataBasePassword());
            tcpEditor.setSchema(profile.getSchema());
            tcpEditor.setSshSession(profile.getSshSession());
            encryptionKeyEditor.setEncryptionKey(profile.getEncryptionKey());
        } else {
            profileNameEdit.setText("");
            tcpEditor.setDataBaseHost("");
            tcpEditor.setDataBasePort(3306);
            tcpEditor.setDataBaseUser("");
            tcpEditor.setDataBasePassword("");
            tcpEditor.setSchema("");
            tcpEditor.setSshSession(null);
        }
    }

    private void onNewButton(ActionEvent event) {
        event.consume();

        var profile = new ConnectionProfile("New Profile" + (++counter), "money");
        profileListView.getItems().add(profile);
        profileListView.getSelectionModel().select(profile);
    }

    private void createValidationSupport() {
        validation.registerValidator(tcpEditor.getSchemaEdit(), (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBaseHostEdit(), (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBaseUserEdit(), (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBasePortEdit(), INTEGER_VALIDATOR);
        validation.initInitialDecoration();

        profileNameValidation.registerValidator(profileNameEdit, (Control control, String value) -> {
            var selected = getSelectedProfile().orElse(null);

            return ValidationResult.fromErrorIf(control, null,
                    profileListView.getItems().stream().anyMatch(p -> p != selected && p.getName().equals(value)));
        });

        profileNameValidation.initInitialDecoration();
    }

    private BorderPane initLeftPane() {
        var pane = new BorderPane();

        var titled = new TitledPane(RB.getString("text.Profiles"), profileListView);
        titled.setCollapsible(false);
        titled.setMaxHeight(Double.MAX_VALUE);

        pane.setCenter(titled);
        return pane;
    }

    private VBox initCenterPane() {
        var pane = new VBox();

        var hBox = new HBox(new Label(RB.getString("label.ProfileName")), profileNameEdit);
        hBox.setAlignment(Pos.CENTER_LEFT);

        var titled = new TitledPane(RB.getString("text.Connection"), tcpEditor);
        titled.setCollapsible(false);

        var encryptionPane = new TitledPane(RB.getString("label.Encryption.Key"), encryptionKeyEditor);
        encryptionPane.setCollapsible(false);

        pane.getChildren().addAll(hBox, titled, encryptionPane, testStatusLabel);

        HBox.setHgrow(profileNameEdit, Priority.ALWAYS);
        HBox.setMargin(profileNameEdit, new Insets(0.0, 0.0, 10.0, 5.0));
        VBox.setMargin(titled, new Insets(0.0, 0.0, 10.0, 0.0));
        VBox.setMargin(encryptionPane, new Insets(0.0, 0.0, 10.0, 0.0));

        return pane;
    }
}
