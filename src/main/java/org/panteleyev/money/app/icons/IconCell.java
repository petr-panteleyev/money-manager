/*
 Copyright (C) 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.icons;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.Icon;

import static org.panteleyev.money.app.icons.IconManager.getImage;

class IconCell extends BorderPane {
    private final Icon icon;

    IconCell(Icon icon, IconWindowController parent) {
        this.icon = icon;

        setOnMouseClicked(event -> parent.onSelect(this));

        setCenter(new ImageView(getImage(icon.uuid())));

        setPrefHeight(40);
        setPrefWidth(40);
    }

    Icon getIcon() {
        return icon;
    }
}
