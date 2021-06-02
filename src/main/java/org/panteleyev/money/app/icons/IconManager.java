/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Function;
import static org.panteleyev.money.persistence.DataCache.cache;

public class IconManager {
    public static final Function<Category, Image> CATEGORY_TO_IMAGE = category -> IconManager.getImage(category.iconUuid());
    public static final Function<Account, Image> ACCOUNT_TO_IMAGE = account -> IconManager.getImage(account.iconUuid());

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

    public static Image getImage(UUID uuid) {
        return (uuid == null ? EMPTY_ICON : cache().getIcon(uuid).orElse(EMPTY_ICON)).getImage();
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
            .sorted(Comparator.comparing(Icon::getName))
            .toList()
        ));
    }
}
