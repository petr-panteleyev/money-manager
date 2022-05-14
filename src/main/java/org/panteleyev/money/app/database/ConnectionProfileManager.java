/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.database;

import javafx.scene.control.Dialog;
import org.panteleyev.money.app.ApplicationFiles;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.panteleyev.money.app.GlobalContext.files;

public final class ConnectionProfileManager {
    private static final String PROFILE_PROPERTY = "profile";
    private static final String NO_AUTO_PROPERTY = "noauto";

    private boolean autoConnect = false;
    private ConnectionProfile defaultProfile = null;

    private final Map<String, ConnectionProfile> profiles = new HashMap<>();

    private final Function<ConnectionProfile, Exception> resetDatabaseCallback;
    private final Function<ConnectionProfile, DataSource> buildDataSourceCallback;

    public ConnectionProfileManager(Function<ConnectionProfile, Exception> initDatabaseCallback,
                                    Function<ConnectionProfile, DataSource> buildDataSourceCallback)
    {
        this.resetDatabaseCallback = Objects.requireNonNull(initDatabaseCallback);
        this.buildDataSourceCallback = Objects.requireNonNull(buildDataSourceCallback);
    }

    public boolean getAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean b) {
        autoConnect = b;
    }

    public Function<ConnectionProfile, Exception> getResetDatabaseCallback() {
        return resetDatabaseCallback;
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
        profList.forEach(p -> profiles.put(p.name(), p));
    }

    public void saveProfiles() {
        files().write(ApplicationFiles.AppFile.PROFILES, out -> new ProfileSettings(
            profiles.values(),
            defaultProfile == null ? "" : defaultProfile.name(),
            autoConnect
        ).save(out));
    }

    public void loadProfiles() {
        files().read(ApplicationFiles.AppFile.PROFILES, in -> {
            var settings = ProfileSettings.load(in);
            autoConnect = settings.autoConnect();
            profiles.clear();
            profiles.putAll(settings.profiles().stream()
                .collect(Collectors.toMap(ConnectionProfile::name, Function.identity())));
            var defaultProfileName = settings.defaultProfile();
            if (defaultProfileName != null && !defaultProfileName.isBlank()) {
                defaultProfile = profiles.get(defaultProfileName);
            } else {
                defaultProfile = null;
            }
        });
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
        profiles.remove(profile.name());
        if (defaultProfile == profile) {
            defaultProfile = null;
            autoConnect = false;
        }
    }

    public String getDatabaseHost(ConnectionProfile profile) {
        return profile.dataBaseHost();
    }

    public int getDatabasePort(ConnectionProfile profile) {
        return profile.dataBasePort();
    }

    public Dialog<?> getEditor() {
        return new ConnectionProfilesEditor(this);
    }

    public Optional<ConnectionProfile> getProfileToOpen() {
        if ("true".equalsIgnoreCase(System.getProperty(NO_AUTO_PROPERTY))) {
            return Optional.empty();
        }

        var profileName = System.getProperty(PROFILE_PROPERTY);
        if (profileName != null) {
            return Optional.ofNullable(get(profileName));
        }

        if (getAutoConnect()) {
            return getDefaultProfile();
        }

        return Optional.empty();
    }
}
