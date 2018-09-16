/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.profiles;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProfileXmlParser extends DefaultHandler {
    private boolean autoConnect;
    private String defaultProfileName;
    private List<ConnectionProfile> profiles;

    private Map<String, String> tags = null;
    private String value = "";

    ProfileXmlParser() {
        autoConnect = false;
        defaultProfileName = "";
        profiles = new ArrayList<>();
    }

    boolean isAutoConnect() {
        return autoConnect;
    }

    String getDefaultProfileName() {
        return defaultProfileName;
    }

    public List<ConnectionProfile> getProfiles() {
        return profiles;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if ("profile".equals(qName)) {
            tags = new HashMap<>();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        switch (qName) {
            case "autoConnect":
                autoConnect = Boolean.parseBoolean(value);
                break;

            case "defaultProfileName":
                defaultProfileName = value;
                break;

            case "profile":
                var profile = parseProfile(tags);
                profiles.add(profile);
                tags = null;
                break;

            default:
                if (tags != null) {
                    tags.put(qName, value);
                }
                break;
        }

        value = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        value = new String(ch, start, length);
        if (value.trim().isEmpty()) {
            value = "";
        }
    }

    private static ConnectionProfile parseProfile(Map<String, String> tags) {
        return new ConnectionProfile(
                tags.get("name"),
                tags.get("dataBaseHost"),
                Integer.parseInt(tags.get("dataBasePort")),
                tags.get("dataBaseUser"),
                tags.get("dataBasePassword"),
                tags.get("schema"),
                tags.get("encryptionKey"),
                tags.get("sshSession"));
    }
}
