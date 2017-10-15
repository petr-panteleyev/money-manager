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

package org.panteleyev.money.profiles;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.panteleyev.money.MainWindowController;
import org.panteleyev.money.Styles;
import java.util.Collections;
import java.util.ResourceBundle;

class TCPEditor extends VBox {
    private static final ResourceBundle RB = MainWindowController.RB;

    private final ComboBox<ConnectionType> typeList = initTypeList();
    private final TextField schemaEdit = initSchemaEdit();
    private final TextField dataBaseHostEdit = new TextField();
    private final TextField dataBasePortEdit = new TextField();
    private final TextField dataBaseUserEdit = new TextField();
    private final PasswordField dataBasePasswordEdit = new PasswordField();

    // SSH parameters
    private final TextField sshHostEdit = new TextField();
    private final TextField sshPortEdit = new TextField();
    private final TextField sshUserEdit = new TextField();
    private final PasswordField sshPasswordEdit = new PasswordField();
    private final Label sshKeyFileLabel = new Label("SSH Key File:");
    private final TextField sshKeyFileEdit = new TextField();
    private final Button sshKeyFileButton = new Button("...");

    TCPEditor() {
        HBox typePane = new HBox(new Label(RB.getString("label.ConnectionMethod")), typeList);
        typePane.setAlignment(Pos.CENTER_LEFT);

        HBox keyFilePane = new HBox(sshKeyFileEdit, sshKeyFileButton);

        HBox.setMargin(typeList, new Insets(0.0, 0.0, 0.0, 5.0));

        GridPane mySqlGrid = new GridPane();
        mySqlGrid.getStyleClass().add(Styles.GRID_PANE);

        mySqlGrid.addRow(0, new Label(RB.getString("label.Host")), dataBaseHostEdit,
                new Label(RB.getString("label.Port")), dataBasePortEdit);
        mySqlGrid.addRow(1, new Label(RB.getString("label.User")), dataBaseUserEdit);
        mySqlGrid.addRow(2, new Label(RB.getString("label.Password")), dataBasePasswordEdit);
        mySqlGrid.addRow(3, new Label(RB.getString("label.Schema")), schemaEdit);

        mySqlGrid.getColumnConstraints().addAll(newColumnConstraints(Priority.NEVER),
                newColumnConstraints(Priority.ALWAYS));

        TitledPane mySqlPane = new TitledPane("MySQL", mySqlGrid);
        mySqlPane.setCollapsible(false);

        GridPane sshGrid = new GridPane();
        sshGrid.getStyleClass().add(Styles.GRID_PANE);

        sshGrid.addRow(0, new Label(RB.getString("label.Host")), sshHostEdit,
                new Label(RB.getString("label.Port")), sshPortEdit);
        sshGrid.addRow(1, new Label(RB.getString("label.User")), sshUserEdit);
        sshGrid.addRow(2, new Label(RB.getString("label.Password")), sshPasswordEdit);
        sshGrid.addRow(3, sshKeyFileLabel, keyFilePane);

        sshGrid.getColumnConstraints().addAll(newColumnConstraints(Priority.NEVER),
                newColumnConstraints(Priority.ALWAYS));

        TitledPane sshPane = new TitledPane("SSH", sshGrid);
        sshPane.setCollapsible(false);
        sshPane.visibleProperty().bind(typeList.getSelectionModel().selectedItemProperty()
                .isEqualTo(ConnectionType.TCP_OVER_SSH));

        getChildren().addAll(typePane, mySqlPane, sshPane);
        VBox.setMargin(typePane, new Insets(10.0, 10.0, 5.0, 10.0));
        VBox.setMargin(mySqlPane, new Insets(5.0, 10.0, 5.0, 10.0));
        VBox.setMargin(sshPane, new Insets(5.0, 10.0, 10.0, 10.0));

        GridPane.setColumnSpan(dataBaseUserEdit, 3);
        GridPane.setColumnSpan(dataBasePasswordEdit, 3);
        GridPane.setColumnSpan(schemaEdit, 3);
        GridPane.setColumnSpan(sshUserEdit, 3);
        GridPane.setColumnSpan(sshPasswordEdit, 3);
        GridPane.setColumnSpan(keyFilePane, 3);
    }

    TextField getSchemaEdit() {
        return schemaEdit;
    }

    TextField getDataBaseHostEdit() {
        return dataBaseHostEdit;
    }

    TextField getDataBasePortEdit() {
        return dataBasePortEdit;
    }

    TextField getDataBaseUserEdit() {
        return dataBaseUserEdit;
    }

    ConnectionType getType() {
        return typeList.getSelectionModel().getSelectedItem();
    }

    void setType(ConnectionType type) {
        typeList.getSelectionModel().select(type);
    }

    String getSchema() {
        return schemaEdit.getText();
    }

    void setSchema(String schema) {
        schemaEdit.setText(schema);
    }

    String getDataBaseHost() {
        return dataBaseHostEdit.getText();
    }

    void setDataBaseHost(String host) {
        dataBaseHostEdit.setText(host);
    }

    int getDataBasePort() {
        return Integer.parseInt(dataBasePortEdit.getText());
    }

    void setDataBasePort(int port) {
        dataBasePortEdit.setText(Integer.toString(port));
    }

    String getDataBaseUser() {
        return dataBaseUserEdit.getText();
    }

    void setDataBaseUser(String user) {
        dataBaseUserEdit.setText(user);
    }

    String getDataBasePassword() {
        return dataBasePasswordEdit.getText();
    }

    void setDataBasePassword(String password) {
        dataBasePasswordEdit.setText(password);
    }

    private static ComboBox<ConnectionType> initTypeList() {
        ComboBox<ConnectionType> typeList = new ComboBox<>();
        //typeList.setItems(FXCollections.observableArrayList(ConnectionType.values()));
        typeList.setItems(FXCollections.observableArrayList(Collections.singletonList(ConnectionType.TCP_IP)));
        typeList.getSelectionModel().select(0);
        return typeList;
    }

    private TextField initSchemaEdit() {
        TextField schemaEdit = new TextField();
        schemaEdit.setMaxWidth(Double.MAX_VALUE);
        return schemaEdit;
    }

    private static ColumnConstraints newColumnConstraints(Priority hGrow) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setHgrow(hGrow);
        return constraints;
    }
}
