/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.Options;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.panteleyev.money.XMLUtils.closeTag;
import static org.panteleyev.money.XMLUtils.openTag;
import static org.panteleyev.money.XMLUtils.writeTag;
import static org.panteleyev.money.XMLUtils.writeXmlHeader;

public final class ConnectionProfileManager {
    private static final String PROFILES_FILE = "profiles.xml";

    private static boolean autoConnect = false;
    private static ConnectionProfile defaultProfile = null;

    private static Map<String, ConnectionProfile> profiles = new HashMap<>();

    public static boolean getAutoConnect() {
        return autoConnect;
    }

    public static void setAutoConnect(boolean b) {
        autoConnect = b;
    }

    public static ConnectionProfile getDefaultProfile() {
        return defaultProfile;
    }

    public static void setDefaultProfile(ConnectionProfile profile) {
        defaultProfile = profile;
    }

    public static void setProfiles(List<ConnectionProfile> profList) {
        profiles.clear();
        profList.forEach(p -> profiles.put(p.getName(), p));
    }

    public static void saveProfiles(OutputStream out) throws IOException {
        try (var w = new PrintWriter(out)) {
            writeXmlHeader(w);

            openTag(w, "MoneyManager");
            writeTag(w, "autoConnect", autoConnect);
            writeTag(w, "defaultProfileName", defaultProfile == null ? "" : defaultProfile.getName());

            openTag(w, "profiles");

            for (var profile : profiles.values()) {
                exportProfile(w, profile);
            }

            closeTag(w, "profiles");
            closeTag(w, "MoneyManager");
        }
    }

    public static void saveProfiles() {
        var file = new File(Options.getSettingsDirectory(), PROFILES_FILE);
        try (var out = new FileOutputStream(file)) {
            saveProfiles(out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static void loadProfiles(InputStream inputStream) throws Exception {
        var factory = SAXParserFactory.newInstance();
        var parser = factory.newSAXParser();

        var importParser = new ProfileXmlParser();
        parser.parse(inputStream, importParser);

        autoConnect = importParser.isAutoConnect();
        importParser.getProfiles().forEach(p -> profiles.put(p.getName(), p));
        defaultProfile = profiles.get(importParser.getDefaultProfileName());
    }

    public static void loadProfiles() {
        profiles.clear();

        var file = new File(Options.getSettingsDirectory(), PROFILES_FILE);
        if (!file.exists()) {
            return;
        }

        try (var inputStream = new FileInputStream(file)) {
            loadProfiles(inputStream);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Collection<ConnectionProfile> getAll() {
        return profiles.values();
    }

    public static ConnectionProfile get(String name) {
        return profiles.get(name);
    }

    public static void set(String name, ConnectionProfile profile) {
        profiles.put(name, profile);
    }

    public static int size() {
        return profiles.size();
    }

    public static void deleteProfile(ConnectionProfile profile) {
        profiles.remove(profile.getName());
        if (defaultProfile == profile) {
            defaultProfile = null;
            autoConnect = false;
        }
    }

    private ConnectionProfileManager() {
    }

    private static void exportProfile(Writer w, ConnectionProfile profile) throws IOException {
        openTag(w, "profile");

        writeTag(w, "name", profile.getName());
        writeTag(w, "type", profile.getType().name());
        writeTag(w, "dataBaseHost", profile.getDataBaseHost());
        writeTag(w, "dataBasePort", Integer.toString(profile.getDataBasePort()));
        writeTag(w, "dataBaseUser", profile.getDataBaseUser());
        writeTag(w, "dataBasePassword", profile.getDataBasePassword());
        writeTag(w, "schema", profile.getSchema());
        writeTag(w, "remoteHost", profile.getRemoteHost());
        writeTag(w, "remotePort", profile.getRemotePort());

        closeTag(w, "profile");
    }
}
