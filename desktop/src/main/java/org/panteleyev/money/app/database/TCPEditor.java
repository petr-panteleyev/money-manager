/*
 Copyright © 2020-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.money.app.Images;

import java.util.List;
import java.util.function.Consumer;

import static javafx.event.ActionEvent.ACTION;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.grid.GridBuilder.columnConstraints;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.app.database.ConnectionProfilesEditor.DEFAULT_DATABASE;
import static org.panteleyev.money.app.database.ConnectionProfilesEditor.DEFAULT_SCHEMA;

final class TCPEditor extends VBox {
    private final TextField schemaEdit = initSchemaEdit();
    private final TextField databaseNameEdit = new TextField();
    private final TextField dataBaseHostEdit = new TextField();
    private final TextField dataBasePortEdit = new TextField();
    private final TextField dataBaseUserEdit = new TextField();
    private final PasswordField dataBasePasswordEdit = new PasswordField();

    TCPEditor(ValidationSupport validation, Consumer<ActionEvent> resetSchemaHandler) {
        var resetSchemaButton = button("Сбросить");
        resetSchemaButton.setGraphic(new ImageView(Images.WARNING));
        resetSchemaButton.disableProperty().bind(validation.invalidProperty());
        resetSchemaButton.addEventFilter(ACTION, resetSchemaHandler::accept);

        getChildren().addAll(gridPane(
                List.of(
                        gridRow(label("Сервер:"), dataBaseHostEdit, label("Порт:"), dataBasePortEdit),
                        gridRow(label("Логин:"), gridCell(dataBaseUserEdit, 3, 1)),
                        gridRow(label("Пароль:"), gridCell(dataBasePasswordEdit, 3, 1)),
                        gridRow(label("База данных:"), gridCell(databaseNameEdit, 3, 1)),
                        gridRow(label("Схема:"), gridCell(schemaEdit, 2, 1), resetSchemaButton)
                ), b -> b.withStyle(GRID_PANE)
                        .withConstraints(columnConstraints(Priority.NEVER), columnConstraints(Priority.ALWAYS))
        ));

        VBox.setMargin(getChildren().getFirst(), new Insets(10.0, 10.0, 10.0, 10.0));
    }

    TextField getSchemaEdit() {
        return schemaEdit;
    }

    TextField getDatabaseNameEdit() {
        return databaseNameEdit;
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

    String getSchema() {
        return schemaEdit.getText();
    }

    void setSchema(String schema) {
        schemaEdit.setText(schema);
    }

    String getDatabaseName() {
        return databaseNameEdit.getText();
    }

    void setDatabaseName(String databaseName) {
        databaseNameEdit.setText(databaseName);
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

    private TextField initSchemaEdit() {
        var schemaEdit = new TextField();
        schemaEdit.setMaxWidth(Double.MAX_VALUE);
        return schemaEdit;
    }

    void setProfile(ConnectionProfile profile) {
        if (profile != null) {
            setDataBaseHost(profile.dataBaseHost());
            setDataBasePort(profile.dataBasePort());
            setDataBaseUser(profile.dataBaseUser());
            setDataBasePassword(profile.dataBasePassword());
            setDatabaseName(profile.databaseName());
            setSchema(profile.schema());
        } else {
            setDataBaseHost("localhost");
            setDataBasePort(5432);
            setDataBaseUser("");
            setDataBasePassword("");
            setDatabaseName(DEFAULT_DATABASE);
            setSchema(DEFAULT_SCHEMA);
        }
    }
}
