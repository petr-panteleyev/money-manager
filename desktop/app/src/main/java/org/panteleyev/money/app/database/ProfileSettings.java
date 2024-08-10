/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

import org.panteleyev.commons.xml.StartElementWrapper;
import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.commons.xml.XMLStreamWriterWrapper;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

record ProfileSettings(Collection<ConnectionProfile> profiles, String defaultProfile, boolean autoConnect) {
    private static final QName ROOT = new QName("settings");
    private static final QName AUTO_CONNECT = new QName("autoConnect");
    private static final QName DEFAULT_PROFILE = new QName("defaultProfile");
    private static final QName PROFILES_ROOT = new QName("profiles");
    private static final QName PROFILE_ELEMENT = new QName("profile");
    private static final QName PROFILE_NAME = new QName("name");
    private static final QName PROFILE_HOST = new QName("host");
    private static final QName PROFILE_PORT = new QName("port");
    private static final QName PROFILE_SCHEMA = new QName("schema");
    private static final QName PROFILE_USER = new QName("user");
    private static final QName PROFILE_PASSWORD = new QName("password");
    private static final QName PROFILE_DATABASE = new QName("database");

    public void save(OutputStream out) {
        try (var writer = XMLStreamWriterWrapper.newInstance(out)) {
            writer.document(ROOT, () -> {
                writer.textElement(AUTO_CONNECT, autoConnect());
                writer.textElement(DEFAULT_PROFILE, defaultProfile());
                writer.element(PROFILES_ROOT, () -> {
                    for (var profile : profiles()) {
                        serialize(writer, profile);
                    }
                });
            });
        }
    }

    static ProfileSettings load(InputStream in) {
        try (var reader = XMLEventReaderWrapper.newInstance(in)) {
            var autoConnect = new AtomicBoolean(false);
            var defaultProfile = new AtomicReference<>("");
            var profiles = new ArrayList<ConnectionProfile>();

            while (reader.hasNext()) {
                var event = reader.nextEvent();

                event.ifStartElement(AUTO_CONNECT, _ -> autoConnect.set(reader.getElementValue(false)));
                event.ifStartElement(DEFAULT_PROFILE, _ -> reader.getElementText().ifPresent(defaultProfile::set));
                event.ifStartElement(PROFILE_ELEMENT, element -> profiles.add(deserialize(element)));
            }

            return new ProfileSettings(
                    profiles,
                    defaultProfile.get(),
                    autoConnect.get()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void serialize(XMLStreamWriterWrapper writer, ConnectionProfile profile) {
        writer.element(PROFILE_ELEMENT, Map.of(
                PROFILE_NAME, profile.name(),
                PROFILE_HOST, profile.dataBaseHost(),
                PROFILE_PORT, Integer.toString(profile.dataBasePort()),
                PROFILE_USER, profile.dataBaseUser(),
                PROFILE_PASSWORD, profile.dataBasePassword(),
                PROFILE_DATABASE, profile.databaseName(),
                PROFILE_SCHEMA, profile.schema()
        ));
    }

    private static ConnectionProfile deserialize(StartElementWrapper element) {
        return new ConnectionProfile(
                element.getAttributeValue(PROFILE_NAME).orElseThrow(),
                element.getAttributeValue(PROFILE_HOST, "localhost"),
                element.getAttributeValue(PROFILE_PORT, 3306),
                element.getAttributeValue(PROFILE_USER).orElseThrow(),
                element.getAttributeValue(PROFILE_PASSWORD).orElseThrow(),
                element.getAttributeValue(PROFILE_DATABASE).orElseThrow(),
                element.getAttributeValue(PROFILE_SCHEMA).orElseThrow()
        );
    }
}
