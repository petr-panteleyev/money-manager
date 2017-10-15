/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

import javax.xml.bind.annotation.XmlElement;

public class ProfileXml {
    private String name = "";
    private ConnectionType type = ConnectionType.TCP_IP;
    private String dataBaseHost = ConnectionProfile.DEFAULT_HOST;
    private int dataBasePort = ConnectionProfile.DEFAULT_PORT;
    private String dataBaseUser;
    private String dataBasePassword;
    private String schema = ConnectionProfile.DEFAULT_SCHEMA;
    private String remoteHost = ConnectionProfile.DEFAULT_HOST;
    private int remotePort = ConnectionProfile.DEFAULT_SSH_PORT;

    public ProfileXml() {
    }

    public ProfileXml(ConnectionProfile profile) {
        this.name = profile.getName();
        this.type = profile.getType();
        this.dataBaseHost = profile.getDataBaseHost();
        this.dataBasePort = profile.getDataBasePort();
        this.dataBaseUser = profile.getDataBaseUser();
        this.dataBasePassword = profile.getDataBasePassword();
        this.schema = profile.getSchema();
        this.remoteHost = profile.getRemoteHost();
        this.remotePort = profile.getRemotePort();
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "type")
    public ConnectionType getType() {
        return type;
    }

    public void setType(ConnectionType type) {
        this.type = type;
    }

    @XmlElement(name = "dataBaseHost")
    public String getDataBaseHost() {
        return dataBaseHost;
    }

    public void setDataBaseHost(String dataBaseHost) {
        this.dataBaseHost = dataBaseHost;
    }

    @XmlElement(name = "dataBasePort")
    public int getDataBasePort() {
        return dataBasePort;
    }

    public void setDataBasePort(int dataBasePort) {
        this.dataBasePort = dataBasePort;
    }

    @XmlElement(name = "dataBaseUser")
    public String getDataBaseUser() {
        return dataBaseUser;
    }

    public void setDataBaseUser(String dataBaseUser) {
        this.dataBaseUser = dataBaseUser;
    }

    @XmlElement(name = "dataBasePassword")
    public String getDataBasePassword() {
        return dataBasePassword;
    }

    public void setDataBasePassword(String dataBasePassword) {
        this.dataBasePassword = dataBasePassword;
    }

    @XmlElement(name = "schema")
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @XmlElement(name = "remoteHost")
    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @XmlElement(name = "remotePort")
    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public ConnectionProfile profile() {
        return new ConnectionProfile(name, type, dataBaseHost, dataBasePort, dataBaseUser, dataBasePassword,
                schema, remoteHost, remotePort);
    }
}
