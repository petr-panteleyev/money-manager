/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void saveProfiles() {
        ProfileListXml xmlList = new ProfileListXml(autoConnect,
                defaultProfile == null ? "" : defaultProfile.getName(),
                profiles.values());

        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlList, new File(Options.getSettingsDirectory(), PROFILES_FILE));
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void loadProfiles() {
        profiles.clear();

        File file = new File(Options.getSettingsDirectory(), PROFILES_FILE);
        if (!file.exists()) {
            return;
        }

        try {
            ProfileListXml loaded = (ProfileListXml) getContext().createUnmarshaller().unmarshal(file);
            autoConnect = loaded.isAutoConnect();
            loaded.getProfiles().forEach(p -> profiles.put(p.getName(), p.profile()));
            defaultProfile = profiles.get(loaded.getDefaultProfileName());
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static JAXBContext getContext() {
        try {
            return JAXBContext.newInstance(ProfileListXml.class, ProfileXml.class);
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
}
