/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
