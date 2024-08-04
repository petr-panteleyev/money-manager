/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.app.settings.Settings;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.desktop.persistence.MoneyDAO;

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
