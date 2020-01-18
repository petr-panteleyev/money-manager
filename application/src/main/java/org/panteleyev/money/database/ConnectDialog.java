/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.database;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.ToStringConverter;
import java.util.Objects;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.Constants.COLON;
import static org.panteleyev.money.MainWindowController.CSS_PATH;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.Styles.GRID_PANE;

public class ConnectDialog extends BaseDialog<ConnectionProfile> {
    private final ComboBox<ConnectionProfile> profileComboBox;
    private final CheckBox defaultCheck;
    private final CheckBox autoConnectCheck;

    private final ConnectionProfileManager profileManager;

    public ConnectDialog(ConnectionProfileManager profileManager) {
        super(CSS_PATH);
        Objects.requireNonNull(profileManager);

        this.profileManager = profileManager;

        profileComboBox = initProfileComboBox();
        defaultCheck = initDefaultCheck();
        autoConnectCheck = initAutoConnectCheck();

        setTitle(RB.getString("Connection"));

        var pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);
        pane.addRow(0, newLabel(RB, "Profile", COLON), profileComboBox);
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
                    profileManager.setDefaultProfile(selected);
                }
                profileManager.setAutoConnect(autoConnectCheck.isSelected());
                profileManager.saveProfiles();
                return selected;
            } else {
                return null;
            }
        });
    }

    private ComboBox<ConnectionProfile> initProfileComboBox() {
        var cb = new ComboBox<ConnectionProfile>();

        cb.setItems(FXCollections.observableArrayList(profileManager.getAll()));

        var defaultProfile = profileManager.getDefaultProfile();
        defaultProfile.ifPresentOrElse(p -> cb.getSelectionModel().select(p), () -> {
            if (profileManager.size() > 0) {
                cb.getSelectionModel().select(0);
            }
        });

        cb.setConverter(new ToStringConverter<>() {
            public String toString(ConnectionProfile profile) {
                return profile != null ? profile.getName() : "";
            }
        });

        cb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            defaultCheck.setSelected(Objects.equals(profileManager.getDefaultProfile().orElse(null), newValue));
            autoConnectCheck.setSelected(defaultCheck.isSelected() && profileManager.getAutoConnect());
        });

        return cb;
    }

    private CheckBox initDefaultCheck() {
        var check = newCheckBox(RB, "Default_Profile");
        check.setSelected(Objects.equals(profileManager.getDefaultProfile().orElse(null),
            profileComboBox.getSelectionModel().getSelectedItem()));
        return check;
    }

    private CheckBox initAutoConnectCheck() {
        var check = newCheckBox(RB, "Connect_at_startup");
        check.disableProperty().bind(defaultCheck.selectedProperty().not());
        check.setSelected(defaultCheck.isSelected() && profileManager.getAutoConnect());
        return check;
    }
}
