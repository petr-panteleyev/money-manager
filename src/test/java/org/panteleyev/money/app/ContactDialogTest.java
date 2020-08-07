/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import static org.panteleyev.money.test.BaseTestUtils.randomContactType;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ContactDialogTest extends BaseTest {
    private final static Contact CONTACT = new Contact.Builder()
        .guid(UUID.randomUUID())
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

    @BeforeClass
    public void setupAndSkip() {
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
            var dialog = new ContactDialog(null, null);
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
            var dialog = new ContactDialog(null, CONTACT);
            var category = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(category);
        });

        var contact = queue.take();

        assertEquals(contact.uuid(), CONTACT.uuid());
        assertContact(contact);
        assertTrue(contact.modified() > CONTACT.modified());
        assertTrue(contact.modified() > contact.created());
    }

    private static void assertContact(Contact contact) {
        assertEquals(contact.name(), CONTACT.name());
        assertEquals(contact.phone(), CONTACT.phone());
        assertEquals(contact.mobile(), CONTACT.mobile());
        assertEquals(contact.email(), CONTACT.email());
        assertEquals(contact.web(), CONTACT.web());
        assertEquals(contact.comment(), CONTACT.comment());
        assertEquals(contact.street(), CONTACT.street());
        assertEquals(contact.city(), CONTACT.city());
        assertEquals(contact.country(), CONTACT.country());
        assertEquals(contact.zip(), CONTACT.zip());
        assertEquals(contact.type(), CONTACT.type());
    }
}
