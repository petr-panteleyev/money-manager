/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public class ConnectionProfile {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_SCHEMA = "money";
    private static final int DEFAULT_SSH_PORT = 22;

    private final String name;
    private final ConnectionType type;
    private final String dataBaseHost;
    private final int dataBasePort;
    private final String dataBaseUser;
    private final String dataBasePassword;
    private final String schema;
    private final String remoteHost;
    private final int remotePort;

    public ConnectionProfile(String name, ConnectionType type, String dataBaseHost, int dataBasePort,
                             String dataBaseUser, String dataBasePassword, String schema, String remoteHost,
                             int remotePort) {
        this.name = name;
        this.type = type;
        this.dataBaseHost = dataBaseHost;
        this.dataBasePort = dataBasePort;
        this.dataBaseUser = dataBaseUser;
        this.dataBasePassword = dataBasePassword;
        this.schema = schema;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    /**
     * Profile for a typical MySQL connection.
     *
     * @param name             profile name
     * @param dataBaseHost     database host
     * @param dataBasePort     database port
     * @param dataBaseUser     database user
     * @param dataBasePassword database password
     * @param schema           schema
     */
    public ConnectionProfile(String name, String dataBaseHost, int dataBasePort, String dataBaseUser,
                             String dataBasePassword, String schema) {
        this(name, ConnectionType.TCP_IP, dataBaseHost, dataBasePort, dataBaseUser, dataBasePassword, schema,
                DEFAULT_HOST, DEFAULT_SSH_PORT);
    }

    public ConnectionProfile(String name, String dataBaseUser, String dataBasePassword) {
        this(name, ConnectionType.TCP_IP, DEFAULT_HOST, DEFAULT_PORT, dataBaseUser, dataBasePassword,
                DEFAULT_SCHEMA, DEFAULT_HOST, DEFAULT_SSH_PORT);
    }

    public ConnectionProfile(String name, String schema) {
        this(name, ConnectionType.TCP_IP, DEFAULT_HOST, DEFAULT_PORT, "", "",
                schema, DEFAULT_HOST, DEFAULT_SSH_PORT);
    }

    public String getName() {
        return name;
    }

    public ConnectionType getType() {
        return type;
    }

    public String getDataBaseHost() {
        return dataBaseHost;
    }

    public int getDataBasePort() {
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

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getConnectionString() {
        return "mysql://" + dataBaseHost + ":" + dataBasePort + "/" + schema;
    }

    public DataSource build() {
        var ds = new MysqlDataSource();

        ds.setEncoding("utf8");
        ds.setPort(dataBasePort);
        ds.setServerName(dataBaseHost);
        ds.setUser(dataBaseUser);
        ds.setPassword(dataBasePassword);
        ds.setDatabaseName(schema);

        return ds;
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
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.dataBaseHost, that.dataBaseHost)
                && this.dataBasePort == that.dataBasePort
                && Objects.equals(this.dataBaseUser, that.dataBaseUser)
                && Objects.equals(this.dataBasePassword, that.dataBasePassword)
                && Objects.equals(this.schema, that.schema)
                && Objects.equals(this.remoteHost, that.remoteHost)
                && this.remotePort == that.remotePort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, dataBaseHost, dataBasePort, dataBaseUser, dataBasePassword, schema,
                remoteHost, remotePort);
    }

    @Override
    public String toString() {
        return super.toString()
                + " name=" + name
                + " type=" + type
                + " dataBaseHost=" + dataBaseHost
                + " dataBasePort=" + dataBasePort
                + " dataBaseUser=" + dataBaseUser
                + " dataBasePassword=" + dataBasePassword
                + " schema=" + schema
                + " remoteHost=" + remoteHost
                + " remotePort=" + remotePort;
    }
}
