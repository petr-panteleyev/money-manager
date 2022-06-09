/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

import org.panteleyev.money.xml.XMLUtils;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import static org.panteleyev.money.xml.XMLUtils.appendElement;
import static org.panteleyev.money.xml.XMLUtils.appendTextNode;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

record ProfileSettings(Collection<ConnectionProfile> profiles, String defaultProfile, boolean autoConnect) {
    private static final String ROOT = "settings";
    private static final String AUTO_CONNECT = "autoConnect";
    private static final String DEFAULT_PROFILE = "defaultProfile";
    private static final String PROFILES_ROOT = "profiles";
    private static final String PROFILE_ELEMENT = "profile";
    private static final String PROFILE_NAME = "name";
    private static final String PROFILE_HOST = "host";
    private static final String PROFILE_PORT = "port";
    private static final String PROFILE_SCHEMA = "schema";
    private static final String PROFILE_USER = "user";
    private static final String PROFILE_PASSWORD = "password";

    public void save(OutputStream out) {
        var root = createDocument(ROOT);
        appendTextNode(root, AUTO_CONNECT, autoConnect());
        appendTextNode(root, DEFAULT_PROFILE, defaultProfile());
        var profileRoot = appendElement(root, PROFILES_ROOT);
        for (var p : profiles()) {
            serialize(profileRoot, p);
        }
        writeDocument(root.getOwnerDocument(), out);
    }

    static ProfileSettings load(InputStream in) {
        try {
            var rootElement = readDocument(in);

            var autoConnect = false;
            var defaultProfile = "";

            var autoConnectElementList = rootElement.getElementsByTagName(AUTO_CONNECT);
            if (autoConnectElementList.getLength() == 1) {
                autoConnect = Boolean.parseBoolean(autoConnectElementList.item(0).getTextContent());
            }

            var defaultProfileNodeList = rootElement.getElementsByTagName(DEFAULT_PROFILE);
            if (defaultProfileNodeList.getLength() == 1) {
                defaultProfile = defaultProfileNodeList.item(0).getTextContent();
            }

            var profileElements = rootElement.getElementsByTagName(PROFILE_ELEMENT);
            var profiles = new ArrayList<ConnectionProfile>(profileElements.getLength());
            for (int i = 0; i < profileElements.getLength(); i++) {
                if (profileElements.item(i) instanceof Element element) {
                    profiles.add(deserializeConnectionProfile(element));
                }
            }

            return new ProfileSettings(
                    profiles,
                    defaultProfile,
                    autoConnect
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void serialize(Element parent, ConnectionProfile profile) {
        var element = appendElement(parent, PROFILE_ELEMENT);
        element.setAttribute(PROFILE_NAME, profile.name());
        element.setAttribute(PROFILE_HOST, profile.dataBaseHost());
        element.setAttribute(PROFILE_PORT, Integer.toString(profile.dataBasePort()));
        element.setAttribute(PROFILE_USER, profile.dataBaseUser());
        element.setAttribute(PROFILE_PASSWORD, profile.dataBasePassword());
        element.setAttribute(PROFILE_SCHEMA, profile.schema());
    }

    private static ConnectionProfile deserializeConnectionProfile(Element element) {
        return new ConnectionProfile(
                element.getAttribute(PROFILE_NAME),
                XMLUtils.getAttribute(element, PROFILE_HOST, "localhost"),
                XMLUtils.getAttribute(element, PROFILE_PORT, 3306),
                element.getAttribute(PROFILE_USER),
                element.getAttribute(PROFILE_PASSWORD),
                element.getAttribute(PROFILE_SCHEMA)
        );
    }
}
