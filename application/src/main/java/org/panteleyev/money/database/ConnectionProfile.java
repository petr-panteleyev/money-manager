/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.database;

import java.util.Objects;

public class ConnectionProfile {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;

    private final String name;
    private final String dataBaseHost;
    private final int dataBasePort;
    private final String dataBaseUser;
    private final String dataBasePassword;
    private final String schema;
    private final String encryptionKey;

    /**
     * Profile for a typical database connection.
     *
     * @param name             profile name
     * @param dataBaseHost     database host
     * @param dataBasePort     database port
     * @param dataBaseUser     database user
     * @param dataBasePassword database password
     * @param schema           schema
     */
    ConnectionProfile(String name, String dataBaseHost, int dataBasePort,
                      String dataBaseUser, String dataBasePassword, String schema,
                      String encryptionKey) {
        this.name = name;
        this.dataBaseHost = dataBaseHost;
        this.dataBasePort = dataBasePort;
        this.dataBaseUser = dataBaseUser;
        this.dataBasePassword = dataBasePassword;
        this.schema = schema;
        this.encryptionKey = encryptionKey;
    }

    ConnectionProfile(String name, String schema) {
        this(name, DEFAULT_HOST, DEFAULT_PORT, "", "",
                schema, "");
    }

    public String getName() {
        return name;
    }

    String getDataBaseHost() {
        return dataBaseHost;
    }

    int getDataBasePort() {
        return dataBasePort;
    }

    public String getDataBaseUser() {
        return dataBaseUser;
    }

    public String getDataBasePassword() {
        return dataBasePassword;
    }

    public String getSchema() {
        return schema;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getConnectionString() {
        return getConnectionString(dataBasePort);
    }

    public String getConnectionString(int port) {
        return "mysql://" + dataBaseHost + ":" + port + "/" + schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConnectionProfile)) {
            return false;
        }

        var that = (ConnectionProfile) o;

        return Objects.equals(this.name, that.name)
                && Objects.equals(this.dataBaseHost, that.dataBaseHost)
                && this.dataBasePort == that.dataBasePort
                && Objects.equals(this.dataBaseUser, that.dataBaseUser)
                && Objects.equals(this.dataBasePassword, that.dataBasePassword)
                && Objects.equals(this.schema, that.schema)
                && Objects.equals(this.encryptionKey, that.encryptionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataBaseHost, dataBasePort, dataBaseUser, dataBasePassword, schema,
                encryptionKey);
    }

    @Override
    public String toString() {
        return super.toString()
                + " name=" + name
                + " dataBaseHost=" + dataBaseHost
                + " dataBasePort=" + dataBasePort
                + " dataBaseUser=" + dataBaseUser
                + " dataBasePassword=" + dataBasePassword
                + " schema=" + schema;
    }
}
