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
