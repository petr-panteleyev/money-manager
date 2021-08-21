/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import org.panteleyev.money.app.settings.Settings;
import org.panteleyev.money.messaging.MessageQueue;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;

public final class GlobalContext {
    private static final DataCache CACHE = new DataCache();
    private static final MoneyDAO DAO = new MoneyDAO(CACHE);
    private static final ApplicationFiles FILES = new ApplicationFiles();
    private static final Settings SETTINGS = new Settings(FILES);
    private static final MessageQueue QUEUE = new MessageQueue();

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

    public static MessageQueue queue() {
        return QUEUE;
    }
}
