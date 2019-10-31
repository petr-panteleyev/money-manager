/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.icons;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import org.panteleyev.money.Images;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Icon;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.money.persistence.DataCache.cache;

public class IconManager {
    final static class IconListCell extends ListCell<Icon> {
        @Override
        public void updateItem(Icon item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(new ImageView(item.getImage()));
            }
        }
    }

    public static final Icon EMPTY_ICON = new Icon(null, "-", null, Images.EMPTY, 0, 0);

    private IconManager() {
    }

    public static ImageView getImageView(UUID uuid) {
        var icon = uuid == null ? EMPTY_ICON : cache().getIcon(uuid).orElse(EMPTY_ICON);
        return new ImageView(icon.getImage());
    }

    public static ImageView getAccountImageView(Account account) {
        var uuid = account.getIconUuid();
        if (uuid == null) {
            uuid = cache().getCategory(account.getCategoryUuid()).map(Category::getIconUuid).orElse(null);
        }

        return getImageView(uuid);
    }

    public static void setupComboBox(ComboBox<Icon> comboBox) {
        comboBox.setCellFactory(p -> new IconListCell());
        comboBox.setButtonCell(new IconListCell());

        comboBox.getItems().clear();
        comboBox.getItems().add(EMPTY_ICON);
        comboBox.getItems().addAll(FXCollections.observableArrayList(cache().getIcons().stream()
            .sorted(Comparator.comparing(Icon::getName))
            .collect(Collectors.toList()))
        );
    }
}
