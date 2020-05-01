package org.panteleyev.money.app.database;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Application;
import javafx.scene.control.Dialog;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class ConnectionProfileManager {
    private static final String PREF_ROOT = "database_connection_profiles";
    private static final String PREF_COUNT = "count";
    private static final String PREF_AUTO_CONNECT = "auto_connect";
    private static final String PREF_DEFAULT_PROFILE_NAME = "default_profile_name";
    private static final String PREF_PROFILE_ROOT = "profile.";

    private static final String CMD_LINE_PARAM = "profile";

    private boolean autoConnect = false;
    private ConnectionProfile defaultProfile = null;

    private Map<String, ConnectionProfile> profiles = new HashMap<>();

    private final Function<ConnectionProfile, Exception> initDatabaseCallback;
    private final Function<ConnectionProfile, DataSource> buildDataSourceCallback;
    private final Preferences preferencesParent;

    public ConnectionProfileManager(Function<ConnectionProfile, Exception> initDatabaseCallback,
                                    Function<ConnectionProfile, DataSource> buildDataSourceCallback,
                                    Preferences preferencesParent)
    {
        Objects.requireNonNull(initDatabaseCallback);
        Objects.requireNonNull(buildDataSourceCallback);
        Objects.requireNonNull(preferencesParent);

        this.initDatabaseCallback = initDatabaseCallback;
        this.buildDataSourceCallback = buildDataSourceCallback;
        this.preferencesParent = preferencesParent;
    }

    public boolean getAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean b) {
        autoConnect = b;
    }

    public Function<ConnectionProfile, Exception> getInitDatabaseCallback() {
        return initDatabaseCallback;
    }

    public Function<ConnectionProfile, DataSource> getBuildDataSourceCallback() {
        return buildDataSourceCallback;
    }

    public Optional<ConnectionProfile> getDefaultProfile() {
        return Optional.ofNullable(defaultProfile);
    }

    public void setDefaultProfile(ConnectionProfile profile) {
        defaultProfile = profile;
    }

    public void setProfiles(List<ConnectionProfile> profList) {
        profiles.clear();
        profList.forEach(p -> profiles.put(p.getName(), p));
    }

    public void saveProfiles() {
        saveProfiles(preferencesParent);
    }

    private void saveProfiles(Preferences parent) {
        parent.remove(PREF_ROOT);
        var root = parent.node(PREF_ROOT);

        root.putBoolean(PREF_AUTO_CONNECT, autoConnect);
        root.put(PREF_DEFAULT_PROFILE_NAME,
            defaultProfile == null ? "" : defaultProfile.getName());
        root.putInt(PREF_COUNT, profiles.size());

        var index = 0;
        for (var profile : profiles.values()) {
            var node = root.node(PREF_PROFILE_ROOT + index);
            saveProfile(profile, node);
            index++;
        }
    }

    public void loadProfiles() {
        loadProfiles(preferencesParent);
    }

    private void loadProfiles(Preferences parent) {
        var root = parent.node(PREF_ROOT);

        autoConnect = root.getBoolean(PREF_AUTO_CONNECT, false);

        profiles.clear();

        var count = root.getInt(PREF_COUNT, 0);
        for (int index = 0; index < count; index++) {
            var nodeName = PREF_PROFILE_ROOT + index;
            try {
                if (!root.nodeExists(nodeName)) {
                    break;
                }
            } catch (BackingStoreException ex) {
                break;
            }

            var profile = loadProfile(root.node(nodeName));
            profiles.put(profile.getName(), profile);
        }

        var defaultProfileName = root.get(PREF_DEFAULT_PROFILE_NAME, "");
        if (!defaultProfileName.isEmpty()) {
            defaultProfile = profiles.get(defaultProfileName);
        }
    }

    public Collection<ConnectionProfile> getAll() {
        return profiles.values();
    }

    public ConnectionProfile get(String name) {
        return profiles.get(name);
    }

    public void set(String name, ConnectionProfile profile) {
        profiles.put(name, profile);
    }

    public int size() {
        return profiles.size();
    }

    public void deleteProfile(ConnectionProfile profile) {
        profiles.remove(profile.getName());
        if (defaultProfile == profile) {
            defaultProfile = null;
            autoConnect = false;
        }
    }

    private static void saveProfile(ConnectionProfile profile, Preferences node) {
        node.put("name", profile.getName());
        node.put("database_host", profile.getDataBaseHost());
        node.putInt("database_port", profile.getDataBasePort());
        node.put("database_user", profile.getDataBaseUser());
        node.put("database_password", profile.getDataBasePassword());
        node.put("schema", profile.getSchema());
        node.put("encryption_key", profile.getEncryptionKey());
    }

    private static ConnectionProfile loadProfile(Preferences node) {
        return new ConnectionProfile(
            node.get("name", ""),
            node.get("database_host", "localhost"),
            node.getInt("database_port", 3306),
            node.get("database_user", ""),
            node.get("database_password", ""),
            node.get("schema", ""),
            node.get("encryption_key", "")
        );
    }

    public String getDatabaseHost(ConnectionProfile profile) {
        return profile.getDataBaseHost();
    }

    public int getDatabasePort(ConnectionProfile profile) {
        return profile.getDataBasePort();
    }

    public Dialog getEditor(boolean useEncryption) {
        return new ConnectionProfilesEditor(this, useEncryption);
    }

    public Optional<ConnectionProfile> getProfileToOpen(Application application) {
        Application.Parameters params = application.getParameters();

        var profileName = params.getNamed().get(CMD_LINE_PARAM);
        if (profileName != null) {
            return Optional.ofNullable(get(profileName));
        }

        if (getAutoConnect()) {
            return getDefaultProfile();
        }

        return Optional.empty();
    }
}
