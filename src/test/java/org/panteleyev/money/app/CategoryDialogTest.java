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
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static org.panteleyev.money.test.BaseTestUtils.randomCategoryType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CategoryDialogTest extends BaseTest {
    private static final String CATEGORY_NAME = UUID.randomUUID().toString();
    private static final String CATEGORY_COMMENT = UUID.randomUUID().toString();
    private static final CategoryType CATEGORY_TYPE = randomCategoryType();
    private static final CategoryType CATEGORY_TYPE_NEW = randomCategoryType();

    private final static Category CATEGORY = new Category.Builder()
        .name(UUID.randomUUID().toString())
        .comment(UUID.randomUUID().toString())
        .type(CategoryType.BANKS_AND_CASH)
        .uuid(UUID.randomUUID())
        .modified(System.currentTimeMillis())
        .build();

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private void setupDialog(CategoryDialog dialog) {
        dialog.getNameEdit().setText(CATEGORY_NAME);
        dialog.getCommentEdit().setText(CATEGORY_COMMENT);
        dialog.getTypeComboBox().getSelectionModel().select(CATEGORY_TYPE);
    }

    private void setupDialogUpdate(CategoryDialog dialog) {
        dialog.getTypeComboBox().getSelectionModel().select(CATEGORY_TYPE_NEW);
    }

    @Test
    public void testNewCategory() throws Exception {
        BlockingQueue<Category> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new CategoryDialog(null, null, null);
            setupDialog(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var category = queue.take();

        assertNotNull(category.uuid());
        assertEquals(category.name(), CATEGORY_NAME);
        assertEquals(category.comment(), CATEGORY_COMMENT);
        assertEquals(category.type(), CATEGORY_TYPE);
        assertEquals(category.created(), category.modified());
    }

    @Test
    public void testExistingCategory() throws Exception {
        BlockingQueue<Category> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new CategoryDialog(null, null, CATEGORY);
            setupDialogUpdate(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var category = queue.take();

        assertEquals(category.uuid(), CATEGORY.uuid());
        assertEquals(category.name(), CATEGORY.name());
        assertEquals(category.comment(), CATEGORY.comment());
        assertEquals(category.type(), CATEGORY_TYPE_NEW);
        assertTrue(category.modified() > CATEGORY.modified());
        assertTrue(category.modified() > category.created());
    }
}
