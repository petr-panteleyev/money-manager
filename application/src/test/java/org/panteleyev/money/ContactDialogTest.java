/*
 * Copyright (c) 2019, 2020, Petr Panteleyev <petr@panteleyev.org>
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
        .typeId(randomContactType().getId())
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
        dialog.getNameField().setText(CONTACT.getName());
        dialog.getPhoneField().setText(CONTACT.getPhone());
        dialog.getMobileField().setText(CONTACT.getMobile());
        dialog.getEmailField().setText(CONTACT.getEmail());
        dialog.getWebField().setText(CONTACT.getWeb());
        dialog.getCommentEdit().setText(CONTACT.getComment());
        dialog.getStreetField().setText(CONTACT.getStreet());
        dialog.getCityField().setText(CONTACT.getCity());
        dialog.getCountryField().setText(CONTACT.getCountry());
        dialog.getZipField().setText(CONTACT.getZip());
        dialog.getTypeChoiceBox().getSelectionModel().select(CONTACT.getType());
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

        assertNotNull(contact.getUuid());
        assertContact(contact);
        assertEquals(contact.getCreated(), contact.getModified());
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

        assertEquals(contact.getUuid(), CONTACT.getUuid());
        assertContact(contact);
        assertTrue(contact.getModified() > CONTACT.getModified());
        assertTrue(contact.getModified() > contact.getCreated());
    }

    private static void assertContact(Contact contact) {
        assertEquals(contact.getName(), CONTACT.getName());
        assertEquals(contact.getPhone(), CONTACT.getPhone());
        assertEquals(contact.getMobile(), CONTACT.getMobile());
        assertEquals(contact.getEmail(), CONTACT.getEmail());
        assertEquals(contact.getWeb(), CONTACT.getWeb());
        assertEquals(contact.getComment(), CONTACT.getComment());
        assertEquals(contact.getStreet(), CONTACT.getStreet());
        assertEquals(contact.getCity(), CONTACT.getCity());
        assertEquals(contact.getCountry(), CONTACT.getCountry());
        assertEquals(contact.getZip(), CONTACT.getZip());
        assertEquals(contact.getType(), CONTACT.getType());
    }
}
