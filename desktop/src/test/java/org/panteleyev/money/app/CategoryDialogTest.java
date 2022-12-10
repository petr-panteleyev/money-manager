/*
 Copyright © 2019-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.test.BaseTestUtils.randomCategoryType;

public class CategoryDialogTest {
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

    @BeforeAll
    public static void init() {
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
        assertEquals(CATEGORY_NAME, category.name());
        assertEquals(CATEGORY_COMMENT, category.comment());
        assertEquals(CATEGORY_TYPE, category.type());
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

        assertEquals(CATEGORY.uuid(), category.uuid());
        assertEquals(CATEGORY.name(), category.name());
        assertEquals(CATEGORY.comment(), category.comment());
        assertEquals(CATEGORY_TYPE_NEW, category.type());
        assertTrue(category.modified() > CATEGORY.modified());
        assertTrue(category.modified() > category.created());
    }
}
