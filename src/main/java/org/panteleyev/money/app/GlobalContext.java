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
package org.panteleyev.money.app;

import org.panteleyev.money.app.settings.Settings;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;

public final class GlobalContext {
    private static final DataCache CACHE = new DataCache();
    private static final MoneyDAO DAO = new MoneyDAO(CACHE);
    private static final ApplicationFiles FILES = new ApplicationFiles();
    private static final Settings SETTINGS = new Settings(FILES);

    private GlobalContext() {
    }

    public static DataCache cache() {
        return CACHE;
    }

    public static MoneyDAO dao() {
        return DAO;
    }

    public static Settings settings() {
        return SETTINGS;
    }

    public static ApplicationFiles files() {
        return FILES;
    }
}
