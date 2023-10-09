/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.contact;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Contact;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.test.BaseTestUtils.randomContactType;
import static org.panteleyev.money.test.BaseTestUtils.randomString;

public class ContactDialogTest {
    private final static Contact CONTACT = new Contact.Builder()
            .uuid(UUID.randomUUID())
            .name(randomString())
            .type(randomContactType())
            .phone(randomString())
            .mobile(randomString())
            .email(randomString())
            .web(randomString())
            .comment(randomString())
            .street(randomString())
            .city(randomString())
            .country(randomString())
            .zip(randomString())
            .created(System.currentTimeMillis())
            .modified(System.currentTimeMillis())
            .build();

    @BeforeAll
    public static void init() {
        new JFXPanel();
    }

    private void setupDialog(ContactDialog dialog) {
        dialog.getNameField().setText(CONTACT.name());
        dialog.getPhoneField().setText(CONTACT.phone());
        dialog.getMobileField().setText(CONTACT.mobile());
        dialog.getEmailField().setText(CONTACT.email());
        dialog.getWebField().setText(CONTACT.web());
        dialog.getCommentEdit().setText(CONTACT.comment());
        dialog.getStreetField().setText(CONTACT.street());
        dialog.getCityField().setText(CONTACT.city());
        dialog.getCountryField().setText(CONTACT.country());
        dialog.getZipField().setText(CONTACT.zip());
        dialog.getTypeBox().getSelectionModel().select(CONTACT.type());
    }

    @Test
    public void testNewContact() throws Exception {
        var queue = new ArrayBlockingQueue<Contact>(1);

        Platform.runLater(() -> {
            var dialog = new ContactDialog(null, null, null, cache());
            setupDialog(dialog);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var contact = queue.take();

        assertNotNull(contact.uuid());
        assertContact(contact);
        assertEquals(contact.created(), contact.modified());
    }

    @Test
    public void testExistingCurrency() throws Exception {
        var queue = new ArrayBlockingQueue<Contact>(1);

        Platform.runLater(() -> {
            var dialog = new ContactDialog(null, null, CONTACT, cache());
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var contact = queue.take();

        assertEquals(CONTACT.uuid(), contact.uuid());
        assertContact(contact);
        assertTrue(contact.modified() > CONTACT.modified());
        assertTrue(contact.modified() > contact.created());
    }

    private static void assertContact(Contact contact) {
        assertEquals(CONTACT.name(), contact.name());
        assertEquals(CONTACT.phone(), contact.phone());
        assertEquals(CONTACT.mobile(), contact.mobile());
        assertEquals(CONTACT.email(), contact.email());
        assertEquals(CONTACT.web(), contact.web());
        assertEquals(CONTACT.comment(), contact.comment());
        assertEquals(CONTACT.street(), contact.street());
        assertEquals(CONTACT.city(), contact.city());
        assertEquals(CONTACT.country(), contact.country());
        assertEquals(CONTACT.zip(), contact.zip());
        assertEquals(CONTACT.type(), contact.type());
    }
}
