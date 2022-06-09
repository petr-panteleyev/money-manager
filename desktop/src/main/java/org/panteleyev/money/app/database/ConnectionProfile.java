/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.database;

/**
 * Profile for a typical database connection.
 */
public record ConnectionProfile(String name, String dataBaseHost, int dataBasePort,
                                String dataBaseUser, String dataBasePassword, String schema
) {
    ConnectionProfile(String name, String schema) {
        this(name, "localhost", 3306, "", "", schema);
    }

    public String getConnectionString() {
        return getConnectionString(dataBasePort);
    }

    public String getConnectionString(int port) {
        return "mysql://" + dataBaseHost + ":" + port + "/" + schema;
    }
}
