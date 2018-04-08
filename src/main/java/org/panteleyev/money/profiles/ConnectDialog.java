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

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.panteleyev.money.MainWindowController;
import org.panteleyev.money.Styles;
import org.panteleyev.money.ToStringConverter;
import org.panteleyev.utilities.fx.BaseDialog;
import java.util.Objects;
import java.util.ResourceBundle;

public class ConnectDialog extends BaseDialog<ConnectionProfile> {
    private static final ResourceBundle RB = MainWindowController.RB;

    private final ComboBox<ConnectionProfile> profileComboBox = initProfileComboBox();
    private final CheckBox defaultCheck = initDefaultCheck();
    private final CheckBox autoConnectCheck = initAutoConnectCheck();

    public ConnectDialog() {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("connection.Dialog.Title"));

        var pane = new GridPane();
        pane.getStyleClass().add(Styles.GRID_PANE);
        pane.addRow(0, new Label(RB.getString("label.Profile")), profileComboBox);
        pane.addRow(1, defaultCheck);
        pane.addRow(2, autoConnectCheck);
        getDialogPane().setContent(pane);

        GridPane.setColumnSpan(defaultCheck, 2);
        GridPane.setColumnSpan(autoConnectCheck, 2);

        profileComboBox.setMaxWidth(Double.MAX_VALUE);

        createDefaultButtons(RB);

        setResultConverter(b -> {
            if (b == ButtonType.OK) {
                ConnectionProfile selected = profileComboBox.getSelectionModel().getSelectedItem();
                if (defaultCheck.isSelected()) {
                    ConnectionProfileManager.setDefaultProfile(selected);
                }
                ConnectionProfileManager.setAutoConnect(autoConnectCheck.isSelected());
                ConnectionProfileManager.saveProfiles();
                return selected;
            } else {
                return null;
            }
        });
    }

    private ComboBox<ConnectionProfile> initProfileComboBox() {
        var cb = new ComboBox<ConnectionProfile>();

        cb.setItems(FXCollections.observableArrayList(ConnectionProfileManager.getAll()));

        var defaultProfile = ConnectionProfileManager.getDefaultProfile();
        if (defaultProfile != null) {
            cb.getSelectionModel().select(defaultProfile);
        } else if (ConnectionProfileManager.size() > 0) {
            cb.getSelectionModel().select(0);
        }

        cb.setConverter(new ToStringConverter<>() {
            public String toString(ConnectionProfile profile) {
                return profile != null ? profile.getName() : "";
            }
        });

        cb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            defaultCheck.setSelected(Objects.equals(ConnectionProfileManager.getDefaultProfile(), newValue));
            autoConnectCheck.setSelected(defaultCheck.isSelected() && ConnectionProfileManager.getAutoConnect());
        });

        return cb;
    }

    private CheckBox initDefaultCheck() {
        var check = new CheckBox(RB.getString("check.Default.Profile"));
        check.setSelected(Objects.equals(ConnectionProfileManager.getDefaultProfile(),
                profileComboBox.getSelectionModel().getSelectedItem()));
        return check;
    }

    private CheckBox initAutoConnectCheck() {
        var check = new CheckBox(RB.getString("connect.Dialog.autoCheck"));
        check.disableProperty().bind(defaultCheck.selectedProperty().not());
        check.setSelected(defaultCheck.isSelected() && ConnectionProfileManager.getAutoConnect());
        return check;
    }
}
