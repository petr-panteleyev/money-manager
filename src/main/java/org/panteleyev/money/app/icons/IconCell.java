package org.panteleyev.money.app.icons;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.Icon;

class IconCell extends BorderPane {
    private final Icon icon;

    IconCell(Icon icon, IconWindowController parent) {
        this.icon = icon;

        setOnMouseClicked(event -> parent.onSelect(this));

        setCenter(new ImageView(icon.getImage()));

        setPrefHeight(40);
        setPrefWidth(40);
    }

    Icon getIcon() {
        return icon;
    }
}