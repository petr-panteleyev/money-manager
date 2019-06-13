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

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static org.panteleyev.money.BaseTestUtils.randomCategoryType;
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
        .catTypeId(CategoryType.BANKS_AND_CASH.getId())
        .guid(UUID.randomUUID())
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
            var dialog = new CategoryDialog(null);
            setupDialog(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var category = queue.take();

        assertNotNull(category.getUuid());
        assertEquals(category.getName(), CATEGORY_NAME);
        assertEquals(category.getComment(), CATEGORY_COMMENT);
        assertEquals(category.getType(), CATEGORY_TYPE);
        assertEquals(category.getCreated(), category.getModified());
    }

    @Test
    public void testExistingCategory() throws Exception {
        BlockingQueue<Category> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new CategoryDialog(CATEGORY);
            setupDialogUpdate(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var category = queue.take();

        assertEquals(category.getUuid(), CATEGORY.getUuid());
        assertEquals(category.getName(), CATEGORY.getName());
        assertEquals(category.getComment(), CATEGORY.getComment());
        assertEquals(category.getType(), CATEGORY_TYPE_NEW);
        assertTrue(category.getModified() > CATEGORY.getModified());
        assertTrue(category.getModified() > category.getCreated());
    }
}
