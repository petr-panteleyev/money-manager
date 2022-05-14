/*
 Copyright (c) 2017-2022, Petr Panteleyev

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

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.panteleyev.money.app.Images;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Icon;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import static org.panteleyev.money.app.GlobalContext.cache;

public class IconManager {
    public static final int ICON_SIZE = 16;
    public static final int ICON_BYTE_LENGTH = 8192;

    public static final Function<Category, Image> CATEGORY_TO_IMAGE = category -> IconManager.getImage(category.iconUuid());
    public static final Function<Account, Image> ACCOUNT_TO_IMAGE = account -> IconManager.getImage(account.iconUuid());

    private static final Map<Icon, Image> imageMap = new ConcurrentHashMap<>();

    final static class IconListCell extends ListCell<Icon> {
        @Override
        public void updateItem(Icon item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(new ImageView(getImage(item.uuid())));
            }
        }
    }

    public static final Icon EMPTY_ICON = new Icon(UUID.randomUUID(), "-", new byte[0], 0, 0);

    private IconManager() {
    }

    public static Image getImage(UUID uuid) {
        var icon = uuid == null ? EMPTY_ICON : cache().getIcon(uuid).orElse(EMPTY_ICON);
        return imageMap.computeIfAbsent(icon, key -> {
            if (key.equals(EMPTY_ICON)) {
                return Images.EMPTY;
            }
            try (var inputStream = new ByteArrayInputStream(key.bytes())) {
                return new Image(inputStream, ICON_SIZE, ICON_SIZE, true, true);
            } catch (IOException ex) {
                return Images.EMPTY;
            }
        });
    }

    public static ImageView getImageView(UUID uuid) {
        return new ImageView(getImage(uuid));
    }

    public static ImageView getAccountImageView(Account account) {
        var uuid = account.iconUuid();
        if (uuid == null) {
            uuid = cache().getCategory(account.categoryUuid()).map(Category::iconUuid).orElse(null);
        }

        return getImageView(uuid);
    }

    public static void setupComboBox(ComboBox<Icon> comboBox) {
        comboBox.setCellFactory(p -> new IconListCell());
        comboBox.setButtonCell(new IconListCell());

        comboBox.getItems().clear();
        comboBox.getItems().add(EMPTY_ICON);
        comboBox.getItems().addAll(FXCollections.observableArrayList(cache().getIcons().stream()
            .sorted(Comparator.comparing(Icon::name))
            .toList()
        ));
    }
}
